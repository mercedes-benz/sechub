package com.mercedesbenz.sechub.wrapper.prepare.modules.skopeo;

import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironmentVariables.*;
import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperKeyConstants.KEY_PDS_PREPARE_AUTHENTICATION_FILE_SKOPEO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
import com.mercedesbenz.sechub.commons.pds.PDSProcessAdapterFactory;
import com.mercedesbenz.sechub.commons.pds.ProcessAdapter;
import com.mercedesbenz.sechub.wrapper.prepare.modules.WrapperTool;

@Component
public class WrapperSkopeo extends WrapperTool {

    @Value("${" + KEY_PDS_PREPARE_AUTHENTICATION_FILE_SKOPEO + ":authentication.json}")
    String pdsPrepareAuthenticationFileSkopeo;

    @Autowired
    PDSProcessAdapterFactory processAdapterFactory;

    public void download(SkopeoContext context) throws IOException {
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
    public void cleanUploadDirectory(Path uploadDirectory) throws IOException {
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
        ProcessAdapter process = null;

        try {
            process = processAdapterFactory.startProcess(builder);
        } catch (IOException e) {
            throw new IOException("Error while login with Skopeo to: " + context.getLocation(), e);
        }

        waitForProcessToFinish(process);
    }

    private ProcessBuilder buildProcessLogin(SkopeoContext context) {
        List<String> commands = new ArrayList<>();

        String location = transformLocationForLogin(context.getLocation());
        File downloadDirectory = context.getToolDownloadDirectory().toFile();

        commands.add("skopeo");
        commands.add("login");
        commands.add(location);
        commands.add("--username");
        commands.add(CryptoAccess.CRYPTO_STRING.unseal(context.getCredentialMap().get(PDS_PREPARE_CREDENTIAL_USERNAME)));
        commands.add("--password");
        commands.add(CryptoAccess.CRYPTO_STRING.unseal(context.getCredentialMap().get(PDS_PREPARE_CREDENTIAL_PASSWORD)));
        commands.add("--authfile");
        commands.add(pdsPrepareAuthenticationFileSkopeo);

        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.directory(downloadDirectory);
        builder.inheritIO();

        return builder;
    }

    private ProcessBuilder buildProcessDownload(SkopeoContext context) {
        List<String> commands = new ArrayList<>();

        String location = transformLocationForDownload(context.getLocation());
        File downloadDirectory = context.getToolDownloadDirectory().toFile();

        commands.add("skopeo");
        commands.add("copy");
        commands.add(location);
        commands.add("docker-archive:" + context.getDownloadTarFilename());
        if (!context.getCredentialMap().isEmpty()) {
            commands.add("--authfile");
            commands.add(pdsPrepareAuthenticationFileSkopeo);
        }

        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.directory(downloadDirectory);
        builder.inheritIO();

        return builder;
    }

    private ProcessBuilder buildProcessClean(Path skopeoDownloadDirectory) {
        // removes authentication file
        List<String> commands = new ArrayList<>();

        commands.add("rm");
        commands.add("-rf");
        commands.add(pdsPrepareAuthenticationFileSkopeo);

        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.directory(skopeoDownloadDirectory.toFile());
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
