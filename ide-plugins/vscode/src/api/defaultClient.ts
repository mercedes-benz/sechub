import * as vscode from 'vscode';
import {
  ConfigurationApi,
  JobManagementApi,
  OtherApi,
  ProjectAdministrationApi,
  SecHubExecutionApi,
  SecHubExecutionApiWorkaround,
  SignUpApi,
  SystemApi,
  UserSelfServiceApi,
  Configuration,
} from 'sechub-openapi-ts-client';
import { SECHUB_CREDENTIAL_KEYS } from '../utils/sechubConstants';

export interface DefaultClient {
  withProjectApi: ProjectAdministrationApi,
  withSignUpApi: SignUpApi,
  withUserSelfServiceApi: UserSelfServiceApi,
  withSystemApi: SystemApi,
  withConfigurationApi: ConfigurationApi,
  withOtherApi: OtherApi,
  withExecutionApi: SecHubExecutionApiWorkaround,
  withSechubExecutionApi: SecHubExecutionApi,
  withJobManagementApi: JobManagementApi,
}

let clientInstance: DefaultClient | null = null;

export async function getDefaultClient(context: vscode.ExtensionContext): Promise<DefaultClient> {
    if (clientInstance) {
        return clientInstance;
    }

    const username = context.globalState.get<string>(SECHUB_CREDENTIAL_KEYS.username);
    const password = context.globalState.get<string>(SECHUB_CREDENTIAL_KEYS.apiToken);
    const serverUrl = context.globalState.get<string>(SECHUB_CREDENTIAL_KEYS.serverUrl);

    if (!username || !password || !serverUrl) {
        throw new Error('API credentials or URL are not set.');
    }

    const clientConfig = new Configuration({
        basePath: serverUrl,
        username: username,
        password: password,
        headers: {
            'Content-Type': 'application/json',
        },
    });

    clientInstance = {
        withProjectApi: new ProjectAdministrationApi(clientConfig),
        withOtherApi: new OtherApi(clientConfig),
        withSignUpApi: new SignUpApi(clientConfig),
        withUserSelfServiceApi: new UserSelfServiceApi(clientConfig),
        withSystemApi: new SystemApi(clientConfig),
        withConfigurationApi: new ConfigurationApi(clientConfig),
        withExecutionApi: new SecHubExecutionApiWorkaround(clientConfig),
        withSechubExecutionApi: new SecHubExecutionApi(clientConfig),
        withJobManagementApi: new JobManagementApi(clientConfig),
    };

    return clientInstance;
}