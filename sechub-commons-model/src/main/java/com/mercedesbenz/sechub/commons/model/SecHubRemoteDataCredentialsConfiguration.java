package com.mercedesbenz.sechub.commons.model;

<<<<<<< HEAD
import java.util.Optional;

=======
>>>>>>> 9b452d59ffb4af49a30c0d7a560a034ffa110cc2
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// TODO: 04.03.24 lbottne credentials im team klären

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubRemoteDataCredentialsConfiguration {
    public static final String PROPERTY_USER = "user";

    private SecHubRemoteDataUserConfiguration user;

<<<<<<< HEAD
    public void setUser(SecHubRemoteDataUserConfiguration user) {
        this.user = user;
    }

    public Optional<SecHubRemoteDataUserConfiguration> getUser() {
        return Optional.ofNullable(user);
    }
=======
    // TODO: 04.03.24 getter

>>>>>>> 9b452d59ffb4af49a30c0d7a560a034ffa110cc2
}
