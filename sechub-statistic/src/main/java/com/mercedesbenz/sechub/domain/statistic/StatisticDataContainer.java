package com.mercedesbenz.sechub.domain.statistic;

import static com.mercedesbenz.sechub.sharedkernel.util.Assert.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StatisticDataContainer<T extends StatisticDataType> {

    private Map<T, List<StatisticDataKeyValue>> map = new LinkedHashMap<>();

    /**
     * Adds data for given type
     * 
     * @param type  may not be <code>null</code>
     * @param key   may not be <code>null</code>
     * @param value
     */
    public void add(T type, StatisticDataKey key, long value) {
        add(type, key, BigInteger.valueOf(value));
    }

    /**
     * Adds data for given type
     * 
     * @param type  may not be <code>null</code>
     * @param key   may not be <code>null</code>
     * @param value may not be <code>null</code>
     */
    public void add(T type, StatisticDataKey key, BigInteger value) {
        notNull(type, "Type may not be null!");
        notNull(key, "Key may not be null!");
        notNull(value, "Value may not be null!");

        List<StatisticDataKeyValue> list = map.computeIfAbsent(type, (t) -> new ArrayList<>());
        list.add(new StatisticDataKeyValue(key, value));
    }

    public Set<T> getTypes() {
        return map.keySet();
    }

    /**
     * Resolves a list of key values for given type
     * 
     * @param type defines the statistic data type for which the key values shall be
     *             fetched
     * @return unmodifiable list, never <code>null</code>
     */
    public List<StatisticDataKeyValue> getKeyValues(T type) {
        if (type == null) {
            return Collections.emptyList();
        }
        List<StatisticDataKeyValue> list = map.get(type);
        if (list == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(list);
    }

}
