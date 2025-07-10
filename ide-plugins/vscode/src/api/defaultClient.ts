import * as vscode from 'vscode';
import {
  Configuration,
  DefaultApiClient,
} from 'sechub-openapi-ts-client';
import { SECHUB_CREDENTIAL_KEYS } from '../utils/sechubConstants';

export class DefaultClient {
  private static instance: DefaultClient | null = null;
  private apiClient: DefaultApiClient;

  private constructor(apiClient: DefaultApiClient) {
    this.apiClient = apiClient;
  }

  public static async initialize(context: vscode.ExtensionContext): Promise<DefaultClient> {
    if (DefaultClient.instance) {
      return DefaultClient.instance;
    }

    const apiClient = await DefaultClient.createApiClient(context);
    DefaultClient.instance = new DefaultClient(apiClient);
    vscode.window.showInformationMessage('SecHub client initialized successfully.');
    return DefaultClient.instance;
  }

  public static async updateClient(context: vscode.ExtensionContext): Promise<void> {
    const apiClient = await DefaultClient.createApiClient(context);
    if (DefaultClient.instance) {
      DefaultClient.instance.apiClient = apiClient;
      vscode.window.showInformationMessage('SecHub client updated successfully.');
    } else {
      throw new Error('SecHub client is not initialized yet.');
    }
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

    console.log('Creating SecHub API client with configuration:', clientConfig);

    return new DefaultApiClient(clientConfig);
  }

  public getApiClient(): DefaultApiClient {
    return this.apiClient;
  }
}