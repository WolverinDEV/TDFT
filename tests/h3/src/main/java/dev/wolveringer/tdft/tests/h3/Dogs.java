package dev.wolveringer.tdft.tests.h3;

import dev.wolveringer.tdft.TestContext;
import dev.wolveringer.tdft.source.TestSource;
import dev.wolveringer.tdft.test.Test;
import dev.wolveringer.tdft.test.TestUnit;
import org.apache.commons.lang3.Validate;

import java.lang.reflect.Modifier;
import java.util.Comparator;

public class Dogs extends TestUnit {
    public Dogs() {
        super("Heritages");
    }

    @Override
    public boolean executable(TestSource source) {
        return source.getProjectName().toLowerCase().startsWith("h03_");
    }

    @Override
    protected void registerTests(TestContext context) {
        Test[] htests = this.registerRunHeritageTests();
        this.registerFunctionalityTest(htests);
    }

    private Test[] registerRunHeritageTests() {
        Test[] result = new Test[3];

        Test testInterfaceRun = this.registerTest(context -> {
            Class<?> klass = context.getHelper().resolveClass("h3.RunningBehavior");
            Validate.isTrue(klass.isInterface(), "RunningBehavior must be an interface");
            context.getHelper().resolveMethod(klass, "run", 0, String.class);
        }, "interface test RunningBehavior");

        Test testInterfaceBark = this.registerTest(context -> {
            Class<?> klass = context.getHelper().resolveClass("h3.BarkBehavior");
            Validate.isTrue(klass.isInterface(), "BarkBehavior must be an interface");
            context.getHelper().resolveMethod(klass, "bark", 0, String.class);
        }, "interface test BarkBehavior");

        Test testClassDog = this.registerTest(context -> {
            Class<?> klass = context.getHelper().resolveClass("h3.Dog");
            Validate.isTrue(Modifier.isAbstract(klass.getModifiers()), "Dog must be an abstract class");
            context.getHelper().resolveMethod(klass, "bark", Modifier.PUBLIC, String.class);
            context.getHelper().resolveMethod(klass, "run", Modifier.PUBLIC, String.class);
        }, "class test BarkBehavior");

        result[0] = this.registerTest(context -> {
            Class<?> klass = context.getHelper().resolveClass("h3.RunningBehavior");

            Class<?> klassNoRunning = context.getHelper().resolveClass("h3.NoRunning");
            Class<?> klassSlowRunning = context.getHelper().resolveClass("h3.SlowRunning");
            Class<?> klassFastRunning = context.getHelper().resolveClass("h3.FastRunning");

            context.getHelper().ensureImplements(klassNoRunning, klass);
            context.getHelper().ensureImplements(klassSlowRunning, klass);
            context.getHelper().ensureImplements(klassFastRunning, klass);
        }, "test run heritages").requireTest(testInterfaceRun);

        result[1] = this.registerTest(context -> {
            Class<?> klass = context.getHelper().resolveClass("h3.BarkBehavior");

            Class<?> klassNoBarking = context.getHelper().resolveClass("h3.NoBarking");
            Class<?> klassQuietBarking = context.getHelper().resolveClass("h3.QuietBarking");
            Class<?> klassLoudBarking = context.getHelper().resolveClass("h3.LoudBarking");

            context.getHelper().ensureImplements(klassNoBarking, klass);
            context.getHelper().ensureImplements(klassQuietBarking, klass);
            context.getHelper().ensureImplements(klassLoudBarking, klass);
        }, "test bark heritages").requireTest(testInterfaceBark);

        result[2] = this.registerTest(context -> {
            Class<?> klass = context.getHelper().resolveClass("h3.Dog");

            Class<?> klassDogPuppet = context.getHelper().resolveClass("h3.DogPuppet");
            Class<?> klassStandardDog = context.getHelper().resolveClass("h3.StandardDog");
            Class<?> klassGermanShepherd = context.getHelper().resolveClass("h3.GermanShepherd");
            Class<?> klassPoodle = context.getHelper().resolveClass("h3.Poodle");

            context.getHelper().ensureExtends(klassDogPuppet, klass);
            context.getHelper().ensureExtends(klassStandardDog, klass);
            context.getHelper().ensureExtends(klassGermanShepherd, klass);
            context.getHelper().ensureExtends(klassPoodle, klass);
        }, "test bark heritages").requireTest(testInterfaceBark);

        return result;
    }

    private void registerFunctionalityTest(Test[] heritageTests) {
        this.registerTest(context -> {
            Object dog = context.getHelper().createInstance("h3.DogPuppet");

            context.getHelper().executeWithExpect(dog, "run", Modifier.PUBLIC, "no running", String::equals);
            context.getHelper().executeWithExpect(dog, "bark", Modifier.PUBLIC, "no barking", String::equals);
        }, "test DogPuppet").requireTest(heritageTests);

        this.registerTest(context -> {
            Object dog = context.getHelper().createInstance("h3.StandardDog");

            context.getHelper().executeWithExpect(dog, "run", Modifier.PUBLIC, "slow running", String::equals);
            context.getHelper().executeWithExpect(dog, "bark", Modifier.PUBLIC, "loud barking", String::equals);
        }, "test StandardDog").requireTest(heritageTests);

        this.registerTest(context -> {
            Object dog = context.getHelper().createInstance("h3.Poodle");

            context.getHelper().executeWithExpect(dog, "run", Modifier.PUBLIC, "slow running", String::equals);
            context.getHelper().executeWithExpect(dog, "bark", Modifier.PUBLIC, "quiet barking", String::equals);
        }, "test Poodle").requireTest(heritageTests);

        this.registerTest(context -> {
            Object dog = context.getHelper().createInstance("h3.GermanShepherd");

            context.getHelper().executeWithExpect(dog, "run", Modifier.PUBLIC, "fast running", String::equals);
            context.getHelper().executeWithExpect(dog, "bark", Modifier.PUBLIC, "loud barking", String::equals);
        }, "test GermanShepherd").requireTest(heritageTests);
    }
}
