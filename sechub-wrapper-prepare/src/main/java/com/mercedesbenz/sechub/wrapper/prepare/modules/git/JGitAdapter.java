// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules.git;

import static com.mercedesbenz.sechub.wrapper.prepare.modules.UsageExceptionExitCode.*;

import java.net.URL;
import java.nio.file.Path;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.pds.commons.core.PDSLogSanitizer;
import com.mercedesbenz.sechub.wrapper.prepare.PrepareWrapperUsageException;

@Component
public class JGitAdapter {

    private final PDSLogSanitizer logSanitizer;

    private final GitLocationConverter urlConverter;

    private static final Logger LOG = LoggerFactory.getLogger(JGitAdapter.class);

    public JGitAdapter(PDSLogSanitizer logSanitizer, GitLocationConverter urlConverter) {
        this.logSanitizer = logSanitizer;
        this.urlConverter = urlConverter;
    }

    public void clone(GitContext gitContext) {

        String location = gitContext.getLocation();
        String sanitizedLocation = logSanitizer.sanitize(location, 1024);

        LOG.info("Start cloning location: {}", sanitizedLocation);

        URL url = urlConverter.convertLocationToHttpsBasedURL(location);
        Path downloadDirectory = gitContext.getToolDownloadDirectory();
        Path repository = Path.of(gitContext.getRepositoryName());

        LOG.debug("Git clone command will use repository: {}, url: {}, downloadDirectory: {}", repository, url, downloadDirectory);

        /* @formatter:off */
        CloneCommand cloneCommand = Git.cloneRepository().
                setURI(url.toExternalForm()).
                setDirectory(downloadDirectory.resolve(repository).toFile());
        /* @formatter:on */

        handleGitCredentials(gitContext, url, downloadDirectory, cloneCommand);
        handleGitHistory(gitContext, cloneCommand);

        try (Git git = cloneCommand.call()) {
        } catch (GitAPIException e) {

            throw new PrepareWrapperUsageException("Could not clone defined repository: " + url, e, GIT_CLONING_FAILED);
        }
    }

    private void handleGitHistory(GitContext gitContext, CloneCommand cloneCommand) {
        if (gitContext.isCloneWithoutHistory()) {
            LOG.debug("Cloning repository without history");
            cloneCommand.setDepth(1);
        }
    }

    private void handleGitCredentials(GitContext gitContext, URL url, Path downloadDirectory, CloneCommand cloneCommand) {
        if (gitContext.hasCredentials()) {

            String username = gitContext.getUnsealedUsername();
            String password = gitContext.getUnsealedPassword();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Cloning private repository: {} with username and password to: {} ", logSanitizer.sanitize(url, 1024), downloadDirectory);
            }

            cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));

        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cloning public repository: {} with username and password to: {} ", logSanitizer.sanitize(url, 1024), downloadDirectory);
            }
        }
    }

}