package com.mercedesbenz.sechub.domain.scan.report;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.core.util.SimpleStringUtils;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubCodeCallStack;
import com.mercedesbenz.sechub.commons.model.SecHubFinding;
import com.mercedesbenz.sechub.sharedkernel.MustBeDocumented;

@Component
public class ScanReportSensitiveDataObfuscator {

    @Value("${sechub.report.sensitivedata.max.nonobfuscated.characters:0}")
    @MustBeDocumented("Define the amount of visible characters which are NOT obfuscated.")
    int sourceVisibleLength;

    private static final Logger LOG = LoggerFactory.getLogger(ScanReportSensitiveDataObfuscator.class);

    /**
     * Obfuscates sensitive scan report data
     *
     * @param report the report to obfuscate
     */
    public void obfuscate(ScanSecHubReport report) {

        if (report == null) {
            return;
        }

        /* result and findings are not null */
        List<SecHubFinding> findings = report.getResult().getFindings();

        for (SecHubFinding finding : findings) {
            /* obfuscates secrets from secret scan */
            if (ScanType.SECRET_SCAN.equals(finding.getType())) {
                SecHubCodeCallStack code = finding.getCode();
                if (code == null) {
                    LOG.debug("Could not obfuscate secret: codeCallstack was null");
                    continue;
                }

                String secret = code.getSource();
                if (secret == null) {
                    LOG.debug("Could not obfuscate secret: code source was null");
                    continue;
                }

                String obfuscated = SimpleStringUtils.createObfuscatedString(secret, sourceVisibleLength);
                if (obfuscated == null) {
                    LOG.debug("Could not obfuscate secret: obfuscated string was null");
                    continue;
                }
                code.setSource(obfuscated);
            }
        }

    }

}
