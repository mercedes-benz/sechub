package com.mercedesbenz.sechub.domain.scan.report;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.ScanTypeSummaryFindingOverviewData;
import com.mercedesbenz.sechub.commons.model.SecHubFinding;
import com.mercedesbenz.sechub.commons.model.SecHubReportMetaData;
import com.mercedesbenz.sechub.commons.model.SecHubReportModel;
import com.mercedesbenz.sechub.commons.model.SecHubReportScanTypeSummary;
import com.mercedesbenz.sechub.commons.model.SecHubReportSummary;
import com.mercedesbenz.sechub.commons.model.Severity;

class ScanReportToSecHubReportModelWithSummariesTransformerTest {

    private static boolean DEBUG = Boolean.valueOf(System.getProperty("sechub.test.debug"));

    private static final int CRITICAL_FINDING1_CWEID = 1;
    private static final int HIGH_FINDING_CWEID = 2;
    private static final int MEDIUM_FINDING_CWEID = 3;
    private static final int LOW_FINDING_CWEID = 4;
    private static final int INFO_FINDING_CWEID = 5;
    private static final int UNCLASSIFIED_FINDING_CWEID = 6;
    private static final int CRITICAL_FINDING2_CWEID = 7;

    private static final String CRITICAL_FINDING1_NAME = "Critical name1";
    private static final String CRITICAL_FINDING2_NAME = "Critical name2";
    private static final String HIGH_FINDING_NAME = "Cross Site Scripting (Reflected)";
    private static final String MEDIUM_FINDING_NAME = "CSP: Wildcard Directive";
    private static final String LOW_FINDING_NAME = "Cookie Without Secure Flag";
    private static final String INFO_FINDING_NAME = "Info name";
    private static final String UNCLASSIFIED_FINDING_NAME = "Unclassified name";

    private SecHubFinding criticalCodeScanFinding1;
    private SecHubFinding criticalCodeScanFinding2;

    private SecHubFinding highSecretScanFinding;
    private SecHubFinding mediumWebScanFinding;
    private SecHubFinding lowInfraScanFinding;
    private SecHubFinding infoLicenseFinding;
    private SecHubFinding unclassifiedFinding;
    private ScanReportToSecHubReportModelWithSummariesTransformer transformerToTest;

    private SecHubFinding criticalWebScanFinding1;

    private SecHubFinding lowCodeScanFinding;

    @BeforeEach
    void beforeEach() {

        transformerToTest = new ScanReportToSecHubReportModelWithSummariesTransformer();

        criticalCodeScanFinding1 = new SecHubFinding();
        criticalCodeScanFinding1.setCweId(CRITICAL_FINDING1_CWEID);
        criticalCodeScanFinding1.setSeverity(Severity.CRITICAL);
        criticalCodeScanFinding1.setName(CRITICAL_FINDING1_NAME);
        criticalCodeScanFinding1.setType(ScanType.CODE_SCAN);

        criticalWebScanFinding1 = new SecHubFinding();
        criticalWebScanFinding1.setCweId(CRITICAL_FINDING1_CWEID);
        criticalWebScanFinding1.setSeverity(Severity.CRITICAL);
        criticalWebScanFinding1.setName(CRITICAL_FINDING1_NAME);
        criticalWebScanFinding1.setType(ScanType.WEB_SCAN);

        criticalCodeScanFinding2 = new SecHubFinding();
        criticalCodeScanFinding2.setCweId(CRITICAL_FINDING2_CWEID);
        criticalCodeScanFinding2.setSeverity(Severity.CRITICAL);
        criticalCodeScanFinding2.setName(CRITICAL_FINDING2_NAME);
        criticalCodeScanFinding2.setType(ScanType.CODE_SCAN);

        highSecretScanFinding = new SecHubFinding();
        highSecretScanFinding.setCweId(HIGH_FINDING_CWEID);
        highSecretScanFinding.setSeverity(Severity.HIGH);
        highSecretScanFinding.setName(HIGH_FINDING_NAME);
        highSecretScanFinding.setType(ScanType.SECRET_SCAN);

        mediumWebScanFinding = new SecHubFinding();
        mediumWebScanFinding.setCweId(MEDIUM_FINDING_CWEID);
        mediumWebScanFinding.setSeverity(Severity.MEDIUM);
        mediumWebScanFinding.setName(MEDIUM_FINDING_NAME);
        mediumWebScanFinding.setType(ScanType.WEB_SCAN);

        lowInfraScanFinding = new SecHubFinding();
        lowInfraScanFinding.setCweId(LOW_FINDING_CWEID);
        lowInfraScanFinding.setSeverity(Severity.LOW);
        lowInfraScanFinding.setName(LOW_FINDING_NAME);
        lowInfraScanFinding.setType(ScanType.INFRA_SCAN);

        lowCodeScanFinding = new SecHubFinding();
        lowCodeScanFinding.setCweId(LOW_FINDING_CWEID);
        lowCodeScanFinding.setSeverity(Severity.LOW);
        lowCodeScanFinding.setName(LOW_FINDING_NAME);
        lowCodeScanFinding.setType(ScanType.CODE_SCAN);

        infoLicenseFinding = new SecHubFinding();
        infoLicenseFinding.setCweId(INFO_FINDING_CWEID);
        infoLicenseFinding.setSeverity(Severity.INFO);
        infoLicenseFinding.setName(INFO_FINDING_NAME);
        infoLicenseFinding.setType(ScanType.LICENSE_SCAN);

        unclassifiedFinding = new SecHubFinding();
        unclassifiedFinding.setCweId(UNCLASSIFIED_FINDING_CWEID);
        unclassifiedFinding.setSeverity(Severity.UNCLASSIFIED);
        unclassifiedFinding.setName(UNCLASSIFIED_FINDING_NAME);

    }

    @Test
    void report_with_3_finding_is_transformed_to_model_with_3_findings() {
        /* prepare */
        ScanReport report = buildReport(criticalCodeScanFinding1, criticalWebScanFinding1, lowCodeScanFinding);

        /* execute */
        SecHubReportModel result = transformerToTest.transform(report);

        /* test */
        if (DEBUG) {
            String asJson = result.toFormattedJSON();
            System.out.println(asJson);
        }
        List<SecHubFinding> findings = result.getResult().getFindings();
        assertEquals(3, findings.size());
    }

    @Test
    void report_with_no_finding_is_transformed_with_meta_data_but_no_findings() {
        /* prepare */
        ScanReport report = buildReport();

        /* execute */
        SecHubReportModel result = transformerToTest.transform(report);

        /* test */
        if (DEBUG) {
            String asJson = result.toFormattedJSON();
            System.out.println(asJson);
        }
        assertEquals(0, result.getResult().getCount());

        Optional<SecHubReportMetaData> metaDataOpt = result.getMetaData();
        assertTrue(metaDataOpt.isPresent());

        SecHubReportSummary summary = metaDataOpt.get().getSummary();
        assertTrue(summary.getCodeScan().isEmpty());
        assertTrue(summary.getInfraScan().isEmpty());
        assertTrue(summary.getSecretScan().isEmpty());
        assertTrue(summary.getLicenseScan().isEmpty());
        assertTrue(summary.getWebScan().isEmpty());
    }

    @Test
    void report_with_1_critical_codescan_finding_is_transformed_with_meta_data_details() {

        /* prepare */
        ScanReport report = buildReport(criticalCodeScanFinding1);

        /* execute */
        SecHubReportModel result = transformerToTest.transform(report);

        /* test */
        if (DEBUG) {
            String asJson = result.toFormattedJSON();
            System.out.println(asJson);
        }
        SecHubReportSummary summary = result.getMetaData().get().getSummary();
        List<ScanTypeSummaryFindingOverviewData> critical = summary.getCodeScan().get().getDetails().getCritical();
        assertEquals(1, critical.size());
        ScanTypeSummaryFindingOverviewData criticalCodeScanDetails = critical.iterator().next();
        assertNotNull(criticalCodeScanDetails);
        assertEquals(1, criticalCodeScanDetails.getCount());
        assertEquals(CRITICAL_FINDING1_NAME, criticalCodeScanDetails.getName());
        assertEquals(CRITICAL_FINDING1_CWEID, criticalCodeScanDetails.getCweId());
    }

    @Test
    void report_with_4_same_critical_codescan_finding_is_transformed_with_meta_data_details() {

        /* prepare */
        ScanReport report = buildReport(criticalCodeScanFinding1, criticalCodeScanFinding1, criticalCodeScanFinding1, criticalCodeScanFinding1);

        /* execute */
        SecHubReportModel result = transformerToTest.transform(report);

        /* test */
        if (DEBUG) {
            String asJson = result.toFormattedJSON();
            System.out.println(asJson);
        }
        SecHubReportSummary summary = result.getMetaData().get().getSummary();
        List<ScanTypeSummaryFindingOverviewData> critical = summary.getCodeScan().get().getDetails().getCritical();
        assertEquals(1, critical.size());
        ScanTypeSummaryFindingOverviewData criticalCodeScanDetails = critical.iterator().next();
        assertNotNull(criticalCodeScanDetails);
        assertEquals(4, criticalCodeScanDetails.getCount());
        assertEquals(CRITICAL_FINDING1_CWEID, criticalCodeScanDetails.getCweId());
        assertEquals(CRITICAL_FINDING1_NAME, criticalCodeScanDetails.getName());
    }

    @Test
    void report_with_5_critical_codescan_finding_2_different_is_transformed_with_meta_data_details() {

        /* prepare */
        ScanReport report = buildReport(criticalCodeScanFinding2, criticalCodeScanFinding1, criticalCodeScanFinding2, criticalCodeScanFinding2,
                criticalCodeScanFinding1);

        /* execute */
        SecHubReportModel result = transformerToTest.transform(report);

        /* test */
        if (DEBUG) {
            String asJson = result.toFormattedJSON();
            System.out.println(asJson);
        }
        SecHubReportSummary summary = result.getMetaData().get().getSummary();
        List<ScanTypeSummaryFindingOverviewData> critical = summary.getCodeScan().get().getDetails().getCritical();
        assertEquals(2, critical.size());

        Iterator<ScanTypeSummaryFindingOverviewData> iterator = critical.iterator();
        ScanTypeSummaryFindingOverviewData criticalCodeScanDetails1 = iterator.next();
        assertEquals(2, criticalCodeScanDetails1.getCount());
        assertEquals(CRITICAL_FINDING1_NAME, criticalCodeScanDetails1.getName()); // finding1 first because name sorted...
        assertEquals(CRITICAL_FINDING1_CWEID, criticalCodeScanDetails1.getCweId());

        ScanTypeSummaryFindingOverviewData criticalCodeScanDetails2 = iterator.next();
        assertEquals(3, criticalCodeScanDetails2.getCount());
        assertEquals(CRITICAL_FINDING2_NAME, criticalCodeScanDetails2.getName());
        assertEquals(CRITICAL_FINDING2_CWEID, criticalCodeScanDetails2.getCweId());
    }

    @Test
    void report_with_5_critical_codescan_finding_2_different_is_transformed_with_meta_data_counts() {

        /* prepare */
        ScanReport report = buildReport(criticalCodeScanFinding2, criticalCodeScanFinding1, criticalCodeScanFinding2, criticalCodeScanFinding2,
                criticalCodeScanFinding1);

        /* execute */
        SecHubReportModel result = transformerToTest.transform(report);

        /* test */
        if (DEBUG) {
            String asJson = result.toFormattedJSON();
            System.out.println(asJson);
        }
        SecHubReportSummary summary = result.getMetaData().get().getSummary();
        SecHubReportScanTypeSummary codeScan = summary.getCodeScan().get();
        assertEquals(5, codeScan.getRed());
        assertEquals(5, codeScan.getTotal());
    }

    @Test
    void report_with_different_findings_and_severities_is_transformed_with_meta_data_counts() {

        /* prepare */
        /* @formatter:off */
        ScanReport report = buildReport(
                criticalCodeScanFinding2,
                lowCodeScanFinding,
                mediumWebScanFinding,
                mediumWebScanFinding,
                criticalWebScanFinding1,
                highSecretScanFinding,
                infoLicenseFinding,
                lowInfraScanFinding,
                criticalCodeScanFinding1,
                criticalCodeScanFinding2,
                criticalCodeScanFinding2,
                criticalCodeScanFinding1);
        /* @formatter:on */

        /* execute */
        SecHubReportModel result = transformerToTest.transform(report);

        /* test */
        if (DEBUG) {
            String asJson = result.toFormattedJSON();
            System.out.println(asJson);
        }
        SecHubReportSummary summary = result.getMetaData().get().getSummary();
        SecHubReportScanTypeSummary codeScan = summary.getCodeScan().get();
        assertEquals(5, codeScan.getRed());
        assertEquals(1, codeScan.getGreen());
        assertEquals(6, codeScan.getTotal());

        SecHubReportScanTypeSummary webScan = summary.getWebScan().get();
        assertEquals(1, webScan.getRed());
        assertEquals(2, webScan.getYellow());
        assertEquals(3, webScan.getTotal());

        SecHubReportScanTypeSummary secretScan = summary.getSecretScan().get();
        assertEquals(1, secretScan.getRed());
        assertEquals(1, secretScan.getTotal());

        SecHubReportScanTypeSummary infraScan = summary.getInfraScan().get();
        assertEquals(1, infraScan.getGreen());
        assertEquals(1, infraScan.getTotal());

        SecHubReportScanTypeSummary licenseScan = summary.getLicenseScan().get();
        assertEquals(1, licenseScan.getGreen());
        assertEquals(1, licenseScan.getTotal());

        assertEquals(12, result.getResult().getCount());
    }

    private ScanReport buildReport(SecHubFinding... findingsForReport) {

        ScanReport report;
        List<SecHubFinding> findings;
        SecHubReportModel model;

        report = new ScanReport();
        report.setResultType(ScanReportResultType.MODEL);
        model = new SecHubReportModel();
        findings = model.getResult().getFindings();

        for (SecHubFinding finding : findingsForReport) {
            findings.add(finding);
        }

        String json = model.toJSON();
        report.setResult(json);
        return report;
    }

}
