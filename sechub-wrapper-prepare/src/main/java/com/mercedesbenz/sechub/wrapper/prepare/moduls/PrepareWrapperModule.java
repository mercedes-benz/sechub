// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.moduls;

import java.io.IOException;

import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperContext;

@Service
public interface PrepareWrapperModule {

    boolean isAbleToPrepare(PrepareWrapperContext context);

    void prepare(PrepareWrapperContext context) throws IOException;

    void cleanup(PrepareWrapperContext contex) throws IOException;

    boolean isDownloadSuccessful(PrepareWrapperContext contex) throws IOException;

    boolean isModuleEnabled();

}
