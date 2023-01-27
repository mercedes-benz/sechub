package com.mercedesbenz.sechub.domain.statistic.job;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.mercedesbenz.sechub.domain.statistic.AnyTextAsKey;
import com.mercedesbenz.sechub.domain.statistic.StatisticDataKey;

class JobRunStatisticDataTypeTest {

    @Test
    void all_is_accepted_by_files_data_type() {
        assertTrue(JobRunStatisticDataType.FILES.isKeyAccepted(AnalyticStatisticDataKey.ALL));
    }

    @Test
    void any_text_is_not_accepted_by_files_data_type() {
        assertFalse(JobRunStatisticDataType.FILES.isKeyAccepted(AnyTextAsKey.ANY_TEXT));
        assertFalse(JobRunStatisticDataType.FILES.isKeyAccepted(new AnyTextAsKey("other")));
    }

    @Test
    void any_text_is_accepted_by_lang_loc_data_type() {
        assertTrue(JobRunStatisticDataType.LOC_LANG.isKeyAccepted(AnyTextAsKey.ANY_TEXT));
        assertTrue(JobRunStatisticDataType.LOC_LANG.isKeyAccepted(new AnyTextAsKey("java")));
    }

    @EnumSource(UploadJobStatisticDataKeys.class)
    @ParameterizedTest
    void upload_keys_are_not_accepted_by_files_data_type(StatisticDataKey key) {
        assertFalse(JobRunStatisticDataType.FILES.isKeyAccepted(key));
    }

}
