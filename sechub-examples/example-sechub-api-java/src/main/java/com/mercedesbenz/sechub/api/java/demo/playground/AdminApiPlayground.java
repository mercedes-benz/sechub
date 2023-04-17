package com.mercedesbenz.sechub.api.java.demo.playground;

import static com.mercedesbenz.sechub.api.java.demo.Utils.*;

import java.math.BigDecimal;
import java.util.List;

import com.mercedesbenz.sechub.api.java.AdminApi;
import com.mercedesbenz.sechub.api.java.ApiException;
import com.mercedesbenz.sechub.api.java.SecHubAccess;
import com.mercedesbenz.sechub.api.java.model.ExecutionProfileCreate;
import com.mercedesbenz.sechub.api.java.model.ExecutorConfiguration;
import com.mercedesbenz.sechub.api.java.model.ExecutorConfigurationSetup;
import com.mercedesbenz.sechub.api.java.model.ListOfSignupsInner;
import com.mercedesbenz.sechub.api.java.model.Project;
import com.mercedesbenz.sechub.api.java.model.UserSignup;

public class AdminApiPlayground {
    SecHubAccess access;
    long identifier = System.currentTimeMillis();
    private String userName;
    private AdminApi adminApi;
    private String projectName;
    
    public AdminApiPlayground(SecHubAccess access) {
        userName = "un" + identifier;
        projectName = "pn" + identifier;
        
        this.access=access;
        this.adminApi = access.getAdminApi();
    }

    public void run()  throws ApiException {
        logTitle("Start testing admin API");
        
//        Object result = adminApi.adminChecksServerVersion();
//        assumeEquals("0.0.0", result, "Check server version");

        signupNewUser();
        checkUserSignupListForNewUser();

        acceptUserAndCheckListedAsUser();

        createProjectAndCheckFoundInList();

        
        /* create an executor configuration */
        ExecutorConfigurationSetup setup = new ExecutorConfigurationSetup();
        setup.setBaseURL("https://example.com:8443");
        
        ExecutorConfiguration configuration = new ExecutorConfiguration();
        configuration.setEnabled(false);
        configuration.setExecutorVersion(BigDecimal.valueOf(1));
        configuration.setName("PDS_TEST1");
        configuration.setProductIdentifier("PDS_CODESCAN");
        
        configuration.setSetup(setup);
        
        /* FIXME Albert Tregnaghi, 2023-04-17: the api wantes a JSON result but it isn't - same problem as for adminChecksServerVersion*/
        Object result = adminApi.adminCreatesExecutorConfiguration(configuration);
        System.out.println("result="+result);
        
        /* create a profile */
        String profileName = "profile" +identifier;
        ExecutionProfileCreate create = new ExecutionProfileCreate();
        adminApi.adminCreatesExecutionProfile(profileName, create);
        
    }

    private void createProjectAndCheckFoundInList() throws ApiException {
        Project project = new Project();
        project.setOwner(userName);
        project.setName(projectName);
        project.setApiVersion("1.0");
        project.setDescription("description1");

        adminApi.adminCreatesProject(project);
        logSuccess("Project " + projectName + " created");

        List<String> projects = adminApi.adminListsAllProjects();
        assumeEquals(true, projects.contains(projectName), "Project " + projectName + " was found in list");
    }

    private void acceptUserAndCheckListedAsUser() throws ApiException {
        adminApi.adminAcceptsSignup(userName);
        waitMilliseconds(300);
        
        List<String> usersList = adminApi.adminListsAllUsers();
        logSuccess("List of users has entries: " + usersList.size());
       
        assumeEquals(true, usersList.contains(userName), "Accepted user is found in user list after signup");
    }

    private void checkUserSignupListForNewUser() throws ApiException {
        List<ListOfSignupsInner> waitingSignups = adminApi.adminListsOpenUserSignups();
        boolean foundUserSignup=false;
        for (ListOfSignupsInner signup: waitingSignups) {
            foundUserSignup = signup.getUserId().equals(userName);
            if (foundUserSignup) {
                break;
            }
        }
        assumeEquals(true, foundUserSignup, "Signup for user: " + userName + " was found in list of signups");
    }

    private void signupNewUser() throws ApiException {
        UserSignup signUp = new UserSignup();
        signUp.setApiVersion("1.0");
        signUp.setEmailAdress(userName+"@example.com");
        signUp.setUserId(userName);
        access.getAnonymousApi().userSignup(signUp);
        
        waitMilliseconds(300);
    }

}
