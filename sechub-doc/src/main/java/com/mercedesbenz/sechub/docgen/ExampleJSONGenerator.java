// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.domain.scan.project.ScanMockData;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectMockDataConfiguration;

public class ExampleJSONGenerator implements Generator {

    public String generateScanProjectMockDataConfiguration1() {
        ScanProjectMockDataConfiguration config = new ScanProjectMockDataConfiguration();

        config.setCodeScan(new ScanMockData(TrafficLight.RED));
        config.setWebScan(new ScanMockData(TrafficLight.YELLOW));
        config.setInfraScan(new ScanMockData(TrafficLight.GREEN));

        return prettyPrint(config);

    }

    private String prettyPrint(Object obj) {
        return JSONConverter.get().toJSON(obj, true);
    }

    public String generateScanProjectMockDataConfiguration2() {
        ScanProjectMockDataConfiguration config = new ScanProjectMockDataConfiguration();

        config.setCodeScan(new ScanMockData(TrafficLight.YELLOW));

        return prettyPrint(config);

    }
}
