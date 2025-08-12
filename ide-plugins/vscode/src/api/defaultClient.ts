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

	public async explainByAiMock() {
		const explanation = {
			findingExplanation: {
				title: 'Absolute Path Traversal Vulnerability',
				content:
					"This finding indicates an 'Absolute Path Traversal' vulnerability in the `AsciidocGenerator.java` file. The application constructs a file path using user-supplied input (`args[0]`) without proper validation. An attacker could provide an absolute path (e.g., `/etc/passwd` on Linux or `C:\\Windows\\System32\\drivers\\etc\\hosts` on Windows) as input, allowing them to access arbitrary files on the system, potentially bypassing intended security restrictions [3, 7].",
			},
			potentialImpact: {
				title: 'Potential Impact',
				content:
					'If exploited, this vulnerability could allow an attacker to read sensitive files on the server, including configuration files, source code, or even password files. This could lead to information disclosure, privilege escalation, or other malicious activities [1, 5].',
			},
			recommendations: [
				{
					title: 'Validate and Sanitize User Input',
					content:
						'Always validate and sanitize user-supplied input before using it to construct file paths. In this case, ensure that the `path` variable does not contain an absolute path. You can check if the path starts with a drive letter (e.g., `C:\\`) on Windows or a forward slash (`/`) on Unix-like systems [1].',
				},
				{
					title: 'Use Relative Paths and a Base Directory',
					content:
						"Instead of allowing absolute paths, restrict user input to relative paths within a designated base directory. Construct the full file path by combining the base directory with the user-provided relative path. This limits the attacker's ability to access files outside the intended directory [1].",
				},
				{
					title: 'Normalize the Path',
					content:
						'Normalize the constructed file path to remove any directory traversal sequences (e.g., `../`). This can be achieved using the `java.nio.file.Path.normalize()` method. After normalization, verify that the path still resides within the allowed base directory [1, 6].',
				},
			],
			codeExample: {
				vulnerableExample:
					'public static void main(String[] args) throws Exception {\n  String path = args[0];\n  File documentsGenFolder = new File(path);\n  //Potentially dangerous operation with documentsGenFolder\n}',
				secureExample:
					'public static void main(String[] args) throws Exception {\n  String basePath = "/safe/base/directory";\n  String userPath = args[0];\n\n  // Validate that userPath is not an absolute path\n  if (new File(userPath).isAbsolute()) {\n    System.err.println("Error: Absolute paths are not allowed.");\n    return;\n  }\n\n  Path combinedPath = Paths.get(basePath, userPath).normalize();\n\n  // Ensure the combined path is still within the base directory\n  if (!combinedPath.startsWith(basePath)) {\n    System.err.println("Error: Path traversal detected.");\n    return;\n  }\n\n  File documentsGenFolder = combinedPath.toFile();\n  //Safe operation with documentsGenFolder\n}',
				explanation: {
					title: 'Code Example Explanation',
					content:
						'The vulnerable example directly uses user-provided input to create a `File` object, allowing an attacker to specify an arbitrary file path. The secure example first defines a base directory and combines it with the user-provided path using `Paths.get()`. It then normalizes the path and verifies that it remains within the base directory before creating the `File` object. This prevents path traversal attacks by ensuring that the application only accesses files within the intended directory [2, 6].',
				},
			},
			references: [
				{
					title: 'OWASP Path Traversal',
					content: 'https://owasp.org/www-community/attacks/Path_Traversal',
				},
				{
					title: "CWE-22: Improper Limitation of a Pathname to a Restricted Directory ('Path Traversal')",
					content: 'https://cwe.mitre.org/data  import { RouteParams } from /definitions/22.html',
				},
				{
					title: 'Snyk Path Traversal',
					content: 'https://snyk.io/learn/path-traversal/',
				},
			],
		};
		return explanation;
	}
}
