package dev.wolveringer.tdft.tests.h3;

import dev.wolveringer.tdft.TestContext;
import dev.wolveringer.tdft.source.TestSource;
import dev.wolveringer.tdft.test.Helpers;
import dev.wolveringer.tdft.test.Test;
import dev.wolveringer.tdft.test.TestSuite;
import dev.wolveringer.tdft.test.TestUnit;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
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
        this.registerTriCoeffTests();
        this.registerFacultyTests();
        this.registerBinCoeffTests();
        this.registerPascalTests();
        this.registerTriCoeffTests();;
    }

    private static Map<Integer, Integer[]> pascTriTestSet = new HashMap<>();
    static {
        pascTriTestSet.put(0, new Integer[]{1});
        pascTriTestSet.put(1, new Integer[]{1,1,1});
        pascTriTestSet.put(2, new Integer[]{1,1,1,1,2,1});
        pascTriTestSet.put(3, new Integer[]{1,1,1,1,2,1,1,3,3,1});
        pascTriTestSet.put(4, new Integer[]{1,1,1,1,2,1,1,3,3,1,1,4,6,4,1});
        pascTriTestSet.put(5, new Integer[]{1,1,1,1,2,1,1,3,3,1,1,4,6,4,1,1,5,10,10,5,1});
        pascTriTestSet.put(6, new Integer[]{1,1,1,1,2,1,1,3,3,1,1,4,6,4,1,1,5,10,10,5,1,1,6,15,20,15,6,1});
        pascTriTestSet.put(7, new Integer[]{1,1,1,1,2,1,1,3,3,1,1,4,6,4,1,1,5,10,10,5,1,1,6,15,20,15,6,1,1,7,21,35,35,21,7,1});
        pascTriTestSet.put(8, new Integer[]{1,1,1,1,2,1,1,3,3,1,1,4,6,4,1,1,5,10,10,5,1,1,6,15,20,15,6,1,1,7,21,35,35,21,7,1,1,8,28,56,70,56,28,8,1});
        pascTriTestSet.put(9, new Integer[]{1,1,1,1,2,1,1,3,3,1,1,4,6,4,1,1,5,10,10,5,1,1,6,15,20,15,6,1,1,7,21,35,35,21,7,1,1,8,28,56,70,56,28,8,1,1,9,36,84,126,126,84,36,9,1});
        pascTriTestSet.put(10, new Integer[]{1,1,1,1,2,1,1,3,3,1,1,4,6,4,1,1,5,10,10,5,1,1,6,15,20,15,6,1,1,7,21,35,35,21,7,1,1,8,28,56,70,56,28,8,1,1,9,36,84,126,126,84,36,9,1,1,10,45,120,210,252,210,120,45,10,1});
        pascTriTestSet.put(11, new Integer[]{1,1,1,1,2,1,1,3,3,1,1,4,6,4,1,1,5,10,10,5,1,1,6,15,20,15,6,1,1,7,21,35,35,21,7,1,1,8,28,56,70,56,28,8,1,1,9,36,84,126,126,84,36,9,1,1,10,45,120,210,252,210,120,45,10,1,1,11,55,165,330,462,462,330,165,55,11,1});
        pascTriTestSet.put(12, new Integer[]{1,1,1,1,2,1,1,3,3,1,1,4,6,4,1,1,5,10,10,5,1,1,6,15,20,15,6,1,1,7,21,35,35,21,7,1,1,8,28,56,70,56,28,8,1,1,9,36,84,126,126,84,36,9,1,1,10,45,120,210,252,210,120,45,10,1,1,11,55,165,330,462,462,330,165,55,11,1,1,12,66,220,495,792,924,792,495,220,66,12,1});
        pascTriTestSet.put(13, new Integer[]{1,1,1,1,2,1,1,3,3,1,1,4,6,4,1,1,5,10,10,5,1,1,6,15,20,15,6,1,1,7,21,35,35,21,7,1,1,8,28,56,70,56,28,8,1,1,9,36,84,126,126,84,36,9,1,1,10,45,120,210,252,210,120,45,10,1,1,11,55,165,330,462,462,330,165,55,11,1,1,12,66,220,495,792,924,792,495,220,66,12,1,1,13,78,286,715,1287,1716,1716,1287,715,286,78,13,1});
        pascTriTestSet.put(14, new Integer[]{1,1,1,1,2,1,1,3,3,1,1,4,6,4,1,1,5,10,10,5,1,1,6,15,20,15,6,1,1,7,21,35,35,21,7,1,1,8,28,56,70,56,28,8,1,1,9,36,84,126,126,84,36,9,1,1,10,45,120,210,252,210,120,45,10,1,1,11,55,165,330,462,462,330,165,55,11,1,1,12,66,220,495,792,924,792,495,220,66,12,1,1,13,78,286,715,1287,1716,1716,1287,715,286,78,13,1,1,14,91,364,1001,2002,3003,3432,3003,2002,1001,364,91,14,1});
    }

    private void registerTriCoeffTests() {
        Test methodAvail = this.registerTest(context -> {
            context.getHelper()
                    .resolveMethod("h3.PascalsTriangle", "triangleOfBinCoeff", Modifier.PUBLIC, Integer[].class, int.class);
        }, "test method availability for triangleOfBinCoeff");

        this.registerTest(context -> {
            final Helpers helpers = context.getHelper();

            Object pt = helpers.createInstance("h3.PascalsTriangle");
            Method m = helpers.resolveMethod(pt, "triangleOfBinCoeff", Modifier.PUBLIC, Integer[].class, int.class);

            for(Map.Entry<Integer, Integer[]> pair : pascTriTestSet.entrySet())
                helpers.executeWithExpect(pt, m, pair.getValue(), (a, b) -> {
                    //Arrays.equals(a, b) ? 0 : -1
                    if(Arrays.equals(a, b)) {
                        /* Arrays match, but they should not match for triangleOfBinCoeff because BinCoeff cant handle that large numbers */
                        if(pair.getKey() <= 12)
                            return 0;
                        return -1;
                    } else {
                        if(pair.getKey() <= 12)
                            return -1;
                        return 0;
                    }
                }, pair.getKey());
        }, "triangleOfBinCoeff functionality test").requireTest(methodAvail);
    }

    private void registerPascalTests() {
        Test methodAvail = this.registerTest(context -> {
            context.getHelper()
                    .resolveMethod("h3.PascalsTriangle", "pascalsTriangle", Modifier.PUBLIC, Integer[].class, int.class);
        }, "test method availability for pascalsTriangle");


        this.registerTest(context -> {
            final Helpers helpers = context.getHelper();

            Object pt = helpers.createInstance("h3.PascalsTriangle");
            Method m = helpers.resolveMethod(pt, "pascalsTriangle", Modifier.PUBLIC, Integer[].class, int.class);

            for(Map.Entry<Integer, Integer[]> pair : pascTriTestSet.entrySet())
                helpers.executeWithExpect(pt, m, pair.getValue(), (a, b) -> Arrays.equals(a, b) ? 0 : -1, pair.getKey());
        }, "pascalsTriangle functionality test").requireTest(methodAvail);
    }

    private Test registerBinCoeffTests() {
        Test methodAvail = this.registerTest(context -> {
            context.getHelper()
                    .resolveMethod("h3.PascalsTriangle", "binomialCoefficient", Modifier.PUBLIC, int.class, int.class, int.class);
        }, "test method availability for binomialCoefficient");

        return this.registerTest(context -> {
            final Helpers helpers = context.getHelper();

            Object pt = helpers.createInstance("h3.PascalsTriangle");
            Method m = helpers.resolveMethod(pt, "binomialCoefficient", Modifier.PUBLIC, int.class, int.class, int.class);

            Map<Pair<Integer, Integer>, Integer> testSet = new HashMap<>();
            testSet.put(new ImmutablePair<>(0, 0), 1);
            testSet.put(new ImmutablePair<>(1, 1), 1);
            testSet.put(new ImmutablePair<>(10, 5), 252);
            testSet.put(new ImmutablePair<>(10, 2), 45);
            testSet.put(new ImmutablePair<>(5, 2), 10);
            for(Map.Entry<Pair<Integer, Integer>, Integer> pair : testSet.entrySet())
                helpers.executeWithExpect(pt, m, pair.getValue(), Comparator.comparingInt(a -> a), pair.getKey().getLeft(), pair.getKey().getRight());
        }, "binomialCoefficient functionality test").requireTest(methodAvail);
    }

    private void registerFacultyTests() {
        Test methodAvail = this.registerTest(context -> {
            context.getHelper()
                    .resolveMethod("h3.PascalsTriangle", "faculty", Modifier.PUBLIC, int.class, int.class);
        }, "test method availability for faculty");

        this.registerTest(context -> {
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
        }, "faculty functionality test").requireTest(methodAvail);
    }
}
