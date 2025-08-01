import * as vscode from 'vscode';
import { SecHubContext } from '../extension';
import { SECHUB_COMMANDS, SECHUB_VIEW_IDS } from '../utils/sechubConstants';
import { ReportListTable } from '../webview/reportTable';
import { openCWEIDInBrowser } from '../utils/sechubUtils';
import { FalsePositiveCache } from '../cache/falsePositiveCache';

export class SecHubReportWebViewProvider implements vscode.WebviewViewProvider {

    public static readonly viewType = SECHUB_VIEW_IDS.reportView;

    private _view?: vscode.WebviewView;
    private _sechubContext: SecHubContext;
    private reportListTable = new ReportListTable();

    constructor(
        private readonly _extensionUri: vscode.Uri,
        _sechubContext: SecHubContext,
    ) { 
        this._sechubContext = _sechubContext;
    }

    public async resolveWebviewView(
        webviewView: vscode.WebviewView,
        _context: vscode.WebviewViewResolveContext,
        _token: vscode.CancellationToken,
    ) {
        this._view = webviewView;

        webviewView.webview.options = {
            enableScripts: true,

            localResourceRoots: [
                this._extensionUri
            ]
        };

        webviewView.webview.html = await this._getHtmlForWebview(webviewView.webview);

        webviewView.webview.onDidReceiveMessage(data => {
			switch (data.type) {
                case 'openFinding':
                    {

                    const findingId: number = Number(data.findingId); 
                    const report = this._sechubContext.getReport();
                    if (!report) {
                        vscode.window.showErrorMessage('No report available to open finding.');
                        return;
                    }
                    if (!report.result || !report.result.findings) {
                        vscode.window.showErrorMessage('No findings available in the report.');
                        return;
                    }
                    const finding = report.result.findings.find(f => {
                            return f.id === findingId;
                    });

                    if (finding) {
                        if(finding.web){
                            vscode.commands.executeCommand(SECHUB_COMMANDS.openWebScanInInfoview, finding);
                        }else {
                            vscode.commands.executeCommand(SECHUB_COMMANDS.openFinding, finding, finding.code?.calls);
                            vscode.commands.executeCommand(SECHUB_COMMANDS.openFindingCallStack, finding);
                        }
                        } else {
                            vscode.window.showErrorMessage(`Finding with ID ${findingId} not found in the report.`);
                        }
                    }
                    break;
                case 'markAsFalsePositive':
                    {
                        const findingIds: number[] = data.findingIds as number[];
                        FalsePositiveCache.removeEntryByJobUUID(this._sechubContext.extensionContext, this._sechubContext.getReport()?.jobUUID || '');
                        vscode.commands.executeCommand(SECHUB_COMMANDS.markFalsePositives, this._sechubContext, findingIds);
                    }
                    break;
                case 'openCWEInBrowser':
                    {
                        const cweId = data.cweId;
                        openCWEIDInBrowser(cweId);
                    }
                    break;
                default:
                    break;
                }
            });

    }

    	public async refresh() {
		if (this._view) {
			this._view.show?.(true);
			this._view.webview.html = await this._getHtmlForWebview(this._view.webview);
		}
	}

    private async _getHtmlForWebview(webview: vscode.Webview): Promise<string> {
        const scriptUri = webview.asWebviewUri(vscode.Uri.joinPath(this._extensionUri, 'media', 'js', 'report.js'));
        const styleUri = webview.asWebviewUri(vscode.Uri.joinPath(this._extensionUri, 'media', 'css', 'main.css'));
        const reportStyleUri = webview.asWebviewUri(vscode.Uri.joinPath(this._extensionUri, 'media', 'css', 'report.css'));
        const codiconsUri = webview.asWebviewUri(vscode.Uri.joinPath(vscode.Uri.joinPath(this._extensionUri, 'node_modules', '@vscode/codicons', 'dist', 'codicon.css')));


        const report = this._sechubContext.getReport();
        if (!report) {
            return `<!DOCTYPE html>
            <html lang="en">
            <head>
            <div>
            No SecHub report is loaded.
            </div>
            <div>
            Import a report from your filesystem or load a report from server to view the fndings.
            </div>
            </head>
            <body>`;
        }

        const reportTable = await this.reportListTable.renderReportTable(this._sechubContext.extensionContext, report);
        const nonce = getNonce();

        return `
		<!DOCTYPE html>
			<html lang="en">
			<head>
				<meta charset="UTF-8">
				<!--
					Use a content security policy to only allow loading styles from our extension directory,
					and only allow scripts that have a specific nonce.
					(See the 'webview-sample' extension sample for img-src content security policy examples)
				-->
				<meta http-equiv="Content-Security-Policy" content="default-src 'none';  font-src ${webview.cspSource}; style-src ${webview.cspSource}; script-src 'nonce-${nonce}';">

				<meta name="viewport" content="width=device-width, initial-scale=1.0">
				
				<link href="${styleUri}" rel="stylesheet">
                <link href="${codiconsUri}" rel="stylesheet">
			</head>
			<body class="vscode-light">
				${reportTable}
			<script nonce="${nonce}" src="${scriptUri}" script-src 'nonce-${nonce}'></script>
			</body>
			</html>
		`;
    }
}

function getNonce() {
	let text = '';
	const possible = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
	for (let i = 0; i < 32; i++) {
		text += possible.charAt(Math.floor(Math.random() * possible.length));
	}
	return text;
}