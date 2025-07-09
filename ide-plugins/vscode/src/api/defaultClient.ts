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

export class DefaultClient {
  private static instance: DefaultClient | null = null;
  private clientConfig: Configuration | null = null;

  public withProjectApi: ProjectAdministrationApi;
  public withSignUpApi: SignUpApi;
  public withUserSelfServiceApi: UserSelfServiceApi;
  public withSystemApi: SystemApi;
  public withConfigurationApi: ConfigurationApi;
  public withOtherApi: OtherApi;
  public withExecutionApi: SecHubExecutionApiWorkaround;
  public withSechubExecutionApi: SecHubExecutionApi;
  public withJobManagementApi: JobManagementApi;

  private constructor(clientConfig: Configuration) {
    this.clientConfig = clientConfig;
    this.withProjectApi = new ProjectAdministrationApi(clientConfig);
    this.withOtherApi = new OtherApi(clientConfig);
    this.withSignUpApi = new SignUpApi(clientConfig);
    this.withUserSelfServiceApi = new UserSelfServiceApi(clientConfig);
    this.withSystemApi = new SystemApi(clientConfig);
    this.withConfigurationApi = new ConfigurationApi(clientConfig);
    this.withExecutionApi = new SecHubExecutionApiWorkaround(clientConfig);
    this.withSechubExecutionApi = new SecHubExecutionApi(clientConfig);
    this.withJobManagementApi = new JobManagementApi(clientConfig);
  }

  public static async getInstance(context: vscode.ExtensionContext): Promise<DefaultClient> {
    if (DefaultClient.instance) {
      return DefaultClient.instance;
    }

    const usernamePromise = context.secrets.get(SECHUB_CREDENTIAL_KEYS.username);
    const apiTokenPromise = context.secrets.get(SECHUB_CREDENTIAL_KEYS.apiToken);
    const serverUrl = context.globalState.get<string>(SECHUB_CREDENTIAL_KEYS.serverUrl);

    const [username, apiToken] = await Promise.all([usernamePromise, apiTokenPromise]);

    if (!serverUrl || !username || !apiToken) {
      vscode.window.showErrorMessage('SecHub credentials are not set. Please configure them first.');
      await vscode.commands.executeCommand('sechub.multiStepInput');
      throw new Error('SecHub client is not initialized yet. Please ensure credentials are set.');
    }

    const clientConfig = new Configuration({
      basePath: serverUrl,
      username: username,
      password: apiToken,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    DefaultClient.instance = new DefaultClient(clientConfig);
    console.log('SecHub client initialized successfully.', DefaultClient.instance);
    vscode.window.showInformationMessage('SecHub client initialized successfully.');

    return DefaultClient.instance;
  }
}