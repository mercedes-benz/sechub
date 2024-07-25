// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.TextFileWriter;
import com.mercedesbenz.sechub.commons.archive.ArchiveSupport;
import com.mercedesbenz.sechub.commons.archive.DirectoryAndFileSupport;
import com.mercedesbenz.sechub.commons.core.security.CheckSumSupport;
import com.mercedesbenz.sechub.commons.pds.DefaultProcessBuilderFactory;
import com.mercedesbenz.sechub.commons.pds.PDSProcessAdapterFactory;
import com.mercedesbenz.sechub.commons.pds.ProcessBuilderFactory;
import com.mercedesbenz.sechub.pds.commons.core.PDSLogSanitizer;

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

    @Bean
    CheckSumSupport createCheckSumSupport() {
        return new CheckSumSupport();
    }

    @Bean
    PDSLogSanitizer createPDSLogSanitizer() {
        return new PDSLogSanitizer();
    }

    @Bean
    ProcessBuilderFactory createProcessBuilderFactory() {
        return new DefaultProcessBuilderFactory();
    }

    @Bean
    DirectoryAndFileSupport createDirectoryAndFileSupport() {
        return new DirectoryAndFileSupport();
    }

}
