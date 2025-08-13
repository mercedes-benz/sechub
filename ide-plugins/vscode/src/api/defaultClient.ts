// SPDX-License-Identifier: MIT
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
	FalsePositiveProjectConfiguration,
	FalsePositives,
	UserMarkFalsePositivesRequest,
	UserRequestFindingExplanationRequest,
	SecHubExplanationResponse,
} from 'sechub-openapi-ts-client';
import { SECHUB_API_CLIENT_CONFIG_KEYS } from '../utils/sechubConstants';

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
	}

	// Creates a new ApiClient instance with the current credentials and server URL loaded from the extension context storage
	private static async createApiClient(context: vscode.ExtensionContext): Promise<DefaultApiClient> {
		const [username, apiToken] = await Promise.all([
			context.secrets.get(SECHUB_API_CLIENT_CONFIG_KEYS.username),
			context.secrets.get(SECHUB_API_CLIENT_CONFIG_KEYS.apiToken),
		]);

		const serverUrl = context.globalState.get<string>(SECHUB_API_CLIENT_CONFIG_KEYS.serverUrl);

		if (!serverUrl || !username || !apiToken) {
			vscode.window.showErrorMessage(
				'SecHub credentials are not set. Please configure them to connect to the SecHub server.',
			);
			console.error('SecHub credentials are not set to create an API client. Createing empty client.');
			return new DefaultApiClient(new Configuration());
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

	public async getAssignedProjectDataList(): Promise<ProjectData[] | undefined> {
		try {
			const response: ProjectData[] = await this.apiClient
				.withProjectAdministrationApi()
				.getAssignedProjectDataList();
			return response;
		} catch (error) {
			console.error('Error fetching projects:', error);
			return undefined;
		}
	}

	public async userListsJobsForProject(
		projectId: string,
		requestParameter: UserListsJobsForProjectRequest = {
			projectId: projectId,
			size: '10',
			page: '0',
		},
	): Promise<SecHubJobInfoForUserListPage> {
		try {
			const response: SecHubJobInfoForUserListPage = await this.apiClient
				.withOtherApi()
				.userListsJobsForProject(requestParameter);
			return response;
		} catch (error) {
			console.error('Error fetching latest jobs:', error);
			return {};
		}
	}

	public async userFetchUserDetailInformation(): Promise<UserDetailInformation | undefined> {
		try {
			const response: UserDetailInformation = await this.apiClient
				.withUserSelfServiceApi()
				.userFetchUserDetailInformation();
			return response;
		} catch (error) {
			console.error('Error: could not fetch User Details from Server', error);
			return undefined;
		}
	}

	public async fetchReport(projectId: string, jobUUID: string): Promise<SecHubReport | undefined> {
		const requestParameter: UserDownloadJobReportRequest = {
			projectId: projectId,
			jobUUID: jobUUID,
		};

		try {
			const response = await this.apiClient.withSecHubExecutionApi().userDownloadJobReport(requestParameter);
			return response;
		} catch (error) {
			console.error('Error fetching report:', error);
			return undefined;
		}
	}

	public async userFetchFalsePositiveConfigurationOfProject(
		projectId: string,
	): Promise<FalsePositiveProjectConfiguration | undefined> {
		const requestParameter = {
			projectId: projectId,
		};

		try {
			const response = await this.apiClient
				.withExecutionApi()
				.userFetchFalsePositiveConfigurationOfProject(requestParameter);
			return response;
		} catch (error) {
			console.error('Error fetching false positives project configuration:', error);
			return undefined;
		}
	}

	public async markFalsePositivesForProject(falsePositves: FalsePositives, projectId: string): Promise<void> {
		const requestParameter: UserMarkFalsePositivesRequest = {
			projectId: projectId,
			falsePositives: falsePositves,
		};

		try {
			await this.apiClient.withExecutionApi().userMarkFalsePositives(requestParameter);
		} catch (error) {
			console.error('Error marking findings as false positive:', error);
			throw error;
		}
	}

	public async isAlive(): Promise<void> {
		try {
			await this.apiClient.withSystemApi().anonymousCheckAliveGet();
		} catch (error) {
			console.error('Error client is not alive!', error);
			throw error;
		}
	}

	public async userRequestFindingExplanation(
		projectId: string,
		jobUUID: string,
		findingId: number,
	): Promise<SecHubExplanationResponse> {
		const requestParameter: UserRequestFindingExplanationRequest = {
			projectId,
			jobUUID,
			findingId,
		};

		try {
			const resposne = await this.apiClient.withAssistantApi().userRequestFindingExplanation(requestParameter);
			console.log('AI explanation response:', resposne);
			return resposne;
		} catch (error) {
			console.error('Error requesting AI explanation:', error);
			throw error;
		}
	}
}
