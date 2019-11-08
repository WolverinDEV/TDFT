package dev.wolveringer.tdft;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class AbstractTestLogger implements TestLogger {
    @NonNull
    protected final TestExecutor handle;
    protected String context = "";
    private List<String> contextStack = new ArrayList<>();

    private void generateContext() {
        if(this.contextStack.isEmpty())
            this.context = "";
        else
            this.context = this.contextStack.stream().collect(Collectors.joining("::", "[", "]"));
    }

    @Override
    public void pushContext(String name) {
        this.contextStack.add(name);
        this.generateContext();
    }

    @Override
    public void popContext(String expectedName) {
        Validate.isTrue(!this.context.isEmpty(), "Context stack is empty");
        String givenName = this.contextStack.remove(this.contextStack.size() - 1);
        this.generateContext();

        Validate.isTrue(givenName.equals(expectedName), "Expected context name is not equals to the given name (" + givenName +" != " + expectedName + ")");
    }
}
