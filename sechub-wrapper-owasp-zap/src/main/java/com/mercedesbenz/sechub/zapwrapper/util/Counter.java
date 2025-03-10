package com.mercedesbenz.sechub.zapwrapper.util;

public class Counter {
    private int count;

    public Counter() {
        count = 0;
    }

    public void increment() {
        count++;
    }

    public int getCount() {
        return count;
    }
}
