package dev.wolveringer.tdft;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class TestResult {
    private final int testsRegistered;
    private final int testsTotal;
    private final int testsExecuted;
    private final int testsPassed;

    public int getFailedTests() { return this.testsExecuted - this.testsPassed; }
    public int getSkippedTests() { return this.testsTotal - this.testsExecuted; }

    public boolean successfully() { return this.getFailedTests() == 0; }
}
