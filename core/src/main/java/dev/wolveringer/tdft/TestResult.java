package dev.wolveringer.tdft;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class TestResult {
    private final int testUnitsTotal, testUnitsAvailable;
    private final int testsAvailable, testsExecuted, testsSucceeded, testsSkipped;

    public int getFailedTests() { return this.testsExecuted - this.testsSucceeded - this.testsSkipped; }
    public boolean successfully() { return this.getFailedTests() == 0 && this.getTestsSkipped() == 0; }
}
