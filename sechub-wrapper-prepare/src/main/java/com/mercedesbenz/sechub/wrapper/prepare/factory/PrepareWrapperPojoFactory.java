// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.factory;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.TextFileWriter;
import com.mercedesbenz.sechub.commons.archive.ArchiveSupport;
import com.mercedesbenz.sechub.commons.pds.PDSProcessAdapterFactory;
import com.mercedesbenz.sechub.pds.storage.PDSS3PropertiesSetup;
import com.mercedesbenz.sechub.pds.storage.PDSSharedVolumePropertiesSetup;

@Component
public class PrepareWrapperPojoFactory {

    @Bean
    TextFileWriter createTextFileWriter() {
        return new TextFileWriter();
    }

    @Bean
    PDSProcessAdapterFactory createPDSProcessAdapterFactory() {
        return new PDSProcessAdapterFactory();
    }

    @Bean
    ArchiveSupport createArchiveSupport() {
        return new ArchiveSupport();
    }

    // TODO: 14.05.24 laura is this allowed??? - can I just use PDSMultistorage?
    @Bean
    PDSSharedVolumePropertiesSetup createPDSSharedVolumePropertiesSetup() {
        return new PDSSharedVolumePropertiesSetup();
    }

    @Bean
    PDSS3PropertiesSetup createPDSS3PropertiesSetup() {
        return new PDSS3PropertiesSetup();
    }

}
