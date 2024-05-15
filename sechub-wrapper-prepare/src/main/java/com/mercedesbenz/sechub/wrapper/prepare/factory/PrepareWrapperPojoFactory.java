// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.factory;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.TextFileWriter;
import com.mercedesbenz.sechub.commons.archive.ArchiveSupport;
import com.mercedesbenz.sechub.commons.pds.PDSProcessAdapterFactory;

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

}
