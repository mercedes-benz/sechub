package com.mercedesbenz.sechub.domain.scan.report;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.core.util.SimpleStringUtils;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubFinding;
import com.mercedesbenz.sechub.sharedkernel.MustBeDocumented;

@Component
public class ScanReportSensitiveDataObfuscator {

    @Value("${sechub.secretscan.source.visible.length:0}")
    @MustBeDocumented("Define the amount of visible characters which are NOT obfuscated.")
    int sourceVisibleLength;

    /**
     * Obfuscates sensitive scan report data
     *
     * @param report the report to obfuscate
     */
    public void obfuscate(ScanSecHubReport report) {

        List<SecHubFinding> findings = report.getResult().getFindings();

        /* obfuscates secrets from secret scan */
        for (SecHubFinding finding : findings) {
            if (finding.getType() == ScanType.SECRET_SCAN) {
                String secret = finding.getCode().getSource();
                String obfuscated = SimpleStringUtils.createObfuscatedString(secret, sourceVisibleLength);
                finding.getCode().setSource(obfuscated);
            }
        }

    }

}
