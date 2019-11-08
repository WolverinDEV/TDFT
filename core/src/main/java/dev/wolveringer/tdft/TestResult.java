package dev.wolveringer.tdft;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.io.PrintStream;
import java.text.DecimalFormat;

@AllArgsConstructor
@Getter
@ToString
public class TestResult {
    public static interface PrintTarget {
        void print(String message);
    }

    private final int testUnitsTotal, testUnitsAvailable;
    private final int testsAvailable, testsExecuted, testsSucceeded, testsSkipped;

    public int getFailedTests() { return this.testsExecuted - this.testsSucceeded - this.testsSkipped; }
    public boolean successfully() { return this.getFailedTests() == 0 && this.getTestsSkipped() == 0; }

    private static String formantPer(int a, int b, boolean withOf) {
        float p = (float) (a * 100) / b;
        DecimalFormat decimalFormat = new DecimalFormat("#0.0");
        String app = "(" + decimalFormat.format(p);
        app = StringUtils.leftPad(app, 7, ' ');
        app += "%)";

        String sa = StringUtils.leftPad(a + "", 3, ' ');
        String sb = StringUtils.leftPad(b + "", 3, ' ');
        if(withOf)
            return sa + " of " + sb + app;
        return sa + "       " + app;
    }

    public void print(PrintTarget os) {
        os.print("Test summery:");
        os.print("  Test units executed: " + formantPer(this.getTestUnitsAvailable(), this.getTestUnitsTotal(), true));
        os.print("  Tests executed     : " + formantPer(this.getTestsExecuted(), this.getTestsAvailable(), true));
        os.print("    Succeeded        : " + formantPer(this.getTestsSucceeded(), this.getTestsExecuted(), false));
        os.print("    Failed           : " + formantPer(this.getFailedTests(), this.getTestsExecuted(), false));
        os.print("    Skipped          : " + formantPer(this.getTestsSkipped(), this.getTestsExecuted(), false));
    }
}
