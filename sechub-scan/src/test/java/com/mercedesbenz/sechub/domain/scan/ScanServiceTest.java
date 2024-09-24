// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.task.TaskExecutor;

import com.mercedesbenz.sechub.commons.model.JSONConverterException;
import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.domain.scan.log.ProjectScanLogService;
import com.mercedesbenz.sechub.domain.scan.product.AnalyticsProductExecutionService;
import com.mercedesbenz.sechub.domain.scan.product.CodeScanProductExecutionService;
import com.mercedesbenz.sechub.domain.scan.product.InfrastructureScanProductExecutionService;
import com.mercedesbenz.sechub.domain.scan.product.LicenseScanProductExecutionService;
import com.mercedesbenz.sechub.domain.scan.product.PrepareProductExecutionService;
import com.mercedesbenz.sechub.domain.scan.product.SecretScanProductExecutionService;
import com.mercedesbenz.sechub.domain.scan.product.WebScanProductExecutionService;
import com.mercedesbenz.sechub.domain.scan.project.ScanMockData;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfig;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfigID;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfigService;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectMockDataConfiguration;
import com.mercedesbenz.sechub.domain.scan.report.CreateScanReportService;
import com.mercedesbenz.sechub.domain.scan.report.ScanReport;
import com.mercedesbenz.sechub.sharedkernel.ProgressState;
import com.mercedesbenz.sechub.sharedkernel.ProgressStateFetcher;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.mercedesbenz.sechub.sharedkernel.messaging.AsynchronMessageHandler;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessage;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageService;
import com.mercedesbenz.sechub.sharedkernel.messaging.DomainMessageSynchronousResult;
import com.mercedesbenz.sechub.sharedkernel.messaging.DummyEventInspector;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageDataKeys;
import com.mercedesbenz.sechub.sharedkernel.messaging.MessageID;
import com.mercedesbenz.sechub.sharedkernel.messaging.SynchronMessageHandler;
import com.mercedesbenz.sechub.storage.core.JobStorage;
import com.mercedesbenz.sechub.storage.core.StorageService;

public class ScanServiceTest {

    private static final String TEST_PROJECT_ID1 = "test-project-id1";
    private static final String TRAFFIC_LIGHT = "someColor";
    private static final UUID SECHUB_JOB_UUID = UUID.randomUUID();
    private static final UUID EXECUTION_UUID = UUID.randomUUID();

    private static final String SECHUB_CONFIG_VALID_MINIMUM = "{ \"projectId\" : \"" + TEST_PROJECT_ID1 + "\" }";
    private ScanService serviceToTest;
    private WebScanProductExecutionService webScanProductExecutionService;
    private CodeScanProductExecutionService codeScanProductExecutionService;
    private InfrastructureScanProductExecutionService infrastructureScanProductExecutionService;
    private CreateScanReportService reportService;
    private ScanReport report;
    private StorageService storageService;
    private JobStorage jobStorage;
    private ProjectScanLogService scanLogService;
    private ScanProjectConfigService scanProjectConfigService;
    private ScanJobListener scanJobListener;
    private ScanProgressStateFetcherFactory stateFetcherFactory;
    private LicenseScanProductExecutionService licenseScanProductExecutionService;
    private ProductExecutionServiceContainer productExecutionServiceContainer;
    private AnalyticsProductExecutionService analyticsProductExecutionService;
    private PrepareProductExecutionService prepareProductExecutionService;
    private SecretScanProductExecutionService secretScanProductExecutionService;
    private ProgressState progressState;

    private static final SecHubConfiguration SECHUB_CONFIG = new SecHubConfiguration();

    @Before
    public void before() throws Exception {
        storageService = mock(StorageService.class);
        jobStorage = mock(JobStorage.class);
        scanProjectConfigService = mock(ScanProjectConfigService.class);
        scanJobListener = mock(ScanJobListener.class);
        stateFetcherFactory = mock(ScanProgressStateFetcherFactory.class);
        ProgressStateFetcher progressStateFetcher = mock(ProgressStateFetcher.class);
        progressState = mock(ProgressState.class);

        when(storageService.getJobStorage(any(), any())).thenReturn(jobStorage);
        when(stateFetcherFactory.createProgressStateFetcher(any())).thenReturn(progressStateFetcher);
        when(progressStateFetcher.fetchProgressState()).thenReturn(progressState);

        webScanProductExecutionService = mock(WebScanProductExecutionService.class);
        codeScanProductExecutionService = mock(CodeScanProductExecutionService.class);
        infrastructureScanProductExecutionService = mock(InfrastructureScanProductExecutionService.class);
        licenseScanProductExecutionService = mock(LicenseScanProductExecutionService.class);
        analyticsProductExecutionService = mock(AnalyticsProductExecutionService.class);
        prepareProductExecutionService = mock(PrepareProductExecutionService.class);
        secretScanProductExecutionService = mock(SecretScanProductExecutionService.class);

        scanLogService = mock(ProjectScanLogService.class);

        reportService = mock(CreateScanReportService.class);
        report = mock(ScanReport.class);
        when(report.getTrafficLightAsString()).thenReturn(TRAFFIC_LIGHT);
        when(reportService.createReport(any())).thenReturn(report);

        serviceToTest = new ScanService();
        productExecutionServiceContainer = mock(ProductExecutionServiceContainer.class);
        serviceToTest.productExecutionServiceContainer = productExecutionServiceContainer;

        when(productExecutionServiceContainer.getWebScanProductExecutionService()).thenReturn(webScanProductExecutionService);
        when(productExecutionServiceContainer.getInfraScanProductExecutionService()).thenReturn(infrastructureScanProductExecutionService);
        when(productExecutionServiceContainer.getCodeScanProductExecutionService()).thenReturn(codeScanProductExecutionService);
        when(productExecutionServiceContainer.getLicenseScanProductExecutionService()).thenReturn(licenseScanProductExecutionService);
        when(productExecutionServiceContainer.getAnalyticsProductExecutionService()).thenReturn(analyticsProductExecutionService);
        when(productExecutionServiceContainer.getPrepareProductExecutionService()).thenReturn(prepareProductExecutionService);
        when(productExecutionServiceContainer.getSecretScanProductExecutionService()).thenReturn(secretScanProductExecutionService);

        serviceToTest.reportService = reportService;
        serviceToTest.storageService = storageService;
        serviceToTest.scanLogService = scanLogService;
        serviceToTest.scanProjectConfigService = scanProjectConfigService;
        serviceToTest.scanJobListener = scanJobListener;
        serviceToTest.monitorFactory = stateFetcherFactory;
    }

    @Test
    public void when_no_exception_is_thrown_result_has_not_failed() throws Exception {

        /* execute */
        DomainMessageSynchronousResult result = serviceToTest.receiveSynchronMessage(prepareValidRequest());

        /* test */
        assertFalse(result.hasFailed());
    }

    @Test
    public void scanservice_does_execute_webscan_execution_service() throws Exception {

        /* execute */
        serviceToTest.receiveSynchronMessage(prepareValidRequest());

        /* test */
        verify(webScanProductExecutionService).executeProductsAndStoreResults(any());
    }

    @Test
    public void scanservice_does_execute_infrascan_execution_service() throws Exception {

        /* execute */
        serviceToTest.receiveSynchronMessage(prepareValidRequest());

        /* test */
        verify(infrastructureScanProductExecutionService).executeProductsAndStoreResults(any());
    }

    @Test
    public void scanservice_does_execute_codescan_execution_service() throws Exception {

        /* execute */
        serviceToTest.receiveSynchronMessage(prepareValidRequest());

        /* test */
        verify(codeScanProductExecutionService).executeProductsAndStoreResults(any());
    }

    @Test
    public void scanservice_does_execute_prepare_execution_service() throws Exception {

        /* execute */
        serviceToTest.receiveSynchronMessage(prepareValidRequest());

        /* test */
        verify(prepareProductExecutionService).executeProductsAndStoreResults(any());
    }

    @Test
    public void scanservice_does_cleanup_storage_of_job__when_not_failed() throws Exception {

        /* execute */
        DomainMessageSynchronousResult result = serviceToTest.receiveSynchronMessage(prepareValidRequest());

        /* test */
        verify(jobStorage).deleteAll();
        assertFalse(result.hasFailed());
    }

    /**
     * Here we test that on failure the storage is ALSO cleaned. Why? Because in
     * future there should be the possiblity for a retry mechanism, but currently
     * there is none. When this is implemented we must change the test so it will
     * check there is NO cleaning. But having no retry mechanism implemented, we
     * expect the cleanup process done even when failing.
     *
     * @throws Exception
     */
    @Test
    public void scanservice_does_cleanup_storage_of_job__when_HAS_failed() throws Exception {

        /* prepare */
        DomainMessage request = prepareValidRequest();
        doThrow(new SecHubExecutionException("ups...", new RuntimeException("Wanted test failure"))).when(webScanProductExecutionService)
                .executeProductsAndStoreResults(any());

        /* execute */
        DomainMessageSynchronousResult result = serviceToTest.receiveSynchronMessage(request);

        /* test */
        assertTrue(result.hasFailed());
        verify(jobStorage/* when retry implemented:,never() */).deleteAll();

    }

    @Test
    public void scanservice_does_execute_report_service() throws Exception {

        /* execute */
        serviceToTest.receiveSynchronMessage(prepareValidRequest());

        /* test */
        verify(reportService).createReport(any());
    }

    @Test
    public void scanservice_set_result_traficlight_as_from_report() throws Exception {

        /* execute */
        DomainMessageSynchronousResult result = serviceToTest.receiveSynchronMessage(prepareValidRequest());

        /* test */
        assertEquals(TRAFFIC_LIGHT, result.get(MessageDataKeys.REPORT_TRAFFIC_LIGHT));
    }

    @Test
    public void scanservice_does_NOT_execute_reportservice_when_webscan_throws_sechubexception() throws Exception {

        /* prepare */
        DomainMessage request = prepareValidRequest();
        doThrow(new SecHubExecutionException("ups...", new RuntimeException("Wanted test failure"))).when(webScanProductExecutionService)
                .executeProductsAndStoreResults(any());

        /* execute */
        DomainMessageSynchronousResult result = serviceToTest.receiveSynchronMessage(request);

        /* test */
        assertTrue(result.hasFailed());

        verify(webScanProductExecutionService).executeProductsAndStoreResults(any());
        verify(reportService, never()).createReport(any());

    }

    @Test
    public void event_handling_works_as_expected_and_SCAN_DONE_is_returned_as_resulting_message_id() {
        /* prepare */
        DomainMessage request = prepareValidRequest();

        /* execute */
        DomainMessageSynchronousResult result = simulateEventSend(request, serviceToTest);

        /* test */
        assertEquals(MessageID.SCAN_DONE, result.getMessageId());
    }

    @Test
    public void event_handling_FAILED_when_configuration_is_not_set() {
        /* prepare */
        DomainMessage request = prepareValidRequest();
        request.set(MessageDataKeys.SECHUB_CONFIG, null);

        /* execute */
        DomainMessageSynchronousResult result = simulateEventSend(request, serviceToTest);

        /* test */
        assertEquals(MessageID.SCAN_FAILED, result.getMessageId());
    }

    @Test
    public void event_handling_FAILED_when_configuration_is_set_but_contains_no_projectId() {
        /* prepare */
        SecHubConfiguration configNoProjectId = prepareValidConfiguration();
        configNoProjectId.setProjectId(null);
        DomainMessage request = prepareRequest(configNoProjectId);

        /* execute */
        DomainMessageSynchronousResult result = simulateEventSend(request, serviceToTest);

        /* test */
        assertEquals(MessageID.SCAN_FAILED, result.getMessageId());
    }

    @Test
    public void scan_service_fetches_configuration_without_accesscheck() throws Exception {
        /* prepare */
        SecHubConfiguration configNoProjectId = prepareValidConfiguration();
        DomainMessage request = prepareRequest(configNoProjectId);

        /* execute */
        simulateEventSend(request, serviceToTest);

        /* test */
        verify(scanProjectConfigService).get(TEST_PROJECT_ID1, ScanProjectConfigID.MOCK_CONFIGURATION, false);
    }

    @Test
    public void scan_service_fetches_mock_configuration_and_puts_mock_project_configuration_complete_in_execution_context() throws Exception {
        /* prepare */
        SecHubConfiguration configNoProjectId = prepareValidConfiguration();
        DomainMessage request = prepareRequest(configNoProjectId);

        ScanProjectMockDataConfiguration projectMockDataConfig = new ScanProjectMockDataConfiguration();
        projectMockDataConfig.setCodeScan(new ScanMockData(TrafficLight.YELLOW));

        ScanProjectConfig projectConfig = new ScanProjectConfig(ScanProjectConfigID.MOCK_CONFIGURATION, TEST_PROJECT_ID1);
        projectConfig.setData(projectMockDataConfig.toJSON());

        when(scanProjectConfigService.get("test-project-id1", ScanProjectConfigID.MOCK_CONFIGURATION, false)).thenReturn(projectConfig);

        /* execute */
        simulateEventSend(request, serviceToTest);

        /* test */
        ArgumentCaptor<SecHubExecutionContext> contextCaptor = ArgumentCaptor.forClass(SecHubExecutionContext.class);
        verify(codeScanProductExecutionService).executeProductsAndStoreResults(contextCaptor.capture());
        SecHubExecutionContext context = contextCaptor.getValue();
        assertEquals(projectMockDataConfig, context.getData(ScanKey.PROJECT_MOCKDATA_CONFIGURATION));
    }

    private DomainMessage prepareValidRequest() {

        SecHubConfiguration configMin = prepareValidConfiguration();

        return prepareRequest(configMin);
    }

    private DomainMessage prepareRequest(SecHubConfiguration configMin) {

        DomainMessage request = new DomainMessage(MessageID.START_SCAN);
        request.set(MessageDataKeys.SECHUB_JOB_UUID, SECHUB_JOB_UUID);
        request.set(MessageDataKeys.SECHUB_EXECUTION_UUID, EXECUTION_UUID);
        request.set(MessageDataKeys.SECHUB_CONFIG, configMin);

        return request;
    }

    private SecHubConfiguration prepareValidConfiguration() {
        SecHubConfiguration configMin;
        try {
            configMin = SECHUB_CONFIG.fromJSON(SECHUB_CONFIG_VALID_MINIMUM);
        } catch (JSONConverterException e) {
            throw new IllegalStateException("testcase invalid!");
        }
        return configMin;
    }

    private DomainMessageSynchronousResult simulateEventSend(DomainMessage request, SynchronMessageHandler handler) {
        List<AsynchronMessageHandler> injectedAsynchronousHandlers = new ArrayList<>();
        List<SynchronMessageHandler> injectedSynchronousHandlers = new ArrayList<>();
        injectedSynchronousHandlers.add(handler);

        FakeDomainMessageService fakeDomainMessageService = new FakeDomainMessageService(injectedSynchronousHandlers, injectedAsynchronousHandlers);
        return fakeDomainMessageService.sendSynchron(request);
    }

    private class FakeDomainMessageService extends DomainMessageService {

        public FakeDomainMessageService(List<SynchronMessageHandler> injectedSynchronousHandlers, List<AsynchronMessageHandler> injectedAsynchronousHandlers) {
            super(injectedSynchronousHandlers, injectedAsynchronousHandlers);
            this.taskExecutor = new TestTaskExecutor();
            this.eventInspector = new DummyEventInspector();
        }

    }

    private class TestTaskExecutor implements TaskExecutor {

        @Override
        public void execute(Runnable task) {
            task.run();
        }

    }

}
