// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules.git;

import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironmentVariables.PDS_PREPARE_CREDENTIAL_PASSWORD;
import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironmentVariables.PDS_PREPARE_CREDENTIAL_USERNAME;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import javax.crypto.SealedObject;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;

@Component
public class JGitAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(JGitAdapter.class);

    public void clone(GitContext gitContext) {
        String location = transformLocationToURL(gitContext.getLocation());
        Map<String, SealedObject> credentialMap = gitContext.getCredentialMap();

        String username = getUserNameFromMap(credentialMap);
        String password = getPasswordFromMap(credentialMap);

        CloneCommand command = Git.cloneRepository().setURI(location).setDirectory(Paths.get(gitContext.getUploadDirectory()).toFile());

        if (username != null && password != null) {
            LOG.debug("Cloning private repository: " + location + " with username and password to: " + gitContext.getUploadDirectory());
            command = command.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));
        } else {
            LOG.debug("Cloning public repository: " + location + " to: " + gitContext.getUploadDirectory());
        }

        if (gitContext.isCloneWithoutHistory()) {
            LOG.debug("Cloning repository without history");
            command = command.setDepth(1);
        }

        try (Git git = command.call()) {
        } catch (GitAPIException e) {
            throw new RuntimeException("Error while cloning from repository: " + location + " with " + username + " " + password, e);
        }
    }

    private String getUserNameFromMap(Map<String, SealedObject> credentialMap) {
        return CryptoAccess.CRYPTO_STRING.unseal(credentialMap.get(PDS_PREPARE_CREDENTIAL_USERNAME));
    }

    private String getPasswordFromMap(Map<String, SealedObject> credentialMap) {
        return CryptoAccess.CRYPTO_STRING.unseal(credentialMap.get(PDS_PREPARE_CREDENTIAL_PASSWORD));
    }

    private String transformLocationToURL(String location) {

        if (location.startsWith("https://")) {
            return location;
        }

        /* clone with password and username does only work with URL */
        String URLPrefix = "https://";
        List<String> prefixes = List.of("git@", "git://", "http://", "ssh://");
        for (String prefix : prefixes) {
            if (location.startsWith(prefix)) {
                if (prefix.equals("git@")) {
                    location = location.replace(":", "/");
                }
                location = location.replace(prefix, URLPrefix);
                break;
            }
        }
        try {
            new java.net.URL(location);
        } catch (java.net.MalformedURLException e) {
            throw new IllegalArgumentException("Location is not a valid URL: " + location);
        }
        return location;
    }
}