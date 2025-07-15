import * as vscode from 'vscode';
import {
  Configuration,
  DefaultApiClient,
  ProjectData,
  UserListsJobsForProjectRequest,
  SecHubJobInfoForUserListPage,
  UserDetailInformation,
  UserDownloadJobReportRequest,
  SecHubReport,
} from 'sechub-openapi-ts-client';
import { SECHUB_CREDENTIAL_KEYS } from '../utils/sechubConstants';

export class DefaultClient {
  private static instance: DefaultClient | null = null;
  private apiClient: DefaultApiClient;

  private constructor(apiClient: DefaultApiClient) {
    this.apiClient = apiClient;
  }

  public static async getInstance(context: vscode.ExtensionContext): Promise<DefaultClient> {
    if (!DefaultClient.instance) {
      const apiClient = await DefaultClient.createApiClient(context);
      DefaultClient.instance = new DefaultClient(apiClient);
    }
    return DefaultClient.instance;
  }

  public static async createClient(context: vscode.ExtensionContext): Promise<void> {
    const apiClient = await DefaultClient.createApiClient(context);
    const instance = await DefaultClient.getInstance(context);
    instance.apiClient = apiClient;
    vscode.window.showInformationMessage('SecHub client updated successfully.');
  }

  // Creates a new ApiClient instance with the current credentials and server URL loaded from the extension context storage
  private static async createApiClient(context: vscode.ExtensionContext): Promise<DefaultApiClient> {
    const [username, apiToken] = await Promise.all([
      context.secrets.get(SECHUB_CREDENTIAL_KEYS.username),
      context.secrets.get(SECHUB_CREDENTIAL_KEYS.apiToken),
    ]);

    const serverUrl = context.globalState.get<string>(SECHUB_CREDENTIAL_KEYS.serverUrl);

    if (!serverUrl || !username || !apiToken) {
      vscode.window.showErrorMessage('SecHub credentials are not set. Please configure them first.');
      throw new Error('SecHub client is not initialized yet. Please ensure credentials are set.');
    }

    const clientConfig = new Configuration({
      basePath: serverUrl,
      username: username,
      password: apiToken,
      headers: {
        // eslint-disable-next-line @typescript-eslint/naming-convention
        'Content-Type': 'application/json',
      },
    });

    return new DefaultApiClient(clientConfig);
  }

  public getApiClient(): DefaultApiClient {
    return this.apiClient;
  }

  public async getAssignedProjectDataList(): Promise<ProjectData[]> {
    try {
        const response: ProjectData[] = await this.apiClient.withProjectAdministrationApi().getAssignedProjectDataList();
        return response;
    } catch (error) {
        console.error('Error fetching projects:', error);
        vscode.window.showErrorMessage('Failed to fetch projects from the server.');
        return [];
    }    
  }

  public async userListsJobsForProject(projectId: string, requestParameter: UserListsJobsForProjectRequest = {
      projectId: projectId,
      size: "10", // Example size, adjust as needed
      page: "0", // Example page number, adjust as needed
    }): Promise<SecHubJobInfoForUserListPage> {

    try {
      const response: SecHubJobInfoForUserListPage = await this.apiClient.withOtherApi().userListsJobsForProject(requestParameter);
      return response;
    } catch (error) {
      console.error('Error fetching latest jobs:', error);
      vscode.window.showErrorMessage('Failed to fetch latest jobs from the server.');
      return {};
    }
  }

  public async userFetchUserDetailInformation(): Promise<UserDetailInformation> {
    try {
      const response: UserDetailInformation = await this.apiClient.withUserSelfServiceApi().userFetchUserDetailInformation();
      return response;
    } catch (error) {
      console.error('Error fetching user details:', error);
      vscode.window.showErrorMessage('Failed to fetch user details from the server.');
      return {};
    }
  }

  public async fetchReport(projectId:string, jobUUID: string): Promise<SecHubReport> {

    const requestParameter: UserDownloadJobReportRequest = {
      projectId: projectId,
      jobUUID: jobUUID,
    };

    try {
      const response = await this.apiClient.withSecHubExecutionApi().userDownloadJobReport(requestParameter);
      return response;
    } catch (error) {
      console.error('Error fetching report:', error);
      vscode.window.showErrorMessage('Failed to fetch report from the server.');
      return {};
    }
  }
}