package com.mercedesbenz.sechub.wrapper.prepare.moduls;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteCredentialUserData;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteDataConfiguration;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperRemoteConfigurationExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class PrepareWRapperGitModule implements PrepareWrapperModule{

    // git clone https://username:password@github.com/repo/repository.git
    // 1. git@github.com:mercedes-benz/sechub.git
    // 2. https://github.com/mercedes-benz/sechub.git
    // 3. github.com/example/repo.git

    private static final String GIT_COMMAND = "git clone";

    private static final String GIT_PATTERN = "((git|ssh|http(s)?)|(git@[\\w\\.]+))(:(//)?)([\\w\\.@\\:/\\-~]+)(\\.git)(/)?";

    @Autowired
    PrepareWrapperRemoteConfigurationExtractor extractor;

    public boolean isAbleToPrepare(SecHubConfigurationModel model){
        Pattern gitPattern = Pattern.compile(GIT_PATTERN);
        for (SecHubRemoteDataConfiguration secHubRemoteDataConfiguration : extractor.extractRemoteConfiguration(model)) {
            String location = secHubRemoteDataConfiguration.getLocation();
            if (gitPattern.matcher(location).matches()) {
                return true;
            }
            // TODO: 12.04.24 laura do we really need the type?
            if (secHubRemoteDataConfiguration.getType().contains("git")) {
                return true;
            }
        }
        return false;
    }

    public void prepare(SecHubConfigurationModel model, String folder) throws IOException {
        List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = extractor.extractRemoteConfiguration(model);
        for (SecHubRemoteDataConfiguration secHubRemoteDataConfiguration : remoteDataConfigurationList) {
            String location = secHubRemoteDataConfiguration.getLocation();
            Optional<SecHubRemoteCredentialConfiguration> credentials = secHubRemoteDataConfiguration.getCredentials();

            if (!credentials.isPresent()) {
                clonePublicRepository(location, folder);
                return;
            }

            Optional<SecHubRemoteCredentialUserData> user = credentials.get().getUser();
            if (user.isPresent()){
                String username = user.get().getName();
                String password = user.get().getPassword();
                if (username == null || password == null){
                    throw new IllegalStateException("No username or password found for User: " + user);
                }
                clonePrivateRepositoryWithUser(location, username, password, folder);
            }

            throw new IllegalStateException("Credentials were empty for location: " + location);
        }
    }

    private void clonePrivateRepositoryWithUser(String location, String username, String password, String folder) throws IOException {

        String preparedLocation = "";
        if (location.contains("https://")){
            preparedLocation = location.replace("https://", "https://" + username + ":" + password + "@");
        } else if (location.contains("git@")){
            preparedLocation = location.replace("git@", "https://" + username + ":" + password + "@");
        } else if (location.contains("ssh:")) {
            preparedLocation = location.replace("ssh:", "https://" + username + ":" + password + "@");
        }
        Runtime.getRuntime().exec(GIT_COMMAND + " " + preparedLocation + " " + folder);
    }

    private void clonePublicRepository(String location, String folder) throws IOException {
        Runtime.getRuntime().exec(GIT_COMMAND + " " + location + " " + folder);
    }
}
