package com.github.f4b6a3.hibernate;

public class LocalizationColumnDefinitions {
    public static final String DEFINITION = " VARCHAR(5)";
    @Deprecated
    public static final String NON_NULL = " VARCHAR(5) NOT NULL";
    @Deprecated
    public static final String DEFAULT_NULL = " VARCHAR(5) DEFAULT NULL";

    private LocalizationColumnDefinitions() { }
}
