package dev.wolveringer.tdft;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestOptions {
    /**
     * Enable/disable the printing of a full stack trace on an exception which has been thrown within a test suite
     */
    private boolean fullStackTrace = false;

    /**
     * Exit/abort all other tests as soon one test fails
     */
    private boolean exitOnFailure = false;
}
