package com.mercedesbenz.sechub.domain.statistic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StatisticDataContainerTest {

    private StatisticDataContainer<StatisticDataType> containerToTest;
    private StatisticDataType mockedType1;
    private StatisticDataType mockedType2;
    private StatisticDataKey mockedKey1;
    private StatisticDataKey mockedKey2;

    @BeforeEach
    void beforeEach() {
        mockedType1 = mock(StatisticDataType.class);
        mockedType2 = mock(StatisticDataType.class);

        mockedKey1 = mock(StatisticDataKey.class);
        mockedKey2 = mock(StatisticDataKey.class);

        containerToTest = new StatisticDataContainer<>();

    }

    @Test
    void nothing_added_no_data_found() {
        assertNoDataFound(null);
        assertNoDataFound(mockedType1);
        assertNoDataFound(mockedType2);
        
        Set<StatisticDataType> types = containerToTest.getTypes();
        assertTrue(types.isEmpty());
    }

    @Test
    void added_data_found() {

        /* execute */
        containerToTest.add(mockedType1, mockedKey1, BigInteger.valueOf(1234));
        containerToTest.add(mockedType1, mockedKey1, BigInteger.valueOf(5234));
        containerToTest.add(mockedType1, mockedKey2, 2L);

        /* test */
        assertNoDataFound(null);
        assertNoDataFound(mockedType2);

        assertDataFound(mockedType1, mockedKey1, BigInteger.valueOf(1234));
        assertDataFound(mockedType1, mockedKey1, BigInteger.valueOf(5234));
        assertDataFound(mockedType1, mockedKey2, BigInteger.valueOf(2));
    }
    
    @Test
    void added_types_found() {
        
        /* execute */
        containerToTest.add(mockedType1, mockedKey1, BigInteger.valueOf(1234));
        containerToTest.add(mockedType1, mockedKey1, BigInteger.valueOf(5234));
        containerToTest.add(mockedType2, mockedKey2, BigInteger.valueOf(2));
        
        /* test */
        Set<StatisticDataType> types = containerToTest.getTypes();

        assertTrue(types.contains(mockedType1));
        assertTrue(types.contains(mockedType2));
        assertEquals(2, types.size());
    }

    private void assertDataFound(StatisticDataType type, StatisticDataKey key, BigInteger value) {
        List<StatisticDataKeyValue> result = containerToTest.getKeyValues(type);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (StatisticDataKeyValue keyValue : result) {
            StatisticDataKey foundKey = keyValue.getKey();
            if (!key.equals(foundKey)) {
                continue;
            }
            BigInteger foundValue = keyValue.getValue();
            if (value.equals(foundValue)) {
                return;
            }
        }
        fail("Not found inside container: type" + type + ", key=" + key + " value=" + value);

    }

    void assertNoDataFound(StatisticDataType type) {

        List<StatisticDataKeyValue> result = containerToTest.getKeyValues(type);
        assertNotNull(result);
        assertTrue(result.isEmpty());

    }

}
