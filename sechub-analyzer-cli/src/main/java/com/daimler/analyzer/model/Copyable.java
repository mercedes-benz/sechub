package com.daimler.analyzer.model;

public interface Copyable<T> {
    public T deepClone();
}
