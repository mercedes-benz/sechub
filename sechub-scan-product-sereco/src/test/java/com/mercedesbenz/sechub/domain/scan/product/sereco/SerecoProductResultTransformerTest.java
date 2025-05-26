// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubCodeCallStack;
import com.mercedesbenz.sechub.commons.model.SecHubFinding;
import com.mercedesbenz.sechub.commons.model.SecHubReportMetaData;
import com.mercedesbenz.sechub.commons.model.SecHubResult;
import com.mercedesbenz.sechub.commons.model.SecHubRevisionData;
import com.mercedesbenz.sechub.commons.model.SecHubVersionControlData;
import com.mercedesbenz.sechub.commons.model.Severity;
import com.mercedesbenz.sechub.domain.scan.AssertSecHubResult;
import com.mercedesbenz.sechub.domain.scan.ReportTransformationResult;
import com.mercedesbenz.sechub.domain.scan.product.ProductResult;
import com.mercedesbenz.sechub.domain.scan.product.config.WithoutProductExecutorConfigInfo;
import com.mercedesbenz.sechub.sereco.metadata.SerecoClassification;
import com.mercedesbenz.sechub.sereco.metadata.SerecoCodeCallStackElement;
import com.mercedesbenz.sechub.sereco.metadata.SerecoMetaData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoRevisionData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoSeverity;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVersionControl;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;

public class SerecoProductResultTransformerTest {

    private SerecoProductResultTransformer transformerToTest;

    @BeforeEach
    void before() {
        transformerToTest = new SerecoProductResultTransformer();
        transformerToTest.falsePositiveMarker = mock(SerecoFalsePositiveMarker.class);
    }

    @Test
    void finding_revision_id_inside_sereco_is_transformed_to_sechub_result() throws Exception {
        /* prepare */
        String converted = createMetaDataWithOneVulnerabilityFoundWithVersionControlData(null, null, "finding-rev1");

        /* execute */
        ReportTransformationResult result = transformerToTest.transform(createProductResult(converted));

        /* test */
        Iterator<SecHubFinding> it = result.getResult().getFindings().iterator();
        assertTrue(it.hasNext(), "no finding found!");

        SecHubFinding finding = it.next();
        Optional<SecHubRevisionData> revOpt = finding.getRevision();
        assertTrue(revOpt.isPresent());
        SecHubRevisionData revisionData = revOpt.get();
        assertEquals("finding-rev1", revisionData.getId());
    }

    @Test
    void version_control_inside_sereco_is_transformed_to_sechub_result() throws Exception {
        /* prepare */
        String converted = createMetaDataWithOneVulnerabilityFoundWithVersionControlData("revision1", "location1", null);

        /* execute */
        ReportTransformationResult result = transformerToTest.transform(createProductResult(converted));

        /* test */
        SecHubReportMetaData metaData = result.getMetaData();
        if (metaData == null) {
            fail("Did not find metadata");
        }
        Optional<SecHubVersionControlData> versionControlOpt = metaData.getVersionControl();
        if (versionControlOpt.isEmpty()) {
            fail("Did not find version control meta data");
        }
        SecHubVersionControlData versionControl = versionControlOpt.get();
        assertEquals("location1", versionControl.getLocation(), "Version control location is not as expected!");
        Optional<SecHubRevisionData> revisionOpt = versionControl.getRevision();
        if (revisionOpt.isEmpty()) {
            fail("No revision opt found inside version control meta data!");
        }
        String revisionId = revisionOpt.get().getId();
        assertEquals("revision1", revisionId, "Version control revision id not as expected");

    }

    @Test
    void version_control_missing_inside_sereco_is_transformed_to_missing_sechub_result() throws Exception {
        /* prepare */
        String converted = createMetaDataWithOneVulnerabilityFoundNoVersionControlData();

        /* execute */
        ReportTransformationResult result = transformerToTest.transform(createProductResult(converted));

        /* test */
        SecHubReportMetaData metaDataOpt = result.getMetaData();
        if (metaDataOpt == null) {
            fail("Did not find metadata");
        }
        Optional<SecHubVersionControlData> versionControlOpt = metaDataOpt.getVersionControl();
        if (versionControlOpt.isPresent()) {
            fail("Found version control meta data!");
        }

    }

    @Test
    void one_vulnerability_in_meta_results_in_one_finding() throws Exception {
        /* prepare */
        String converted = createMetaDataWithOneVulnerabilityFoundNoVersionControlData();

        /* execute */
        ReportTransformationResult result = transformerToTest.transform(createProductResult(converted));

        /* test */
        AssertSecHubResult.assertSecHubResult(result.getResult()).hasFindings(1);
    }

    @Test
    void one_vulnerability_as_secret_in_meta_results_in_one_finding() throws Exception {
        /* prepare */
        String converted = createMetaDataWithOneVulnerabilityAsSecretFound();

        /* execute */
        ReportTransformationResult result = transformerToTest.transform(createProductResult(converted));

        /* test */
        SecHubResult sechubResult = result.getResult();
        for (SecHubFinding finding : sechubResult.getFindings()) {
            assertEquals(ScanType.SECRET_SCAN, finding.getType());
        }

        AssertSecHubResult.assertSecHubResult(sechubResult).hasFindings(1);
        SecHubFinding finding1 = sechubResult.getFindings().get(0);
        assertEquals(Integer.valueOf(4711), finding1.getCweId());
        assertTrue(finding1.getRevision().isEmpty());// no information available

        SecHubCodeCallStack code1 = finding1.getCode();
        assertNotNull(code1);
        assertEquals(Integer.valueOf(1), code1.getLine());
        assertEquals(Integer.valueOf(2), code1.getColumn());
        assertEquals("Location1", code1.getLocation());
        assertEquals("source1", code1.getSource());
        assertEquals("relevantPart1", code1.getRelevantPart());

        SecHubCodeCallStack code2 = code1.getCalls();
        assertNotNull(code2);
        assertEquals(Integer.valueOf(3), code2.getLine());
        assertEquals(Integer.valueOf(4), code2.getColumn());
        assertEquals("Location2", code2.getLocation());
        assertEquals("source2", code2.getSource());
        assertEquals("relevantPart2", code2.getRelevantPart());

    }

    @Test
    void one_vulnerability_as_iac_finding_in_meta_results_in_one_finding() throws Exception {
        /* prepare */
        String converted = createMetaDataWithOneVulnerabilityAsIacFound();

        /* execute */
        ReportTransformationResult result = transformerToTest.transform(createProductResult(converted));

        /* test */
        SecHubResult sechubResult = result.getResult();
        for (SecHubFinding finding : sechubResult.getFindings()) {
            assertEquals(ScanType.IAC_SCAN, finding.getType());
        }

        AssertSecHubResult.assertSecHubResult(sechubResult).hasFindings(1);
        SecHubFinding finding1 = sechubResult.getFindings().get(0);
        assertEquals(Integer.valueOf(4711), finding1.getCweId());
        assertTrue(finding1.getRevision().isEmpty());// no information available

        SecHubCodeCallStack code1 = finding1.getCode();
        assertNotNull(code1);
        assertEquals(Integer.valueOf(1), code1.getLine());
        assertEquals(Integer.valueOf(2), code1.getColumn());
        assertEquals("Location1", code1.getLocation());
        assertEquals("source1", code1.getSource());
        assertEquals("relevantPart1", code1.getRelevantPart());

        SecHubCodeCallStack code2 = code1.getCalls();
        assertNotNull(code2);
        assertEquals(Integer.valueOf(3), code2.getLine());
        assertEquals(Integer.valueOf(4), code2.getColumn());
        assertEquals("Location2", code2.getLocation());
        assertEquals("source2", code2.getSource());
        assertEquals("relevantPart2", code2.getRelevantPart());

    }

    @Test
    void one_vulnerability_as_code_in_meta_results_in_one_finding() throws Exception {
        /* prepare */
        String converted = createMetaDataWithOneVulnerabilityAsCodeFound();

        /* execute */
        ReportTransformationResult result = transformerToTest.transform(createProductResult(converted));

        /* test */
        SecHubResult sechubResult = result.getResult();
        for (SecHubFinding finding : sechubResult.getFindings()) {
            assertEquals(ScanType.CODE_SCAN, finding.getType());
        }

        AssertSecHubResult.assertSecHubResult(sechubResult).hasFindings(1);
        SecHubFinding finding1 = sechubResult.getFindings().get(0);
        assertEquals(Integer.valueOf(4711), finding1.getCweId());

        SecHubCodeCallStack code1 = finding1.getCode();
        assertNotNull(code1);
        assertEquals(Integer.valueOf(1), code1.getLine());
        assertEquals(Integer.valueOf(2), code1.getColumn());
        assertEquals("Location1", code1.getLocation());
        assertEquals("source1", code1.getSource());
        assertEquals("relevantPart1", code1.getRelevantPart());

        SecHubCodeCallStack code2 = code1.getCalls();
        assertNotNull(code2);
        assertEquals(Integer.valueOf(3), code2.getLine());
        assertEquals(Integer.valueOf(4), code2.getColumn());
        assertEquals("Location2", code2.getLocation());
        assertEquals("source2", code2.getSource());
        assertEquals("relevantPart2", code2.getRelevantPart());

    }

    @Test
    void transformation_of_id_finding_description_severity_and_name_are_done() throws Exception {
        /* prepare */
        String converted = createMetaDataWithOneVulnerabilityFoundNoVersionControlData();

        /* execute */
        ReportTransformationResult result = transformerToTest.transform(createProductResult(converted));

        /* test */
        /* @formatter:off */
		for (SecHubFinding f: result.getResult().getFindings()) {
            assertEquals(ScanType.WEB_SCAN,f.getType());
        }
		AssertSecHubResult.assertSecHubResult(result.getResult()).
			hasFindingWithId(1).
				hasDescription("desc1").
				hasSeverity(com.mercedesbenz.sechub.commons.model.Severity.MEDIUM).
				hasName("type1");
		/* @formatter:on */
    }

    @Test
    void transformation_of_solution_is_done() throws Exception {
        /* prepare */
        String converted = createMetaDataWithOneVulnerabilityFoundNoVersionControlData();

        /* execute */
        ReportTransformationResult result = transformerToTest.transform(createProductResult(converted));

        /* test */
        /* @formatter:off */
        AssertSecHubResult.assertSecHubResult(result.getResult()).
            hasFindingWithId(1).
                hasSolution("solution1");
        /* @formatter:on */
    }

    @Test
    void transformation_does_sort_findings() throws Exception {
        /* prepare */
        String converted = createMetaDataWithTwoVulnerabilitiesWrongOrdered();

        /* execute */
        ReportTransformationResult result = transformerToTest.transform(createProductResult(converted));

        /* test */
        List<SecHubFinding> findings = result.getResult().getFindings();
        assertEquals(2, findings.size());

        Iterator<SecHubFinding> iterator = findings.iterator();
        SecHubFinding first = iterator.next();
        SecHubFinding second = iterator.next();

        assertEquals(Severity.CRITICAL, first.getSeverity());
        assertEquals(Severity.MEDIUM, second.getSeverity());

    }

    private ProductResult createProductResult(String converted) {
        ProductResult r = new ProductResult(UUID.randomUUID(), "project1", new WithoutProductExecutorConfigInfo(ProductIdentifier.PDS_WEBSCAN), converted);
        return r;
    }

    private String createMetaDataWithOneVulnerabilityFoundNoVersionControlData() {
        return createMetaDataWithOneVulnerabilityFoundWithVersionControlData(null, null, null);
    }

    private String createMetaDataWithOneVulnerabilityFoundWithVersionControlData(String versionControlRevisionId, String versionControlLocation,
            String findingRevisionId) {
        SerecoMetaData serecoMetaData = new SerecoMetaData();
        List<SerecoVulnerability> vulnerabilities = serecoMetaData.getVulnerabilities();

        SerecoVulnerability v1 = new SerecoVulnerability();
        v1.setDescription("desc1");
        v1.setSeverity(SerecoSeverity.MEDIUM);
        v1.setType("type1");
        v1.setScanType(ScanType.WEB_SCAN);
        v1.setSolution("solution1");
        if (findingRevisionId != null) {
            SerecoRevisionData revision = new SerecoRevisionData();
            revision.setId(findingRevisionId);
            v1.setRevision(revision);
        }

        SerecoClassification cl = v1.getClassification();
        cl.setCapec("capec1");

        vulnerabilities.add(v1);

        if (versionControlRevisionId != null) {
            SerecoVersionControl versionControl = new SerecoVersionControl();
            versionControl.setLocation(versionControlLocation);
            versionControl.setRevisionId(versionControlRevisionId);

            serecoMetaData.setVersionControl(versionControl);
        }

        String converted = JSONConverter.get().toJSON(serecoMetaData);
        return converted;
    }

    private String createMetaDataWithTwoVulnerabilitiesWrongOrdered() {
        SerecoMetaData data = new SerecoMetaData();
        List<SerecoVulnerability> vulnerabilities = data.getVulnerabilities();

        SerecoVulnerability vulnerability1 = new SerecoVulnerability();
        vulnerability1.setDescription("desc1");
        vulnerability1.setSeverity(SerecoSeverity.MEDIUM);
        vulnerability1.setType("type1");
        vulnerability1.setScanType(ScanType.WEB_SCAN);
        vulnerability1.setSolution("solution1");

        vulnerabilities.add(vulnerability1);

        SerecoVulnerability vulnerability2 = new SerecoVulnerability();
        vulnerability2.setDescription("desc1");
        vulnerability2.setSeverity(SerecoSeverity.CRITICAL);
        vulnerability2.setType("type1");
        vulnerability2.setScanType(ScanType.WEB_SCAN);
        vulnerability2.setSolution("solution1");

        vulnerabilities.add(vulnerability2);

        String converted = JSONConverter.get().toJSON(data);
        return converted;
    }

    private String createMetaDataWithOneVulnerabilityAsCodeFound() {
        return createMetaDataWithOneVulnerability(ScanType.CODE_SCAN);
    }

    private String createMetaDataWithOneVulnerabilityAsSecretFound() {
        return createMetaDataWithOneVulnerability(ScanType.SECRET_SCAN);
    }

    private String createMetaDataWithOneVulnerabilityAsIacFound() {
        return createMetaDataWithOneVulnerability(ScanType.IAC_SCAN);
    }

    private String createMetaDataWithOneVulnerability(ScanType scanType) {
        SerecoMetaData data = new SerecoMetaData();
        List<SerecoVulnerability> vulnerabilities = data.getVulnerabilities();

        SerecoVulnerability v1 = new SerecoVulnerability();
        v1.setSeverity(SerecoSeverity.MEDIUM);
        v1.setType("type1");
        v1.setScanType(scanType);

        SerecoCodeCallStackElement serecoCode1 = new SerecoCodeCallStackElement();
        serecoCode1.setLine(1);
        serecoCode1.setColumn(2);
        serecoCode1.setLocation("Location1");
        serecoCode1.setSource("source1");
        serecoCode1.setRelevantPart("relevantPart1");

        v1.setCode(serecoCode1);

        SerecoCodeCallStackElement serecoCode2 = new SerecoCodeCallStackElement();
        serecoCode2.setLine(3);
        serecoCode2.setColumn(4);
        serecoCode2.setLocation("Location2");
        serecoCode2.setSource("source2");
        serecoCode2.setRelevantPart("relevantPart2");

        serecoCode1.setCalls(serecoCode2);

        SerecoClassification cl = v1.getClassification();
        cl.setCapec("capec1");
        cl.setCwe("4711");

        vulnerabilities.add(v1);

        String converted = JSONConverter.get().toJSON(data);
        return converted;
    }

}
