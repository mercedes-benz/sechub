// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.task.TaskExecutor;

import com.daimler.sechub.domain.scan.log.ProjectScanLogService;
import com.daimler.sechub.domain.scan.product.CodeScanProductExecutionService;
import com.daimler.sechub.domain.scan.product.InfrastructureScanProductExecutionService;
import com.daimler.sechub.domain.scan.product.WebScanProductExecutionService;
import com.daimler.sechub.domain.scan.report.CreateScanReportService;
import com.daimler.sechub.domain.scan.report.ScanReport;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionException;
import com.daimler.sechub.sharedkernel.messaging.AsynchronMessageHandler;
import com.daimler.sechub.sharedkernel.messaging.DomainMessage;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageSynchronousResult;
import com.daimler.sechub.sharedkernel.messaging.MessageDataKeys;
import com.daimler.sechub.sharedkernel.messaging.MessageID;
import com.daimler.sechub.sharedkernel.messaging.SynchronMessageHandler;
import com.daimler.sechub.sharedkernel.storage.StorageService;
import com.daimler.sechub.sharedkernel.util.JSONConverterException;
import com.daimler.sechub.storage.core.JobStorage;

public class ScanServiceTest {

	private static final String TRAFFIC_LIGHT = "someColor";
	private static final java.util.UUID UUID = java.util.UUID.randomUUID();
	private static final String SECHUB_CONFIG_VALID_MINIMUM = "{}";
	private ScanService serviceToTest;
	private WebScanProductExecutionService webScanProductExecutionService;
	private CodeScanProductExecutionService codeScanProductExecutionService;
	private InfrastructureScanProductExecutionService infrastructureScanProductExecutionService;
	private CreateScanReportService reportService;
	private ScanReport report;
	private StorageService storageService;
	private JobStorage jobStorage;
	private ProjectScanLogService scanLogService;
	private static final SecHubConfiguration SECHUB_CONFIG = new SecHubConfiguration();

	@Before
	public void before() throws Exception {
		storageService = mock(StorageService.class);
		jobStorage = mock(JobStorage.class);

		when(storageService.getJobStorage(any(), any())).thenReturn(jobStorage);

		webScanProductExecutionService = mock(WebScanProductExecutionService.class);
		codeScanProductExecutionService = mock(CodeScanProductExecutionService.class);
		infrastructureScanProductExecutionService = mock(InfrastructureScanProductExecutionService.class);
		scanLogService = mock(ProjectScanLogService.class);

		reportService = mock(CreateScanReportService.class);
		report = mock(ScanReport.class);
		when(report.getTrafficLightAsString()).thenReturn(TRAFFIC_LIGHT);
		when(reportService.createReport(any())).thenReturn(report);

		serviceToTest = new ScanService();
		serviceToTest.webScanProductExecutionService = webScanProductExecutionService;
		serviceToTest.infraScanProductExecutionService = infrastructureScanProductExecutionService;
		serviceToTest.codeScanProductExecutionService = codeScanProductExecutionService;
		serviceToTest.reportService = reportService;
		serviceToTest.storageService = storageService;
		serviceToTest.scanLogService = scanLogService;
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
		doThrow(new SecHubExecutionException("ups...", new RuntimeException())).when(webScanProductExecutionService).executeProductsAndStoreResults(any());

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
		doThrow(new SecHubExecutionException("ups...", new RuntimeException())).when(webScanProductExecutionService).executeProductsAndStoreResults(any());

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
		DomainMessage request = new DomainMessage(MessageID.START_SCAN);

		/* execute */
		DomainMessageSynchronousResult result = simulateEventSend(request, serviceToTest);

		/* test */
		assertEquals(MessageID.SCAN_DONE, result.getMessageId());
	}

	private DomainMessage prepareValidRequest() {

		SecHubConfiguration configMin;
		try {
			configMin = SECHUB_CONFIG.fromJSON(SECHUB_CONFIG_VALID_MINIMUM);
		} catch (JSONConverterException e) {
			throw new IllegalStateException("testcase invalid!");
		}

		DomainMessage request = new DomainMessage(MessageID.START_SCAN);
		request.set(MessageDataKeys.SECHUB_UUID, UUID);
		request.set(MessageDataKeys.SECHUB_CONFIG, configMin);

		return request;
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
		}

	}

	private class TestTaskExecutor implements TaskExecutor {

		@Override
		public void execute(Runnable task) {
			task.run();
		}

	}

}
