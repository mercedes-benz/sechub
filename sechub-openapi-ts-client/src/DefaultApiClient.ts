// SPDX-License-Identifier: MIT
import { Configuration,
    OtherApi,
    SystemApi,
    EncryptionApi,
    JobAdministrationApi,
    JobManagementApi,
    ProjectAdministrationApi,
    SecHubExecutionApi,
    SignUpApi,
    UserAdministrationApi,
    UserSelfServiceApi,
    ConfigurationApi
 } from "../gen";
import { SecHubExecutionApiWorkaround } from "./executionService/executionService";

export class DefaultApiClient {

    private otherApi: OtherApi;
    private systemApi: SystemApi;
    private encryptionApi: EncryptionApi;
    private jobAdministrationApi: JobAdministrationApi;
    private jobManagementApi: JobManagementApi;
    private projectAdministrationApi: ProjectAdministrationApi;
    private sechubExecutionApi: SecHubExecutionApi;
    private signUpApi: SignUpApi;
    private userAdministrationApi: UserAdministrationApi;
    private userSelfServiceApi: UserSelfServiceApi;
    private configurationApi: ConfigurationApi;
    private executionApi: SecHubExecutionApiWorkaround;

    constructor(protected apiConfig: Configuration) {
        this.otherApi = new OtherApi(apiConfig);
        this.systemApi = new SystemApi(apiConfig);
        this.encryptionApi = new EncryptionApi(apiConfig);
        this.jobAdministrationApi = new JobAdministrationApi(apiConfig);
        this.jobManagementApi = new JobManagementApi(apiConfig);
        this.projectAdministrationApi = new ProjectAdministrationApi(apiConfig);
        this.sechubExecutionApi = new SecHubExecutionApi(apiConfig);
        this.signUpApi = new SignUpApi(apiConfig);
        this.userAdministrationApi = new UserAdministrationApi(apiConfig);
        this.userSelfServiceApi = new UserSelfServiceApi(apiConfig);
        this.configurationApi = new ConfigurationApi(apiConfig);
        this.executionApi = new SecHubExecutionApiWorkaround(apiConfig);
    }

    public withOtherApi(): OtherApi {
        return this.otherApi;
    }

    public withSystemApi(): SystemApi {
        return this.systemApi;
    }

    public withEncryptionApi(): EncryptionApi {
        return this.encryptionApi;
    }

    public withJobAdministrationApi(): JobAdministrationApi {
        return this.jobAdministrationApi;
    }

    public withJobManagementApi(): JobManagementApi {
        return this.jobManagementApi;
    }

    public withProjectAdministrationApi(): ProjectAdministrationApi {
        return this.projectAdministrationApi;
    }

    public withSecHubExecutionApi(): SecHubExecutionApi {
        return this.sechubExecutionApi;
    }

    public withSignUpApi(): SignUpApi {
        return this.signUpApi;
    }

    public withUserAdministrationApi(): UserAdministrationApi {
        return this.userAdministrationApi;
    }

    public withUserSelfServiceApi(): UserSelfServiceApi {
        return this.userSelfServiceApi;
    }

    public withConfigurationApi(): ConfigurationApi {
        return this.configurationApi;
    }

    public withExecutionApi(): SecHubExecutionApiWorkaround {
        return this.executionApi;
    }
}