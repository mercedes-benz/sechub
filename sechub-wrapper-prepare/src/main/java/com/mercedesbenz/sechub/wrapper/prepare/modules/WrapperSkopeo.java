package com.mercedesbenz.sechub.wrapper.prepare.modules;

import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironmentVariables.*;
import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperKeyConstants.KEY_PDS_PREPARE_AUTHENTICATION_FILE_SKOPEO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.pds.ProcessAdapter;

@Component
public class WrapperSkopeo extends WrapperTool {

    @Value("${" + KEY_PDS_PREPARE_AUTHENTICATION_FILE_SKOPEO + ":authentication.json}")
    String pdsPrepareAuthenticationFileSkopeo;

    void download(SkopeoContext context) throws IOException {
        if (!context.getCredentialMap().isEmpty()) {
            login(context);
        }

        ProcessBuilder builder = buildProcessDownload(context);
        ProcessAdapter process = null;

        try {
            process = processAdapterFactory.startProcess(builder);
        } catch (IOException e) {
            throw new IOException("Error while download with Skopeo from: " + context.getLocation(), e);
        }

        waitForProcessToFinish(process);
    }

    @Override
    void cleanUploadDirectory(String uploadDirectory) throws IOException {
        ProcessBuilder builder = buildProcessClean(uploadDirectory);
        ProcessAdapter process = null;

        try {
            process = processAdapterFactory.startProcess(builder);
        } catch (IOException e) {
            throw new IOException("Error while cleaning authentication file.", e);
        }

        waitForProcessToFinish(process);
    }

    private void login(SkopeoContext context) throws IOException {
        ProcessBuilder builder = buildProcessLogin(context);
        exportEnvironmentVariables(builder, context.getCredentialMap());
        ProcessAdapter process = null;

        try {
            process = processAdapterFactory.startProcess(builder);
        } catch (IOException e) {
            throw new IOException("Error while login with Skopeo to: " + context.getLocation(), e);
        }

        waitForProcessToFinish(process);
    }

    private ProcessBuilder buildProcessLogin(SkopeoContext context) {
        // skopeo login "$LOCATION" --username "$USERNAME" --password "$PASSWORD"
        // --authfile "$PDS_JOB_WORKSPACE_LOCATION/$SKOPEO_AUTH"
        List<String> commands = new ArrayList<>();

        String location = transformLocationForLogin(context.getLocation());
        File uploadDir = Paths.get(context.getUploadDirectory()).toAbsolutePath().toFile();

        commands.add("/bin/bash");
        commands.add("-c");
        commands.add("skopeo login " + location + " --username $" + PDS_PREPARE_CREDENTIAL_USERNAME + " --password $" + PDS_PREPARE_CREDENTIAL_PASSWORD
                + " --authfile " + pdsPrepareAuthenticationFileSkopeo);

        /*
         * commands.add("skopeo"); commands.add("login"); commands.add(location);
         * commands.add("--username"); commands.add("$" +
         * PDS_PREPARE_CREDENTIAL_USERNAME); commands.add("--password");
         * commands.add("$" + PDS_PREPARE_CREDENTIAL_PASSWORD);
         * commands.add("--authfile"); commands.add(pdsPrepareAuthenticationFileSkopeo);
         */

        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.directory(uploadDir);
        builder.inheritIO();

        return builder;
    }

    private ProcessBuilder buildProcessDownload(SkopeoContext context) {
        // skopeo copy docker://$LOCATION
        // docker-archive:$PDS_JOB_WORKSPACE_LOCATION/$PDS_PREPARE_UPLOAD_FOLDER
        List<String> commands = new ArrayList<>();

        String location = transformLocationForDownload(context.getLocation());
        File uploadDir = Paths.get(context.getUploadDirectory()).toAbsolutePath().toFile();

        /*
         * commands.add("/bin/bash"); commands.add("-c"); if
         * (!context.getCredentialMap().isEmpty()){ commands.add("skopeo copy " +
         * location + " docker-archive:" + context.getFilename()); }else{
         * commands.add("skopeo copy " + location + " docker-archive:" +
         * context.getFilename() + " --authfile " + pdsPrepareAuthenticationFileSkopeo);
         * }
         */

        commands.add("skopeo");
        commands.add("copy");
        commands.add(location);
        commands.add("docker-archive:" + context.getFilename());
        if (!context.getCredentialMap().isEmpty()) {
            commands.add("--authfile");
            commands.add(pdsPrepareAuthenticationFileSkopeo);
        }

        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.directory(uploadDir);
        builder.inheritIO();

        return builder;
    }

    private ProcessBuilder buildProcessClean(String pdsPrepareUploadFolderDirectory) {
        // removes authentication file
        List<String> commands = new ArrayList<>();

        File uploadDir = Paths.get(pdsPrepareUploadFolderDirectory).toAbsolutePath().toFile();

        commands.add("/bin/bash");
        commands.add("-c");
        commands.add("rm -rf " + pdsPrepareAuthenticationFileSkopeo);

        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.directory(uploadDir);
        builder.inheritIO();
        return builder;
    }

    private String transformLocationForDownload(String location) {
        String dockerPrefix = "docker://";

        if (location.startsWith(dockerPrefix)) {
            return location;
        }

        if (location.startsWith("https://")) {
            location = location.replace("https://", dockerPrefix);
            return location;
        }

        return dockerPrefix + location;
    }

    private String transformLocationForLogin(String location) {
        if (location.startsWith("docker://")) {
            location = location.replace("docker://", "");
        }
        if (location.startsWith("https://")) {
            location = location.replace("https://", "");
        }
        return location.split("/")[0];
    }
}
