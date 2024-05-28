package com.mercedesbenz.sechub.wrapper.prepare.modules.git;

import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironmentVariables.PDS_PREPARE_CREDENTIAL_PASSWORD;
import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironmentVariables.PDS_PREPARE_CREDENTIAL_USERNAME;
import static com.mercedesbenz.sechub.wrapper.prepare.modules.UsageExceptionExitCode.GIT_CLONING_FAILED;
import static com.mercedesbenz.sechub.wrapper.prepare.modules.UsageExceptionExitCode.LOCATION_URL_NOT_VALID_URL;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import javax.crypto.SealedObject;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
import com.mercedesbenz.sechub.pds.commons.core.PDSLogSanitizer;
import com.mercedesbenz.sechub.wrapper.prepare.modules.PrepareWrapperUsageException;

@Component
public class JGitAdapter {

    @Autowired
    PDSLogSanitizer pdsLogSanitizer;

    private static final Logger LOG = LoggerFactory.getLogger(JGitAdapter.class);

    public void clone(GitContext gitContext) {
        String location = transformLocationToURL(gitContext.getLocation());
        Path downloadDirectory = gitContext.getToolDownloadDirectory();
        Map<String, SealedObject> credentialMap = gitContext.getCredentialMap();

        /*@formatter:off*/
        CloneCommand command = Git.cloneRepository()
                .setURI(location).
                setDirectory(downloadDirectory.resolve(Path.of(gitContext.getRepositoryName())).toFile());
        /*@formatter:on*/

        String username = getUserNameFromMap(credentialMap);
        String password = getPasswordFromMap(credentialMap);
        if (username != null && password != null) {
            LOG.debug("Cloning private repository: {} with username and password to: {} ", pdsLogSanitizer.sanitize(location, 1024), downloadDirectory);
            command = command.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));
        } else {
            LOG.debug("Cloning public repository: {} with username and password to: {} ", pdsLogSanitizer.sanitize(location, 1024), downloadDirectory);
        }

        if (gitContext.isCloneWithoutHistory()) {
            LOG.debug("Cloning repository without history");
            command = command.setDepth(1);
        }

        try (Git git = command.call()) {
        } catch (GitAPIException e) {
            LOG.error("Could not clone defined repository: {}", pdsLogSanitizer.sanitize(location, 1024), e);
            throw new PrepareWrapperUsageException("Could not clone defined repository: " + pdsLogSanitizer.sanitize(location, 1024), e, GIT_CLONING_FAILED);
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
            throw new PrepareWrapperUsageException("Location could not be transferred into a valid URL: " + pdsLogSanitizer.sanitize(location, 1024),
                    LOCATION_URL_NOT_VALID_URL);
        }
        return location;
    }
}