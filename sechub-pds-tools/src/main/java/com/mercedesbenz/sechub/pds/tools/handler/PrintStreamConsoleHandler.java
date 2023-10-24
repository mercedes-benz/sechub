// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.tools.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import com.beust.jcommander.internal.Console;

public class PrintStreamConsoleHandler implements ConsoleHandler, Console {

    private PrintStream outputStream;
    private PrintStream errorStream;

    public PrintStreamConsoleHandler() {
        this(System.out, System.err);
    }

    public PrintStreamConsoleHandler(PrintStream outputStream, PrintStream errorStream) {
        this.outputStream = outputStream;
        this.errorStream = errorStream;
    }

    @Override
    public void output(String text) {
        println(text);
    }

    @Override
    public void error(String message, Throwable t) {
        errorStream.println(message);
        if (t != null) {
            t.printStackTrace(errorStream);
        }
    }

    @Override
    public void print(String msg) {
        outputStream.print(msg);
    }

    @Override
    public void println(String msg) {
        outputStream.println(msg);
    }

    @Override
    public char[] readPassword(boolean echoInput) {
        try {
            InputStreamReader isr = new InputStreamReader(System.in);
            BufferedReader in = new BufferedReader(isr);
            String result = in.readLine();
            return result.toCharArray();
        } catch (IOException e) {
            throw new IllegalStateException("Was not able to read password", e);
        }
    }

}
