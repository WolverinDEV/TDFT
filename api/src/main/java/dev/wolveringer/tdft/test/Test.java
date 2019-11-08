package dev.wolveringer.tdft.test;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Getter
public class Test {
    private final TestSuite suite;
    private final String id;
    private Set<String> requiredTests = new HashSet<>();
    @Setter
    private TestState state = TestState.PENDING;

    public Test requireTest(String id) {
        this.requiredTests.add(id);
        return this;
    }

    public Test requireTest(Test... other) {
        for(Test test : other)
            this.requireTest(test.id);
        return this;
    }
}
