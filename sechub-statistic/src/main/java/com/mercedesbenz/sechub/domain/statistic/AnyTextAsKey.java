package com.mercedesbenz.sechub.domain.statistic;

public class AnyTextAsKey implements StatisticDataKey {

    public static final AnyTextAsKey ANY_TEXT = new AnyTextAsKey(null);

    private String text;

    public AnyTextAsKey(String text) {
        this.text = text;
    }

    @Override
    public String getKeyValue() {
        return text;
    }

}