package dev.wolveringer.tdft.h4;

import dev.wolveringer.tdft.TestContext;
import dev.wolveringer.tdft.source.TestSource;
import dev.wolveringer.tdft.test.Test;
import dev.wolveringer.tdft.test.TestUnit;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class Matrix extends TestUnit {
    public Matrix() {
        super("H1 MatrixOperation");
    }

    @Override
    public boolean executable(TestSource source) {
        return source.getProjectName().toLowerCase().startsWith("h04_");
    }

    @Override
    protected void registerTests(TestContext ctx) {
        Test functionAvail = this.registerTest(context -> {
            context.getHelper().resolveMethod("h4.MatrixOperation", "matrixCrossMultiplication", Modifier.PUBLIC, int[][].class, int[][].class);
        }, "test for function matrixCrossMultiplication");

        /* Credits for the test cases going to Georg Wurst */
        this.registerTest(context -> {
            int[][] input = null;
            int[][] output = {{0}};

            Object mo = context.getHelper().createInstance("h4.MatrixOperation");
            Method m = context.getHelper().resolveMethod("h4.MatrixOperation", "matrixCrossMultiplication", Modifier.PUBLIC, int[][].class, int[][].class);
            context.getHelper().executeWithExpect(mo, m, output, Arrays::deepEquals, (Object) input);
        }, "test 01").requireTest(functionAvail);

        this.registerTest(context -> {
            int[][] input = {{1}};
            int[][] output = {{36}};

            Object mo = context.getHelper().createInstance("h4.MatrixOperation");
            context.getHelper().executeWithExpect(mo, "matrixCrossMultiplication", Modifier.PUBLIC, output, Arrays::deepEquals, (Object) input);
        }, "test 02").requireTest(functionAvail);

        this.registerTest(context -> {
            int[][] input = {
                    {1,1,1},
                    {1,1,1},
                    {1,1,1}
            };
            int[][] output = {
                    {-6, -2, -6},
                    {3, 1, 3},
                    {-6, -2, -6}
            };

            Object mo = context.getHelper().createInstance("h4.MatrixOperation");
            context.getHelper().executeWithExpect(mo, "matrixCrossMultiplication", Modifier.PUBLIC, output, Arrays::deepEquals, (Object) input);
        }, "test 03").requireTest(functionAvail);

        this.registerTest(context -> {
            int[][] input = {
                    {1,1,1,1,1,1},
                    {1,1,1,1,1,1},
                    {1,1,1,1,1,1},
                    {1,1,1,1,1,1},
                    {1,1,1,1,1,1}
            };
            int[][] output = {
                    {-6, -2, -2, -2, -2, -6},
                    {3, 1, 1, 1, 1, 3},
                    {3, 1, 1, 1, 1, 3},
                    {3, 1, 1, 1, 1, 3},
                    {-6, -2, -2, -2, -2, -6}
            };

            Object mo = context.getHelper().createInstance("h4.MatrixOperation");
            context.getHelper().executeWithExpect(mo, "matrixCrossMultiplication", Modifier.PUBLIC, output, Arrays::deepEquals, (Object) input);
        }, "test 04").requireTest(functionAvail);

        this.registerTest(context -> {
            int[][] input = {
                    {1,2,3,4},
                    {5,6,7,8},
                    {9,10,11,12}
            };
            int[][] output = {
                    {-60, -72, -336, -576},
                    {810, 4200, 11088, 8064},
                    {-2700, -11880, -18480, -6336}
            };

            Object mo = context.getHelper().createInstance("h4.MatrixOperation");
            context.getHelper().executeWithExpect(mo, "matrixCrossMultiplication", Modifier.PUBLIC, output, Arrays::deepEquals, (Object) input);
        }, "test 05").requireTest(functionAvail);

        this.registerTest(context -> {
            int[][] input = {
                    {-2,1,9,-3},
                    {-11,0,11,2},
                    {3,1,1, -100}
            };
            int[][] output = {
                    {-132, 0, 594, 324},
                    {0, 0, 0, 19800},
                    {198, 0, 2200, 1200}
            };

            Object mo = context.getHelper().createInstance("h4.MatrixOperation");
            context.getHelper().executeWithExpect(mo, "matrixCrossMultiplication", Modifier.PUBLIC, output, Arrays::deepEquals, (Object) input);
        }, "test 06").requireTest(functionAvail);

        this.registerTest(context -> {
            int[][] input = {{1,1}};
            int[][] output = {{12, 12}};

            Object mo = context.getHelper().createInstance("h4.MatrixOperation");
            context.getHelper().executeWithExpect(mo, "matrixCrossMultiplication", Modifier.PUBLIC, output, Arrays::deepEquals, (Object) input);
        }, "test 07").requireTest(functionAvail);
    }
}
