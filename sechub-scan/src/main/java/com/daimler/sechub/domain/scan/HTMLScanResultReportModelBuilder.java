// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.daimler.sechub.commons.model.SecHubFinding;
import com.daimler.sechub.commons.model.SecHubResult;
import com.daimler.sechub.commons.model.TrafficLight;
import com.daimler.sechub.domain.scan.report.ScanReportResult;
import com.daimler.sechub.domain.scan.report.ScanReportTrafficLightCalculator;
import com.daimler.sechub.sharedkernel.MustBeDocumented;

@Component
public class HTMLScanResultReportModelBuilder {

    static final String SHOW_LIGHT = "opacity: 1.0";
    static final String HIDE_LIGHT = "opacity: 0.25";

    private static final Logger LOG = LoggerFactory.getLogger(HTMLScanResultReportModelBuilder.class);

    @Value("${sechub.development.webdesignmode.enabled:false}")
    @MustBeDocumented(scope = "development", value = "Developers can turn on this mode to have reports creating with external css. Normally the html model builder will create embedded css content")
    boolean webDesignMode;

    @Value("classpath:templates/report/html/scanresult.css")
    Resource cssResource;

    String embeddedCSS;

    @Autowired
    ScanReportTrafficLightCalculator trafficLightCalculator;

    public Map<String, Object> build(ScanReportResult scanResult) {
        String trafficLight = scanResult.getTrafficLight();

        String styleRed = HIDE_LIGHT;
        String styleYellow = HIDE_LIGHT;
        String styleGreen = HIDE_LIGHT;

        switch (trafficLight) {
        case "RED":
            styleRed = SHOW_LIGHT;
            break;
        case "YELLOW":
            styleYellow = SHOW_LIGHT;
            break;
        case "GREEN":
            styleGreen = SHOW_LIGHT;
            break;
        default:
        }
        HtmlCodeScanDescriptionSupport codeScanSupport = new HtmlCodeScanDescriptionSupport();
        SecHubResult result = scanResult.getResult();

        Map<Integer, List<HTMLScanResultCodeScanEntry>> codeScanEntries = new HashMap<>();
        for (SecHubFinding finding : result.getFindings()) {
            codeScanEntries.put(finding.getId(), codeScanSupport.buildEntries(finding));
        }

        Map<String, Object> model = new HashMap<>();
        model.put("result", scanResult.getResult());
        model.put("redList", trafficLightCalculator.filterFindingsFor(result, TrafficLight.RED));
        model.put("yellowList", trafficLightCalculator.filterFindingsFor(result, TrafficLight.YELLOW));
        model.put("greenList", trafficLightCalculator.filterFindingsFor(result, TrafficLight.GREEN));

        model.put("trafficlight", trafficLight);

        model.put("styleRed", styleRed);
        model.put("styleYellow", styleYellow);
        model.put("styleGreen", styleGreen);
        model.put("isWebDesignMode", webDesignMode);
        model.put("codeScanEntries", codeScanEntries);
        model.put("codeScanSupport", codeScanSupport);

        if (webDesignMode) {
            File file;
            try {
                if (cssResource == null) {
                    LOG.error("CSS resource not set:{}", cssResource);
                } else {
                    file = cssResource.getFile();
                    String absolutePathToCSSFile = file.getAbsolutePath();
                    LOG.info("Web design mode activate, using not embedded css but ref to:{}", absolutePathToCSSFile);
                    model.put("includedCSSRef", absolutePathToCSSFile);
                }
            } catch (Exception e) {
                LOG.error("Was not able get file from resource:{}", cssResource, e);
            }
        }
        UUID jobUUID = scanResult.getJobUUID();
        if (jobUUID != null) {
            model.put("jobuuid", jobUUID.toString());
        } else {
            model.put("jobuuid", "none");
        }
        model.put("info", scanResult.getInfo());

        return model;
    }
}
