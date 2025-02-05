// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.domain.scan.ScanAssertService;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfig.ScanProjectConfigCompositeKey;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

public class ScanProjectConfigServiceTest {

    private ScanProjectConfigService serviceToTest;

    @BeforeEach
    void beforeEach() {
        serviceToTest = new ScanProjectConfigService();

        serviceToTest.repository = mock(ScanProjectConfigRepository.class);
        serviceToTest.scanAssertService = mock(ScanAssertService.class);
        serviceToTest.userInputAssertion = mock(UserInputAssertion.class);

        ScanProjectConfigCompositeKey key = new ScanProjectConfigCompositeKey(ScanProjectConfigID.MOCK_CONFIGURATION, "project-id1");
        ScanProjectConfig configResult = new ScanProjectConfig(key);
        configResult.setData("configData");

        Optional<ScanProjectConfig> config = Optional.ofNullable(configResult);
        when(serviceToTest.repository.findById(eq(key))).thenReturn(config);

    }

    @Test
    void ensures_projectid_valid() {
        /* execute */
        serviceToTest.get("project-id1", ScanProjectConfigID.MOCK_CONFIGURATION);

        /* test */
        verify(serviceToTest.userInputAssertion).assertIsValidProjectId(eq("project-id1"));
    }

    @Test
    void ensures_user_has_access_without_check_access_param() {
        /* execute */
        serviceToTest.get("project-id1", ScanProjectConfigID.MOCK_CONFIGURATION);

        /* test */
        verify(serviceToTest.scanAssertService).assertUserHasAccessToProject("project-id1");
    }

    @Test
    void ensures_user_has_access_with_param_true() {
        /* execute */
        serviceToTest.get("project-id1", ScanProjectConfigID.MOCK_CONFIGURATION, true);

        /* test */
        verify(serviceToTest.scanAssertService).assertUserHasAccessToProject("project-id1");
    }

    @Test
    void does_NOT_ensures_user_has_access_with_param_false() {
        /* execute */
        serviceToTest.get("project-id1", ScanProjectConfigID.MOCK_CONFIGURATION, false);

        /* test */
        verify(serviceToTest.scanAssertService, never()).assertUserHasAccessToProject("project-id1");
    }

    @Test
    void fetches_data_from_repository() {
        /* execute */
        ScanProjectConfig result = serviceToTest.get("project-id1", ScanProjectConfigID.MOCK_CONFIGURATION);

        /* test */
        assertThat(result).isNotNull();
        assertThat(result.getData()).isEqualTo("configData");
    }

    @Test
    void fetches_null_when_no_config_found() {
        /* execute */
        ScanProjectConfig result = serviceToTest.get("project-id2", ScanProjectConfigID.MOCK_CONFIGURATION);

        /* test */
        assertThat(result).isNull();
    }

    @ParameterizedTest
    @EnumSource(ScanProjectConfigID.class)
    void findAllData(ScanProjectConfigID configId) {
        /* prepare */
        List<String> dataList = List.of("data1", "data2");
        when(serviceToTest.repository.findAllDataForConfigId(configId.getId())).thenReturn(dataList);

        /* execute */
        List<String> result = serviceToTest.findAllData(configId);

        /* test */
        assertThat(result).contains("data1", "data2");
        verify(serviceToTest.repository).findAllDataForConfigId(configId.getId());
    }

    @Test
    void findAllProjectsWhereConfigurationHasGivenData() {
        /* prepare */
        Set<String> projectIds = Set.of("p1", "p2");
        when(serviceToTest.repository.findAllProjectsWhereConfigurationHasGivenData(anySet(), eq("data1"))).thenReturn(projectIds);

        /* execute */
        Set<String> result = serviceToTest.findAllProjectsWhereConfigurationHasGivenData(Set.of("config1", "config2"), "data1");

        /* test */
        assertThat(result).contains("p1", "p2");
        ArgumentCaptor<Set<String>> captor = ArgumentCaptor.captor();
        verify(serviceToTest.repository).findAllProjectsWhereConfigurationHasGivenData(captor.capture(), eq("data1"));
        Set<String> setByArgument = captor.getValue();
        assertThat(setByArgument).contains("config1", "config2").hasSize(2);
    }

}
