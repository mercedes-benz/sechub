package com.mercedesbenz.sechub.wrapper.prepare;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mercedesbenz.sechub.test.ManualTest;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:manual-testdata-skopeo-prepare-wrapper-spring-boot.properties")
class PrepareWrapperApplicationSkopeoManualTest implements ManualTest {

    @BeforeAll
    static void beforeAll() throws Exception {
        PrepareWrapperManualTestUtil.cleanup();
    }

    @Test
    void manualTestByDeveloper() {
        System.out.println("prepare wrapper called, skopeo must be used");
    }

}
