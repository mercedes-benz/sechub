package com.mercedesbenz.sechub.domain.statistic;

/**
 * SecHub has a very generic data model for statistic data. Often it is very
 * good to restrict statistic data types inside code to some accepted keys only.
 * This keep data structure clear and make it easier to collect them by SQL
 * queries. Also it is always clear inside code which type can contain which
 * keys. But sometimes it is necessary to accept any key - e.g. when the key
 * represents a scanned language and we may not constrain this to explicit
 * values. So we need a way to define this as well and it must be still clear
 * inside code, that in this situation there is no restriction and that every
 * text is accepted as key.<br>
 * <br>
 * To provide types which accept any text as key, just define them with
 * referencing {@link #ANY_TEXT}.
 *
 * @author Albert Tregnaghi
 *
 */
public class AnyTextAsKey implements StatisticDataKey {

    /**
     * Use this reference key as accepted key inside statistic data types which
     * wants to accept any text as key. When this is referenced as accepted key, the
     * type implementation must accept any instance of {@link AnyTextAsKey}.<br>
     * <b>Attention:</b>Never uses this as a direct key to store data! Text is
     * <code>null</code> here and this is not accepted. The static field is only for
     * definition inside types!
     */
    public static final AnyTextAsKey ANY_TEXT = new AnyTextAsKey();

    private String text;

    private AnyTextAsKey() {
        /* keep this private because only reference with text being null is allowed */
    }

    /**
     * Creates a key which is represented by given text
     *
     * @param text may not be <code>null</code>
     */
    public AnyTextAsKey(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Text may not be null!");
        }
        this.text = text;
    }

    @Override
    public String getKeyValue() {
        return text;
    }

    @Override
    public String toString() {
        return "AnyTextAsKey:" + text;
    }

}