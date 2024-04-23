package com.mercedesbenz.sechub.wrapper.prepare.moduls;

import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironmentVariables.*;
import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperKeyConstants.KEY_PDS_PREPARE_AUTO_CLEANUP_GIT_FOLDER;
import static com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperKeyConstants.KEY_PDS_PREPARE_MODULE_ENABLED_GIT;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialUserData;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteDataConfiguration;
import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperContext;

@Service
public class PrepareWrapperGitModule implements PrepareWrapperModule {

    Logger LOG = LoggerFactory.getLogger(PrepareWrapperGitModule.class);

    private static final String GIT_PATTERN = "((git|http(s)?)|(git@[\\w\\.]+))(:(//)?)([\\w\\.@\\:/\\-~]+)(\\.git)(/)?";
    private static final String TYPE = "git";
    private final Pattern gitPattern = Pattern.compile(GIT_PATTERN);

    @Value("${" + KEY_PDS_PREPARE_AUTO_CLEANUP_GIT_FOLDER + ":true}")
    private boolean pdsPrepareAutoCleanupGitFolder;

    @Value("${" + KEY_PDS_PREPARE_MODULE_ENABLED_GIT + ":true}")
    private boolean pdsPrepareModuleEnabledGit;

    @Autowired
    PrepareWrapperGIT git;

    public boolean isAbleToPrepare(PrepareWrapperContext context) {
        for (SecHubRemoteDataConfiguration secHubRemoteDataConfiguration : context.getRemoteDataConfigurationList()) {
            String location = secHubRemoteDataConfiguration.getLocation();
            // returns true when either the type is git or the location matches a git URL
            if (isMatchingGitType(secHubRemoteDataConfiguration.getType())) {
                LOG.debug("Type is git");
                if (!isMatchingGitPattern(location)) {
                    LOG.warn("Type is git but location does not match git URL pattern");
                }
                return true;
            }
            if (isMatchingGitPattern(location)) {
                LOG.debug("Location is a git URL");
                return true;
            }
        }
        return false;
    }

    public void prepare(PrepareWrapperContext context) throws IOException {

        LOG.debug("Start remote data preparation for git repository");

        List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = context.getRemoteDataConfigurationList();
        for (SecHubRemoteDataConfiguration secHubRemoteDataConfiguration : remoteDataConfigurationList) {
            String location = secHubRemoteDataConfiguration.getLocation();
            Optional<SecHubRemoteCredentialConfiguration> credentials = secHubRemoteDataConfiguration.getCredentials();

            if (!credentials.isPresent()) {
                // public repository does not need credentials
                git.cloneRepository(location);
                return;
            }

            Optional<SecHubRemoteCredentialUserData> optUser = credentials.get().getUser();
            if (optUser.isPresent()) {
                SecHubRemoteCredentialUserData user = optUser.get();
                String username = user.getName();
                String password = user.getPassword();

                if (username == null || username.isEmpty()) {
                    LOG.error("No username found for User: " + user);
                    throw new IllegalStateException("No username found for User: " + user);
                }
                if (password == null || password.isEmpty()) {
                    LOG.error("No password found for User: " + user);
                    throw new IllegalStateException("No password found for User: " + user);
                }

                git.setEnvironmentVariables(PDS_PREPARE_CREDENTIAL_USERNAME, username);
                git.setEnvironmentVariables(PDS_PREPARE_CREDENTIAL_PASSWORD, password);
                git.cloneRepository(location);
                return;
            }

            LOG.error("Defined credentials have no credential user data for location: " + location);
            throw new IllegalStateException("Defined credentials have no credential user data for location: " + location);
        }
    }

    public void cleanup() throws IOException {
        git.setEnvironmentVariables(PDS_PREPARE_CREDENTIAL_USERNAME, "");
        git.setEnvironmentVariables(PDS_PREPARE_CREDENTIAL_PASSWORD, "");
        if (pdsPrepareAutoCleanupGitFolder) {
            git.cleanGitDirectory();
        }
    }

    public boolean isDownloadSuccessful(PrepareWrapperContext context) throws IOException {
        // check if download folder is not empty
        Path path = Paths.get(context.getEnvironment().getPdsPrepareUploadFolderDirectory());
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> directory = Files.newDirectoryStream(path)) {
                return !directory.iterator().hasNext();
            }
        }
        return false;
    }

    public boolean isModuleEnabled() {
        return pdsPrepareModuleEnabledGit;
    }

    boolean isMatchingGitPattern(String location) {
        if (location == null || location.isEmpty()) {
            return false;
        }
        return gitPattern.matcher(location).matches();
    }

    boolean isMatchingGitType(String type) {
        if (type == null || type.isEmpty()) {
            return false;
        }
        type = type.toLowerCase();
        return TYPE.equals(type);
    }
}
