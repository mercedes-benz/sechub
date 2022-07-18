package com.mercedesbenz.sechub.wrapper.checkmarx;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mercedesbenz.sechub.adapter.mock.MockedAdapterSetupService;
import com.mercedesbenz.sechub.wrapper.checkmarx.cli.CheckmarxWrapperCLIEnvironment;
import com.mercedesbenz.sechub.wrapper.checkmarx.scan.CheckmarxWrapperContextFactory;
import com.mercedesbenz.sechub.wrapper.checkmarx.scan.CheckmarxWrapperScanService;

@SpringBootTest(classes = { CheckmarxWrapperContextFactory.class, CheckmarxWrapperScanService.class, CheckmarxWrapperPojoFactory.class,
        CheckmarxWrapperCLIEnvironment.class, MockedAdapterSetupService.class })
@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
class CheckmarxWrapperScanServiceSpringBootTest {

    @Autowired
    CheckmarxWrapperScanService scanService;

    /**
     * In application-test.properties we have defined to use the mock adapter
     * variant and also other test relevant parts (team id and preset id mapping).
     * So we can execute the scan start completely here.
     *
     * @throws Exception
     */
    @Test
    void start_scan_with_checkmarx_mocked_adapter_is_possible_and_returns_not_null() throws Exception {

        /* execute */
        String result = scanService.startScan();

        /* test */
        assertNotNull(result);

    }

}
