package com.daimler.sechub.sereco.importer;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.sereco.ImportParameter;
import com.daimler.sechub.sereco.metadata.SerecoCodeCallStackElement;
import com.daimler.sechub.sereco.metadata.SerecoMetaData;
import com.daimler.sechub.sereco.metadata.SerecoSeverity;
import com.daimler.sechub.sereco.metadata.SerecoVulnerability;
import com.daimler.sechub.sereco.test.SerecoTestFileSupport;

public class SarifV1JSONImporterTest {

    private SarifV1JSONImporter importerToTest;
    private String sarif;

    @Before
    public void before() {
        importerToTest = new SarifV1JSONImporter();
        sarif = SerecoTestFileSupport.INSTANCE.loadTestFile("sarif/sarif_2.1.0_brakeman.json");
    }

    @Test
    public void sarifreport_can_be_imported() {
        /* prepare */

        ImportParameter param = ImportParameter.builder().importData(sarif).importId("id1").productId("SARIF").build();

        /* execute */
        ProductImportAbility ableToImport = importerToTest.isAbleToImportForProduct(param);

        /* test */
        assertEquals("Was able to import sarif!", ProductImportAbility.ABLE_TO_IMPORT, ableToImport);
    }

    @Test
    public void sarifreport_has_errorlevel() throws Exception {
        /* prepare */
        SerecoMetaData result = importerToTest.importResult(sarif);

        /* execute */
        List<SerecoVulnerability> vulnerabilities = result.getVulnerabilities();
        SerecoVulnerability v1 = vulnerabilities.get(0);

        /* test */
        assertEquals(SerecoSeverity.HIGH, v1.getSeverity());
    }

    @Test
    public void sarifreport_has_no_description() throws Exception {
        /* prepare */
        SerecoMetaData result = importerToTest.importResult(sarif);

        /* execute */
        List<SerecoVulnerability> vulnerabilities = result.getVulnerabilities();
        SerecoVulnerability v1 = vulnerabilities.get(0);

        /* test */
        assertEquals("", v1.getDescription());
    }

    @Test
    public void sarifreport_has_code_info() throws Exception {
        /* prepare */
        SerecoMetaData result = importerToTest.importResult(sarif);

        /* execute */
        List<SerecoVulnerability> vulnerabilities = result.getVulnerabilities();
        SerecoVulnerability v1 = fetchFirstNonFalsePositive(vulnerabilities);
        SerecoCodeCallStackElement codeInfo = v1.getCode();

        /* test */
        assertNotNull(codeInfo);
        assertEquals("Gemfile.lock", codeInfo.getLocation());
        assertEquals(115, codeInfo.getLine().intValue());
        assertEquals(32, vulnerabilities.size());

    }

    private SerecoVulnerability fetchFirstNonFalsePositive(List<SerecoVulnerability> vulnerabilities) {
        Iterator<SerecoVulnerability> vit = vulnerabilities.iterator();
        SerecoVulnerability v1 = vit.next();
        while (v1.isFalsePositive()) {
            /*
             * we have also false positives here .. and just search for first non false
             * Positive
             */
            v1 = vit.next();
        }
        return v1;
    }

}
