package dev.wolveringer.tdft.tests.h3;

import dev.wolveringer.tdft.TestContext;
import dev.wolveringer.tdft.source.TestSource;
import dev.wolveringer.tdft.test.Helpers;
import dev.wolveringer.tdft.test.TestSuite;
import dev.wolveringer.tdft.test.TestUnit;
import org.apache.commons.lang3.Validate;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class PascalsTriangle extends TestUnit  {
    public PascalsTriangle() {
        super("PascalsTriangle");
    }

    @Override
    public boolean executable(TestSource source) {
        return source.getProjectName().toLowerCase().startsWith("h03_");
    }

    @Override
    protected void registerTests(TestContext ctx) {
        this.setupMethodTests();
        this.registerTest(testFaculty, "faculty algo test");
    }

    private void setupMethodTests() {
        this.registerTest(context -> {
            context.getHelper()
                    .resolveMethod("h3.PascalsTriangle", "faculty", Modifier.PUBLIC, int.class, int.class);
        }, "test method availability for faculty");

        this.registerTest(context -> {
            context.getHelper()
                    .resolveMethod("h3.PascalsTriangle", "binomialCoefficient", Modifier.PUBLIC, int.class, int.class, int.class);
        }, "test method availability for binomialCoefficient");

        this.registerTest(context -> {
            context.getHelper()
                    .resolveMethod("h3.PascalsTriangle", "triangleOfBinCoeff", Modifier.PUBLIC, int[].class, int.class);
        }, "test method availability for triangleOfBinCoeff");

        this.registerTest(context -> {
            context.getHelper()
                    .resolveMethod("h3.PascalsTriangle", "pascalsTriangle", Modifier.PUBLIC, int[].class, int.class);
        }, "test method availability for pascalsTriangle");
    }

    private TestSuite testFaculty = context -> {
        final Helpers helpers = context.getHelper();

        Object pt = helpers.createInstance("h3.PascalsTriangle");
        Method m = helpers.resolveMethod(pt, "faculty", Modifier.PUBLIC, int.class, int.class);

        Map<Integer, Integer> testSet = new HashMap<>();
        testSet.put(0, 1);
        testSet.put(1, 1);
        testSet.put(2, 2);
        testSet.put(3, 6);
        testSet.put(4, 24);
        testSet.put(5, 120);
        testSet.put(6, 720);
        testSet.put(7, 5040);
        testSet.put(12, 479001600);
        for(Map.Entry<Integer, Integer> pair : testSet.entrySet())
            helpers.executeWithExpect(pt, m, pair.getValue(), Comparator.comparingInt(a -> a), pair.getKey());
    };
}
