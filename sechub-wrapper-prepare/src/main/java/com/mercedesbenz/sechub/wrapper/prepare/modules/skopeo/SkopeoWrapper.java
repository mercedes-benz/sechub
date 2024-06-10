// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules.skopeo;

import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperKeyConstants.*;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.pds.PDSProcessAdapterFactory;
import com.mercedesbenz.sechub.commons.pds.ProcessAdapter;
import com.mercedesbenz.sechub.commons.pds.ProcessBuilderFactory;
import com.mercedesbenz.sechub.pds.commons.core.PDSLogSanitizer;
import com.mercedesbenz.sechub.wrapper.prepare.modules.AbstractToolWrapper;

@Component
public class SkopeoWrapper extends AbstractToolWrapper {

    private static final String AUTHENTICATION_DEFAULT_FILENAME = "authentication.json";

    @Value("${" + KEY_PDS_PREPARE_AUTHENTICATION_FILE_SKOPEO + ":" + AUTHENTICATION_DEFAULT_FILENAME + "}")
    String pdsPrepareAuthenticationFileSkopeo = AUTHENTICATION_DEFAULT_FILENAME;// when not initialized by spring

    @Autowired
    PDSProcessAdapterFactory processAdapterFactory;

    @Autowired
    PDSLogSanitizer logSanitizer;

    @Autowired
    ProcessBuilderFactory processBuilderFactory;

    @Autowired
    SkopeoLocationConverter locationConverter;

    /**
     * Downloads docker image via skopeo. If credentials are defined, a login is
     * done before automatically afterwards the temp autorization data is removed as
     * well
     *
     * @param context
     * @throws IOException
     */
    public void download(SkopeoContext context) throws IOException {

        handleLoginIfNecessary(context);

        ProcessBuilder builder = buildProcessDownload(context);
        ProcessAdapter process = null;

        try {
            process = processAdapterFactory.startProcess(builder);
        } catch (IOException e) {
            throw new IOException("Error while starting Skopeo download process for: " + logSanitizer.sanitize(context.getLocation(), 512), e);
        }

        waitForProcessToFinish(process);

        handleLogoutIfnecessary(context);
    }

    private void handleLoginIfNecessary(SkopeoContext context) throws IOException {
        if (!context.hasCredentials()) {
            return;
        }
        ProcessBuilder builder = buildProcessLogin(context);
        ProcessAdapter process = null;

        try {
            process = processAdapterFactory.startProcess(builder);
            process.enterInput(context.getUnsealedPassword().toCharArray());
        } catch (IOException e) {
            throw new IOException("Error while login with Skopeo to: " + context.getLocation(), e);
        }

        waitForProcessToFinish(process);
    }

    private ProcessBuilder buildProcessLogin(SkopeoContext context) {
        List<String> commands = new ArrayList<>();

        String location = locationConverter.convertLocationForLogin(context.getLocation());
        File downloadDirectory = context.getToolDownloadDirectory().toFile();

        commands.add("skopeo");
        commands.add("login");
        commands.add(location);
        commands.add("--username");
        commands.add(context.getUnsealedUsername());
        commands.add("--password-stdin");
        commands.add("--authfile");
        commands.add(pdsPrepareAuthenticationFileSkopeo);

        ProcessBuilder builder = processBuilderFactory.createForCommandList(commands);
        builder.directory(downloadDirectory);
        // here we do NO redirect of input!
        builder.redirectOutput(Redirect.INHERIT);
        builder.redirectError(Redirect.INHERIT);

        return builder;
    }

    private ProcessBuilder buildProcessDownload(SkopeoContext context) {
        List<String> commands = new ArrayList<>();

        String location = locationConverter.convertLocationForDownload(context.getLocation());
        File downloadDirectory = context.getToolDownloadDirectory().toFile();

        commands.add("skopeo");
        commands.add("copy");
        commands.add(location);
        commands.add("docker-archive:" + context.getDownloadTarFile());
        if (context.hasCredentials()) {
            commands.add("--authfile");
            commands.add(pdsPrepareAuthenticationFileSkopeo);
        }

        ProcessBuilder builder = processBuilderFactory.createForCommandList(commands);
        builder.directory(downloadDirectory);
        builder.inheritIO();

        return builder;
    }

    private void handleLogoutIfnecessary(SkopeoContext skopeoContext) throws IOException {
        if (!skopeoContext.hasCredentials()) {
            return;
        }
        /*
         * Currently we do a "logout" be removing the authorization file.
         *
         * We could use the login functionality in future, but
         * https://github.com/containers/skopeo/blob/main/docs/skopeo-logout.1.md
         * describes that it also only deletes the file?
         */

        Path toolDownloadFolder = skopeoContext.getToolDownloadDirectory();
        FileUtils.deleteDirectory(toolDownloadFolder.toFile());
    }

}
