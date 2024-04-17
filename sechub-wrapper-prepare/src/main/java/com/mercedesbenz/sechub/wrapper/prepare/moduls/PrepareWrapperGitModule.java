package com.mercedesbenz.sechub.wrapper.prepare.moduls;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialUserData;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteDataConfiguration;
import com.mercedesbenz.sechub.wrapper.prepare.prepare.PrepareWrapperRemoteConfigurationExtractor;

@Service
public class PrepareWrapperGitModule implements PrepareWrapperModule {

    Logger LOG = LoggerFactory.getLogger(PrepareWrapperGitModule.class);

    private static final String GIT_COMMAND = "git clone --depth 1";
    private static final String GIT_PATTERN = "((git|ssh|http(s)?)|(git@[\\w\\.]+))(:(//)?)([\\w\\.@\\:/\\-~]+)(\\.git)(/)?";
    private static final String TYPE = "git";

    @Autowired
    PrepareWrapperRemoteConfigurationExtractor extractor;

    public boolean isAbleToPrepare(SecHubConfigurationModel model) {
        Pattern gitPattern = Pattern.compile(GIT_PATTERN);
        for (SecHubRemoteDataConfiguration secHubRemoteDataConfiguration : extractor.extract(model)) {
            String location = secHubRemoteDataConfiguration.getLocation();
            // returns true when either location matches a git URL or the type is git
            if (gitPattern.matcher(location).matches()) {
                LOG.debug("Location is a git URL");
                return true;
            }
            if (TYPE.equals(secHubRemoteDataConfiguration.getType())) {
                LOG.debug("Type is git");
                return true;
            }
        }
        return false;
    }

    public void prepare(SecHubConfigurationModel model, String pdsPrepareUploadFolderDirectory) throws IOException {

        LOG.debug("Start remote data preparation for git repository");

        List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = extractor.extract(model);
        for (SecHubRemoteDataConfiguration secHubRemoteDataConfiguration : remoteDataConfigurationList) {
            String location = secHubRemoteDataConfiguration.getLocation();
            Optional<SecHubRemoteCredentialConfiguration> credentials = secHubRemoteDataConfiguration.getCredentials();

            if (!credentials.isPresent()) {
                // public repository does not need credentials
                cloneRepository(location, pdsPrepareUploadFolderDirectory);
                return;
            }

            Optional<SecHubRemoteCredentialUserData> optUser = credentials.get().getUser();
            if (optUser.isPresent()) {
                SecHubRemoteCredentialUserData user = optUser.get();
                String username = user.getName();
                String password = user.getPassword();
                if (username == null || password == null) {
                    throw new IllegalStateException("No username or password found for User: " + user);
                }
                String preparedLocation = prepareLocationForPrivateRepo(location, username, password);
                cloneRepository(preparedLocation, pdsPrepareUploadFolderDirectory);
                return;
            }

            throw new IllegalStateException("Defined credentials were empty for location: " + location);
        }
    }

    public void cleanDirectory(String pdsPrepareUploadFolderDirectory) throws IOException {

        LOG.debug("Clean PDS upload pdsPrepareUploadFolderDirectory from .git specific files");

        Runtime.getRuntime().exec("rm -rf " + pdsPrepareUploadFolderDirectory + "*/.git*");
    }

    String prepareLocationForPrivateRepo(String location, String username, String password) {

        LOG.debug("Prepare location string for private repository");

        String preparedLocation;
        if (location.contains("https://")) {
            preparedLocation = location.replace("https://", "https://" + username + ":" + password + "@");
        } else if (location.contains("git@")) {
            preparedLocation = location.replace("git@", "https://" + username + ":" + password + "@");
        } else if (location.contains("ssh://")) {
            preparedLocation = location.replace("ssh://", "https://" + username + ":" + password + "@");
        } else if (location.contains("http://")) {
            preparedLocation = location.replace("http://", "https://" + username + ":" + password + "@");
        } else if (location.contains("git://")) {
            preparedLocation = location.replace("git://", "https://" + username + ":" + password + "@");
        } else {
            preparedLocation = "https://" + username + ":" + password + "@" + location;
        }

        return preparedLocation;
    }

    void cloneRepository(String location, String folder) throws IOException {

        LOG.debug("Start cloning repository");

        validateLocationURL(location);
        Runtime.getRuntime().exec(GIT_COMMAND + " " + location + " " + folder);
    }

    void validateLocationURL(String location) {

        LOG.debug("Validate location URL");

        try {
            new URL(location).toURI();
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid URL, location was not a valid URL", e);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid URL, location has not a valid Syntax", e);
        }
    }
}
