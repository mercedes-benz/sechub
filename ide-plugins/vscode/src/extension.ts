import * as vscode from 'vscode';
import * as  https from 'https';
import * as path from 'path';
import * as fs from 'fs';
import axios from 'axios';
import { load } from 'cheerio';

export function activate(context: vscode.ExtensionContext) {
    // Register the main command
    context.subscriptions.push(
        vscode.commands.registerCommand('sechub.start', () => {
            SecHubPanel.createOrShow(context.extensionUri);
        })
    );

    // Register the webview panel serializer if supported
    if (vscode.window.registerWebviewPanelSerializer) {
        vscode.window.registerWebviewPanelSerializer(SecHubPanel.viewType, {
            async deserializeWebviewPanel(webviewPanel: vscode.WebviewPanel, state: unknown) {
                console.log(`Got state: ${state}`);
                webviewPanel.webview.options = getWebviewOptions(context.extensionUri);
                SecHubPanel.revive(webviewPanel, context.extensionUri);
            }
        });
    }

    // Function to prompt for and store credentials
    async function promptForCredentials() {
        const username = await vscode.window.showInputBox({ prompt: 'Enter your Sechub username' });
        const password = await vscode.window.showInputBox({ prompt: 'Enter your Sechub apitoken', password: true });

        if (username && password) {
            //     const secrets: SecretStorage = context.secrets;
            await context.secrets.store('sechub.username', username);
            await context.secrets.store('sechub.password', password);
            vscode.window.showInformationMessage('Sechub credentials stored securely!');
        } else {
            vscode.window.showWarningMessage('Sechub credentials not provided.');
        }
    }

    async function checkCredentials() {
        const username = await context.secrets.get('sechub.username');
        const password = await context.secrets.get('sechub.password');

        if (!username || !password) {
            await promptForCredentials();
        } else {
            vscode.window.showInformationMessage(`Sechub credentials already stored. Username: ${username}`);
        }
    }

    checkCredentials();

    context.subscriptions.push(
        vscode.commands.registerCommand('sechub.setCredentials', async () => {
            await promptForCredentials();
        })
    );
}

function getWebviewOptions(extensionUri: vscode.Uri): vscode.WebviewOptions {
    return {
        enableScripts: true,
        localResourceRoots: [vscode.Uri.joinPath(extensionUri, 'media')]
    };
}

class SecHubPanel {
    public static currentPanel: SecHubPanel | undefined;
    public static readonly viewType = 'sechub';

    private readonly _panel: vscode.WebviewPanel;
    private readonly _extensionUri: vscode.Uri;
    private _disposables: vscode.Disposable[] = [];

    public static createOrShow(extensionUri: vscode.Uri) {
        const column = vscode.window.activeTextEditor
            ? vscode.window.activeTextEditor.viewColumn
            : undefined;

        if (SecHubPanel.currentPanel) {
            SecHubPanel.currentPanel._panel.reveal(column);
            return;
        }

        const panel = vscode.window.createWebviewPanel(
            SecHubPanel.viewType,
            'SecHub Login',
            column || vscode.ViewColumn.One,
            getWebviewOptions(extensionUri),
        );

        SecHubPanel.currentPanel = new SecHubPanel(panel, extensionUri);
    }

    public static revive(panel: vscode.WebviewPanel, extensionUri: vscode.Uri) {
        SecHubPanel.currentPanel = new SecHubPanel(panel, extensionUri);
    }

    private constructor(panel: vscode.WebviewPanel, extensionUri: vscode.Uri) {
        this._panel = panel;
        this._extensionUri = extensionUri;

        this._update();

        this._panel.onDidDispose(() => this.dispose(), null, this._disposables);

        this._panel.onDidChangeViewState(
            () => {
                if (this._panel.visible) {
                    this._update();
                }
            },
            null,
            this._disposables
        );

        this._panel.webview.onDidReceiveMessage(
            message => {
                switch (message.command) {
                    case 'alert':
                        vscode.window.showErrorMessage(message.text);
                        return;
                }
            },
            null,
            this._disposables
        );
    }

    public dispose() {
        SecHubPanel.currentPanel = undefined;

        this._panel.dispose();

        while (this._disposables.length) {
            const x = this._disposables.pop();
            if (x) {
                x.dispose();
            }
        }
    }

    private async _update() {
        const webview = this._panel.webview;
        this._panel.title = 'SecHub Login';
        const theme = 'vscode';
        const redirectUri = 'http://localhost:3000';
        
        const filePath: vscode.Uri = vscode.Uri.file(path.join(this._extensionUri.fsPath, 'src', 'html', 'file.html'));
        const loginUrl = 'https://localhost:8443/login?theme=vscode&redirectUri=http://localhost:3000';
        const httpLogin = `http://localhost:8000/login?theme=${theme}&redirectUri=${redirectUri}/`;

        
        // LOGIN
        // login download and render
        // const html = await fetchAndSaveContent(loginUrl, this._extensionUri.fsPath, webview);
        // login direct
        this._panel.webview.html = this._getHtmlForWebview(httpLogin)

        // REPORT (BASIC AUTH)
        /*
        const jobUUID = 'af026a36-8e15-40b5-9bd5-0f486245f9c5'
        const interactive = true
        const theme = 'jetbrains'
        const projectId = 'test-checkmarx'
        const reportUrl = `https://localhost:8443/api/project/${projectId}/report/${jobUUID}?interactive=${interactive}&theme=${theme}`;
        const username = 'int-test_superadmin'
        const password = 'int-test_superadmin-pwd'

        const html = await fetchReport(reportUrl, username, password);

        // REPORT EVENTS
        // Attention: can only be loaded when in html (not as extra .js file!!)
        // Inject event bridge script into the HTML to catch events in vscode
        const injectedScript = `
            <script>
                window.addEventListener("START_SCAN", () => {
                    vscode.postMessage({ command: "startScan" });
                });
                window.addEventListener("GO_TO_WEB_UI", () => {
                    vscode.postMessage({ command: "goToWebUi" });
                });
                window.addEventListener("MARK_FALSE_POSITIVE", (e) => {
                    vscode.postMessage({ command: "markFalsePositive", findingId: e.detail?.findingId });
                });
                window.addEventListener("UNMARK_FALSE_POSITIVE", (e) => {
                    vscode.postMessage({ command: "unmarkFalsePositive", findingId: e.detail?.findingId });
                });
                window.addEventListener("JUMP_TO_LOCATION", (e) => {
                    vscode.postMessage({ command: "jumpToLocation", detail: e.detail });
                });
                // VSCode API bridge
                const vscode = acquireVsCodeApi();
            </script>
        `;

        // Insert the script before </body>
        const htmlWithBridge = html.replace(/<\/body>/i, injectedScript + '</body>');

        this._panel.webview.html = htmlWithBridge;

        // Listen for messages from the webview
        this._panel.webview.onDidReceiveMessage(
            message => {
                switch (message.command) {
                    case 'startScan':
                        vscode.window.showInformationMessage('Starting scan...');
                        break;
                    case 'goToWebUi':
                        vscode.window.showInformationMessage('Navigating to Web UI...');
                        break;
                    case 'markFalsePositive':
                        vscode.window.showInformationMessage(`Marking ${message.findingId} as false positive.`);
                        break;
                    case 'unmarkFalsePositive':
                        vscode.window.showInformationMessage(`Unmarking ${message.findingId} as false positive.`);
                        break;
                    case 'jumpToLocation':
                        vscode.window.showInformationMessage(`Jumping to location: ${message.detail.location}`);
                        break;
                }
            },
            undefined,
            this._disposables
        );
        */
    }

    private _getHtmlForWebview(loginPageUrl: string) {

        // const loginPageUrl = 'https://localhost:8443/login?theme=jetbrains&redirectUri=/api/projects'; // problem: ssl certificates
        // const loginPageUrl = `http://localhost:8000/login?theme=${theme}&redirectUri=${redirectUri}/`; // problem: http login redirect auf https (immer)
        // const loginPageUrl = `http://localhost:8000/login?theme=${theme}`;
        // const loginPageUrl = 'https://sechub-dev.app.corpintra.net/login' // problem: x-frame-options
        // const loginPageUrl = 'http://localhost:3000/login' // problem: auch https redirect, nur web-ui funktioniert 

        return `<!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>SecHub Login</title>
                <style>
                    .iframe-error {
                        color: red;
                        margin-top: 20px;
                        display: none;
                    }
                </style>
            </head>
            <body>
                <h1>SecHub Plugin Webview</h1>
				<iframe id="loginFrame" src="${loginPageUrl}" width="100%" height="500px" frameborder="0"></iframe>
            </body>
            </html>`;
    }
}

async function fetchAndSaveContent(url: string, baseDir: string, webview: vscode.Webview): Promise<string> {
    // login download and render

    try {
        const response = await axios.get(url, { httpsAgent: new https.Agent({ rejectUnauthorized: false }) });
        let html = response.data;
        const $ = load(html);

        // Modify form action to point to the correct backend URL
        $('form[action="/login"]').attr('action', 'https://localhost:8443/login');
        console.log('Form action modified:', $('form').attr('action')); // Debugging: Log the form action


        const resources = [
            ...Array.from($('link[rel="stylesheet"]')).map(link => $(link).attr('href')),
            ...Array.from($('script[src]')).map(script => $(script).attr('src')),
            ...Array.from($('img[src]')).map(img => $(img).attr('src'))
        ];

        for (const resourceUrl of resources) {
            if (resourceUrl) {
                const absoluteUrl = new URL(resourceUrl, url).href;
                const resourcePath = path.join(baseDir, 'media', path.basename(resourceUrl));
                await fetchResource(absoluteUrl, resourcePath);

                // Update HTML to reference local paths
                const localPath = webview.asWebviewUri(vscode.Uri.file(resourcePath)).toString();
                html = html.replace(new RegExp(resourceUrl, 'g'), localPath);
                console.log(html)
            }
        }

        return $.html(); // Return the modified HTML
    } catch (error) {
        console.error('Error fetching URL:', error);
        return '';
    }
}

async function fetchResource(url: string, filePath: string) {
    try {
        const response = await axios.get(url, { responseType: 'arraybuffer', httpsAgent: new https.Agent({ rejectUnauthorized: false }) });
        fs.writeFileSync(filePath, response.data);
        console.log(`Resource saved: ${filePath}`);
    } catch (error) {
        console.error(`Error fetching resource ${url}:`, error);
    }
}

async function fetchReport(reportUrl: string, username: string, password: string) {

    try {
        const response = await axios.get(reportUrl, {
            auth: {
                username: username,
                password: password
            },
            httpsAgent: new https.Agent({
                rejectUnauthorized: false 
            }),
            headers: {
                'Accept': 'text/html', // Set the Accept header to text/html
                'Content-Type': 'text/html;charset=UTF-8'
            }
        });

        return response.data;
    } catch (error) {
        console.error('Error fetching report:', error);
        return '<h1>Error loading report</h1>';
    }
}

export function deactivate() {}
