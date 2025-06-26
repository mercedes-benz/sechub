import * as vscode from 'vscode';

export function activate(context: vscode.ExtensionContext) {
    context.subscriptions.push(
        vscode.commands.registerCommand('sechub.start', () => {
            SecHubPanel.createOrShow(context.extensionUri);
        })
    );

    if (vscode.window.registerWebviewPanelSerializer) {
        vscode.window.registerWebviewPanelSerializer(SecHubPanel.viewType, {
            async deserializeWebviewPanel(webviewPanel: vscode.WebviewPanel, state: unknown) {
                console.log(`Got state: ${state}`);
                webviewPanel.webview.options = getWebviewOptions(context.extensionUri);
                SecHubPanel.revive(webviewPanel, context.extensionUri);
            }
        });
    }
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

    private _update() {
        const webview = this._panel.webview;
        this._panel.title = 'SecHub Login';
        this._panel.webview.html = this._getHtmlForWebview(webview);
    }

    private _getHtmlForWebview(webview: vscode.Webview) {
        const theme = 'vscode'
        const redirectUri = 'http://localhost:3000'

        // const loginPageUrl = 'https://localhost:8443/login?theme=jetbrains&redirectUri=/api/projects'; // problem: ssl certificates
        const loginPageUrl = `http://localhost:8000/login?theme=${theme}&redirectUri=${redirectUri}/`; // problem: http login redirect auf https (immer)
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
                <h1>SecHub Plugin</h1>
				<iframe id="loginFrame" src="${loginPageUrl}" width="100%" height="500px" frameborder="0"></iframe>
            </body>
            </html>`;
    }
}
