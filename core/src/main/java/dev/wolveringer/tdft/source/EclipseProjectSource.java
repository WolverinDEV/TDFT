package dev.wolveringer.tdft.source;

import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xeustechnologies.jcl.JarClassLoader;
import org.xeustechnologies.jcl.ProxyClassLoader;

import javax.tools.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class EclipseProjectSource extends ProxyClassLoader implements TestSource {
    private static final Logger logger = LoggerFactory.getLogger(EclipseProjectSource.class);

    private final String filePath;
    private ZipFile file;

    /* Only after initialize */
    @Getter private String projectName;

    private Set<File> srcRoots;
    private File generatedClassRoot;
    private Set<File> libPaths;

    private File projectRoot;

    @Getter
    private JarClassLoader classLoader;

    public EclipseProjectSource(@NonNull String projectPath) {
        this.filePath = projectPath;
    }

    public String getProjectName() {
        return this.projectName;
    }

    @Override
    public void initialize() {
        File file = new File(this.filePath);
        Validate.isTrue(file.getName().endsWith(".zip"), "Source file does not end with .zip");
        Validate.isTrue(file.exists() && file.canRead(), "Source file is missing or not accessible");

        try {
            this.file = new ZipFile(file);
        } catch (IOException e) {
            throw new RuntimeException("failed to open zip archive", e);
        }

        this.classLoader = new JarClassLoader();
        this.classLoader.addLoader(this);

        try {
            File root = File.createTempFile("fop_compile_", "");
            if(!root.isDirectory()) {
                if(root.exists())
                    root.delete();
                Validate.isTrue(root.mkdirs(), "Failed to generate temp dir");
            }
            root.deleteOnExit();
            this.projectRoot = root;

            this.generatedClassRoot = new File(root, "__generated");
            Validate.isTrue(this.generatedClassRoot.mkdirs(), "Failed to generate temp lib dir");
        } catch(IOException ex) {
            throw new RuntimeException("failed to generate temp files", ex);
        }

        try {
            this.loadProject();
        } catch (Exception e) {
            throw new RuntimeException("failed to load project", e);
        } finally {
            try {
                this.file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void finalize() {
        if(this.classLoader != null) {
            for(String klass : new ArrayList<>(this.classLoader.getLoadedClasses().keySet()))
                this.classLoader.unloadClass(klass);
            this.classLoader = null;
        }
    }

    private NodeList documentXPath(Node root, String path) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        return (NodeList) xPath.compile(path).evaluate(root, XPathConstants.NODESET);
    }

    private Stream<File> gatherJavaFiles(File file, int maxDeep) {
        if(file.isFile()) {
            if(!file.getName().endsWith(".java"))
                return Stream.empty();
            return Stream.of(file);
        } else if(file.isDirectory()) {
            if(maxDeep == 0) return Stream.empty();

            File[] files = file.listFiles();
            if(files == null) {
                logger.warn("Failed to list files in directory " + file);
                return Stream.empty();
            }

            return Stream.of(files).filter(e -> e.getName().endsWith(".java") || e.isDirectory()).flatMap(e -> this.gatherJavaFiles(e, maxDeep - 1));
        }

        return Stream.empty();
    }

    private void compile(Iterable<File> sourceDirectories) throws Exception {
        logger.info("Compiling project.");
        long time_begin = System.currentTimeMillis();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, Locale.getDefault(), null);

        List<JavaFileObject> javaFiles = StreamSupport.stream(sourceDirectories.spliterator(), false)
                                            .flatMap(e -> gatherJavaFiles(e, -1))
                                            .flatMap(e -> StreamSupport.stream(fileManager.getJavaFileObjects(e).spliterator(), false))
                                            .collect(Collectors.toList());
        logger.trace("Found " + javaFiles.size() + " source files.");
        if(javaFiles.isEmpty()) return; /* Nothing to compile */

        List<String> options = new ArrayList<>();
        {
            /* dest dir */
            options.add("-d");
            options.add(this.generatedClassRoot.getAbsolutePath());

            /* all libs */
            this.libPaths.forEach(e -> {
                options.add("-classpath");
                options.add(e.getAbsolutePath());
            });
        }

        JavaCompiler.CompilationTask compilerTask = compiler.getTask(null, fileManager, diagnostics, options, null, javaFiles) ;

        if (!compilerTask.call()) {
            for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
                System.err.format("Error on line %d in %s", diagnostic.getLineNumber(), diagnostic);
            }
            throw new Exception("Could not compile project");
        }

        long time_end = System.currentTimeMillis();
        logger.debug("Compiled " + javaFiles.size() + " project files within " + (time_end - time_begin) + "ms.");
    }

    private void extractProject() throws Exception {
        logger.debug("Extracting project to " + this.projectRoot);

        Enumeration<? extends ZipEntry> entries = this.file.entries();
        while(entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();

            File targetFile = new File(this.projectRoot, entry.getName());
            File targetDirectory = targetFile.getParentFile();

            if(!targetDirectory.isDirectory())
                Validate.isTrue(targetDirectory.mkdirs(), "Failed to create directories for project file " + entry.getName());

            FileUtils.copyToFile(this.file.getInputStream(entry), targetFile);
        }
    }

    private void loadProject() throws Exception {
        logger.info("Loading project at " + this.filePath);
        this.extractProject();

        /* searching for the main meta info files */
        File classPathFile = null, projectFile = null;
        {
            File[] directories = this.projectRoot.listFiles();
            Validate.notNull(directories, "Failed to list project directories in tmp project.");

            /* search only on the second layer! */
            Stream<File> metaInfos = Stream.of(directories)
                                        .map(File::listFiles)
                                        .flatMap(Stream::of)
                                        .filter(e -> e.getName().endsWith(".classpath") || e.getName().endsWith(".project"));

            for(File f : metaInfos.collect(Collectors.toList())) {
                if(f.getName().endsWith(".project"))
                    projectFile = f;
                else if(f.getName().endsWith(".classpath"))
                    classPathFile = f;
            }

            Validate.isTrue(projectFile != null, "Failed to find .project file");
            Validate.isTrue(classPathFile != null, "Failed to find .classpath file");

            this.projectName = Paths.get(projectFile.getParentFile().getName()).getFileName().toString();
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();

        /* validate .project file */
        try {
            Document doc = db.parse(projectFile);

            doc.getDocumentElement().normalize();
            NodeList nl = this.documentXPath(doc, "projectDescription/name");
            Validate.isTrue(nl.getLength() == 1, "Failed to retrieve name property");

            String name = nl.item(0).getTextContent();
            Validate.isTrue(name.equals(this.projectName), "Project name does not matches with exported name! (" + name + " != " + this.projectName + ")");

            //TODO: Test for the project nature (org.eclipse.jdt.core.javanature)
        } catch (Exception e) {
            throw new IOException("failed to parse .project file", e);
        }

        /* load info from the classpath file */
        try {
            this.srcRoots = new HashSet<>();
            this.libPaths = new HashSet<>();

            Document doc = db.parse(classPathFile);

            logger.trace("Parsing .classpath file");
            doc.getDocumentElement().normalize();
            {
                NodeList nl = this.documentXPath(doc, "classpath/classpathentry[@kind='src']");
                for(int i = 0; i < nl.getLength(); i++) {
                    String path = nl.item(i).getAttributes().getNamedItem("path").getNodeValue();
                    logger.trace("  Added " + path + " to source path list. Indexed source files:");

                    File rootBase = new File(this.projectRoot, this.projectName + "/" + path);

                    /* fix all files which are im project root and move them into a package, so they're addressable */
                    File default_package = new File(rootBase, "__default");
                    if(!default_package.isDirectory())
                        Validate.isTrue(default_package.mkdirs(), "Failed to generate default package directory");

                    this.gatherJavaFiles(rootBase, 1).forEach(e -> {
                        File target = new File(default_package, Paths.get(e.getAbsolutePath()).getFileName().toString());
                        try {
                            FileUtils.moveFile(e, target);
                        } catch (IOException ex) {
                            throw new RuntimeException("failed to move file " + e.getName() + " to the new default dir", ex);
                        }

                        try {
                            String content = FileUtils.readFileToString(target, Charset.defaultCharset());
                            Validate.isTrue(!content.startsWith("package"), "Java file is in default path, but has a package specified. That's not possible.");

                            content = "package __default;" + System.lineSeparator() +
                                      "import __default.*;" + System.lineSeparator() +
                                      content;

                            FileUtils.write(target, content, Charset.defaultCharset());
                        } catch (IOException ex) {
                            throw new RuntimeException("failed to register new package in moved file", ex);
                        }

                    });

                    this.srcRoots.add(rootBase);
                }
            }
            {
                NodeList nl = this.documentXPath(doc, "classpath/classpathentry[@kind='lib']");
                for(int i = 0; i < nl.getLength(); i++) {
                    String path = nl.item(i).getAttributes().getNamedItem("path").getNodeValue();
                    logger.trace("  Added " + path + " to library path list.");

                    File rootBase = new File(this.projectRoot, this.projectName + "/" + path);
                    this.libPaths.add(rootBase);
                }
            }

            logger.debug("Found " + this.srcRoots.size() + " source paths and " + this.libPaths.size() + " libraries");
        } catch (Exception e) {
            throw new IOException("failed to parse .project file", e);
        }

        /* compile */
        this.compile(this.srcRoots);

        /* add to class loader */
        this.classLoader.add(this.generatedClassRoot.getAbsolutePath() + "/");
        for(File lib : this.libPaths)
            this.classLoader.add(lib.toString());
    }

    @Override
    public File getWorkingDirectory() {
        return new File(this.projectRoot, this.projectName);
    }

    @Override
    public Class loadClass(String klass, boolean resolve) {
        logger.trace("Request for loading class " + klass + " (resolve: " + resolve + ")");
        return null;
    }

    @Override
    public InputStream loadResource(String path) {
        logger.trace("Request for loading resource " + path);
        return null;
    }

    @Override
    public URL findResource(String s) {
        logger.trace("Request to find resource " + s);
        return null;
    }
}
