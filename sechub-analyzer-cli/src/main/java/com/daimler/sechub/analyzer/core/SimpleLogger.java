package com.daimler.sechub.analyzer.core;

public class SimpleLogger {
    // TODO: hack to avoid a logger
    public static void log(String message, boolean debug) {
        if (debug) {
            System.out.println(message);
        }
    }
}
