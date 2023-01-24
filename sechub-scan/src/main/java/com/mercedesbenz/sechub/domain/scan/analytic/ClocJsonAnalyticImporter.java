package com.mercedesbenz.sechub.domain.scan.analytic;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.analytic.AnalyticData;

/**
 * Importer for CLOC output (see https://github.com/AlDanial/cloc) - JSON only
 * 
 * @author Albert Tregnaghi
 *
 */
@Component
public class ClocJsonAnalyticImporter implements AnalyticDataImporter {

    @Override
    public void importIfDataIsSupported(String productResult, AnalyticData model) {

    }

}
