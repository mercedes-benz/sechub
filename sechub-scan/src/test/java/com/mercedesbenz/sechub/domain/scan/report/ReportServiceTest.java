// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.report;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.mercedesbenz.sechub.commons.model.SecHubReportModel;
import com.mercedesbenz.sechub.commons.model.SecHubResult;
import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.commons.model.TrafficLightCalculator;
import com.mercedesbenz.sechub.domain.scan.ReportTransformationResult;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionContext;
import com.mercedesbenz.sechub.domain.scan.SecHubReportProductTransformerService;
import com.mercedesbenz.sechub.domain.scan.product.ReportProductExecutionService;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;

public class ReportServiceTest {

    private CreateScanReportService serviceToTest;
    private ReportProductExecutionService reportProductExecutionService;
    private SecHubReportProductTransformerService secHubResultService;
    private TrafficLightCalculator trafficLightCalculator;
    private SecHubExecutionContext context;
    private ReportTransformationResult reportTransformationResult;
    private ScanReportRepository reportRepository;
    private UUID secHubJobUUID;
    private SecHubConfiguration configuration;
    private ScanReportTransactionService scanReportTransactionService;
    private SecHubResult sechubResult;

    @BeforeEach
    void before() throws Exception {
        serviceToTest = new CreateScanReportService();

        secHubJobUUID = UUID.randomUUID();
        context = mock(SecHubExecutionContext.class);
        configuration = mock(SecHubConfiguration.class);
        when(context.getConfiguration()).thenReturn(configuration);
        when(context.getSechubJobUUID()).thenReturn(secHubJobUUID);
        when(configuration.getProjectId()).thenReturn("project1");

        reportRepository = mock(ScanReportRepository.class);
        /* just return report as given to save method... */
        when(reportRepository.save(any(ScanReport.class))).thenAnswer(new Answer<ScanReport>() {

            @Override
            public ScanReport answer(InvocationOnMock invocation) throws Throwable {
                return (ScanReport) invocation.getArguments()[0];
            }
        });
        scanReportTransactionService = mock(ScanReportTransactionService.class);
        reportProductExecutionService = mock(ReportProductExecutionService.class);

        reportTransformationResult = mock(ReportTransformationResult.class);
        sechubResult = mock(SecHubResult.class);
        SecHubReportModel model= mock();
        when(reportTransformationResult.getModel()).thenReturn(model);
        when(model.getResult()).thenReturn(sechubResult);
        secHubResultService = mock(SecHubReportProductTransformerService.class);
        when(secHubResultService.createResult(context)).thenReturn(reportTransformationResult);

        trafficLightCalculator = mock(TrafficLightCalculator.class);

        serviceToTest.reportProductExecutionService = reportProductExecutionService;
        serviceToTest.reportTransformerService = secHubResultService;
        serviceToTest.trafficLightCalculator = trafficLightCalculator;
        serviceToTest.reportRepository = reportRepository;
        serviceToTest.scanReportTransactionService = scanReportTransactionService;

    }

    @Test
    void createReport_returns_not_null() throws Exception {

        /* execute */
        ScanReport report = serviceToTest.createReport(context);

        /* test */
        assertNotNull(report);

    }

    @ParameterizedTest
    @EnumSource(TrafficLight.class)
    void createReport_set_report_traffic_light_defined_by_trafficlight_calculator_when_at_least_one_productresult(TrafficLight trafficLight) throws Exception {
        /* prepare */
        when(trafficLightCalculator.calculateTrafficLight(sechubResult)).thenReturn(trafficLight);
        when(reportTransformationResult.isAtLeastOneRealProductResultContained()).thenReturn(true);

        /* execute */
        ScanReport report = serviceToTest.createReport(context);

        /* test */
        assertNotNull(report);
        assertEquals(trafficLight.name(), report.getTrafficLightAsString());

    }

    @Test
    void createReport_set_sechub_jobuuid_to_returned_report() throws Exception {
        /* execute */
        ScanReport report = serviceToTest.createReport(context);

        /* test */
        assertNotNull(report);
        assertEquals(secHubJobUUID, report.getSecHubJobUUID());

    }

    @Test
    void createReport_calls_sechub_result_service() throws Exception {

        /* execute */
        serviceToTest.createReport(context);

        /* test */
        verify(secHubResultService).createResult(context);
    }

    @Test
    void createReport_calls_execution_service() throws Exception {

        /* execute */
        serviceToTest.createReport(context);

        /* test */
        verify(reportProductExecutionService).executeProductsAndStoreResults(context);
    }

    @Test
    void createReport_saves_created_report_by_repository() throws Exception {

        /* execute */
        serviceToTest.createReport(context);

        /* test */
        verify(reportRepository).save(any(ScanReport.class));
    }

    @Test
    void createReport_returns_saved_report_by_repository() throws Exception {

        /* execute */
        serviceToTest.createReport(context);

        /* test */
        verify(reportRepository).save(any(ScanReport.class));
    }

    @Test
    void createReport_calls_NOT_trafficlight_calculator_when_result_has_no_real_product_results() throws Exception {
        /* prepare */
        when(reportTransformationResult.isAtLeastOneRealProductResultContained()).thenReturn(false);

        /* execute */
        ScanReport result = serviceToTest.createReport(context);

        /* test */
        verify(trafficLightCalculator, never()).calculateTrafficLight(sechubResult);
        assertEquals(TrafficLight.OFF.name(), result.getTrafficLightAsString());
    }

    @Test
    void createReport_calls_trafficlight_calculator_with_result_at_least_one_real_product_result_contained() throws Exception {
        /* prepare */
        when(reportTransformationResult.isAtLeastOneRealProductResultContained()).thenReturn(true);

        /* execute */
        serviceToTest.createReport(context);

        /* test */
        verify(trafficLightCalculator).calculateTrafficLight(sechubResult);
    }

}
