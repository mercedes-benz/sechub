package com.mercedesbenz.sechub.commons.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubRemoteDataUserConfiguration {
<<<<<<< HEAD

    public static final String PROPERTY_USER = "name";
    public static final String PROPERTY_PASSWORD = "password";

    private String name;

    private String password;

    public String getName() {
        return name;
=======
    public static final String PROPERTY_REMOTE_NAME = "name";
    public static final String PROPERTY_REMOTE_PASSWORD = "password";

    private String user;

    private String password;

    public String getUser() {
        return user;
>>>>>>> 9b452d59ffb4af49a30c0d7a560a034ffa110cc2
    }

    public String getPassword() {
        return password;
    }
}
