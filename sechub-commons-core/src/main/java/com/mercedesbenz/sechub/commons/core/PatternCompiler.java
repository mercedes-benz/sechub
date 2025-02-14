package com.mercedesbenz.sechub.commons.core;

import static java.util.Objects.*;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class PatternCompiler {

    /**
     * Compiles regular expression to a pattern
     *
     * @param regExp regular expression
     * @return pattern, never {@link NullPointerException}
     * @throws PatternSyntaxException
     */
    public Pattern compile(String regExp) {
        return Pattern.compile(requireNonNull(regExp, "Regular expression may not be null!"));
    }
}
