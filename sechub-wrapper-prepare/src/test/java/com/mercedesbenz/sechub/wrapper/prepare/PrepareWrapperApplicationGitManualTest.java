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
@TestPropertySource(locations = "classpath:manual-testdata-git-prepare-wrapper-spring-boot.properties")
class PrepareWrapperApplicationGitManualTest implements ManualTest {

    @BeforeAll
    static void beforeAll() throws Exception {
        PrepareWrapperManualTestUtil.cleanup();
    }

    /**
     * Per default this manual test uses configured shared volume as storage. If you
     * want to test with a S3, please set following environment values in your IDE
     * launch configuration:
     *
     * <pre>
     * PDS_STORAGE_S3_ACCESSKEY
     * PDS_STORAGE_S3_SECRETKEY
     * PDS_STORAGE_S3_BUCKETNAME
     * PDS_STORAGE_S3_ENDPOINT
     * </pre>
     */
    @Test
    void manualTestByDeveloper() {
        System.out.println("prepare wrapper called, jgit must be used");
    }

}
