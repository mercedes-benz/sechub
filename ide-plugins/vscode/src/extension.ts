// SPDX-License-Identifier: MIT
// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below
import * as path from 'path';
import * as vscode from 'vscode';
import { FileLocationExplorer } from './utils/fileLocationExplorer';
import { InfoViewItemMitreCWELinkBuilder } from './provider/items/infoViewItemMitreCWELinkBuilder';
import { SecHubCallHierarchyTreeDataProvider } from './provider/secHubCallHierarchyTreeDataProvider';
import { HierarchyItem } from './provider/items/hierarchyItems';
import { SecHubInfoTreeDataProvider } from './provider/secHubInfoTreeDataProvider';

import { loadFromFile, preSelectedProjectValid } from './utils/sechubUtils';
import { SecHubReport, ScanType, SecHubFinding, SecHubCodeCallStack } from 'sechub-openapi-ts-client';
import { multiStepInput } from './utils/sechubCredentialsMultistepInput';
import { SECHUB_API_CLIENT_CONFIG_KEYS, SECHUB_VIEW_IDS } from './utils/sechubConstants';
import { DefaultClient } from './api/defaultClient';
import { SecHubServerWebviewProvider } from './provider/SecHubServerWebviewProvider';
import { SecHubReportWebViewProvider } from './provider/secHubReportWebViewProvider';
import { commands, sechubFindingCommands ,sechubFindingAndCallstackCommands, markFalsePositiveCommands } from './commands/commands';
import { FalsePositiveCache } from './cache/falsePositiveCache';

export async function activate(context: vscode.ExtensionContext) {
	console.log('SecHub plugin activation requested.');
	
	const secHubContext: SecHubContext = new SecHubContext(context);

	let loadTestData = context.extensionMode === vscode.ExtensionMode.Development;
	let report: SecHubReport | undefined = undefined;
	if (loadTestData) {
		report = loadFromFile(resolveFileLocation("test_sechub_report-1.json"));
	}
	secHubContext.setReport(report);

	await setUpApiClient(context);
	await preSelectedProjectValid(context);

	buildServerWebview(secHubContext);
	buildReportWebview(secHubContext);
	buildCallHierarchyView(secHubContext);
	buildInfoView(secHubContext);

	registerCommands(secHubContext);

	console.log('SecHub plugin has been activated.');
}

function registerCommands(sechubContext: SecHubContext) {

	const registeredCommands = commands.map(({ command, action}) =>
		vscode.commands.registerCommand(command, () => action(sechubContext))
	);

	const registeredHierachyCommands = sechubFindingCommands.map(({ command, action }) =>
		vscode.commands.registerCommand(command, (finding: SecHubFinding) => action(sechubContext, finding))
	);

	const registerTestCommands = sechubFindingAndCallstackCommands.map(({ command, action }) =>
		vscode.commands.registerCommand(command, (finding: SecHubFinding, callstack: SecHubCodeCallStack) => action(sechubContext, finding, callstack))
	);

	const registerMarkFalsePositiveCommands = markFalsePositiveCommands.map(({ command, action }) =>
		vscode.commands.registerCommand(command, async (jobUUID: string, findingIds: number[]) => {
			await action(sechubContext, findingIds);
		})
	);


	sechubContext.extensionContext.subscriptions.push(
		...registeredCommands,
		...registeredHierachyCommands,
		...registerTestCommands,
		...registerMarkFalsePositiveCommands);
}

async function setUpApiClient(context: vscode.ExtensionContext) {
	// Check if SecHub credentials are already set
	// If not, prompt the user to set them up
    const serverUrl = context.globalState.get<string>(SECHUB_API_CLIENT_CONFIG_KEYS.serverUrl);
    const username = context.secrets.get(SECHUB_API_CLIENT_CONFIG_KEYS.username);
    const apiToken = context.secrets.get(SECHUB_API_CLIENT_CONFIG_KEYS.apiToken);
    Promise.all([username, apiToken]).then(([username, apiToken]) => {
        if (!serverUrl || !username || !apiToken) {
            multiStepInput(context).then(() => {
                vscode.window.showInformationMessage('SecHub credentials have been set.');
            }).catch(err => {
                vscode.window.showErrorMessage(`Failed to set SecHub credentials: ${err}`);
            });
        }
    });

	// Initialize the SecHub client
	await DefaultClient.createClient(context).then(() => {
		vscode.window.showInformationMessage('SecHub client initialized successfully.');
	}).catch(err => {
		vscode.window.showErrorMessage(`Failed to initialize SecHub client:	${err}`);
	});
}

function buildCallHierarchyView(context: SecHubContext) {
	const view = vscode.window.createTreeView(SecHubCallHierarchyTreeDataProvider.viewType, {
		treeDataProvider: context.callHierarchyTreeDataProvider
	});
	context.callHierarchyView=view;
}

function buildInfoView(context: SecHubContext) {
	vscode.window.createTreeView(SECHUB_VIEW_IDS.infoView, {
		treeDataProvider: context.infoTreeProvider
	});
}

function buildServerWebview(context: SecHubContext) {
	const provider = new SecHubServerWebviewProvider(context.extensionContext.extensionUri, context);
	context.extensionContext.subscriptions.push(
		vscode.window.registerWebviewViewProvider(SecHubServerWebviewProvider.viewType, provider));
	context.serverWebViewProvider = provider;
}

function buildReportWebview(context: SecHubContext) {
	const provider = new SecHubReportWebViewProvider(context.extensionContext.extensionUri, context);
	context.extensionContext.subscriptions.push(
		vscode.window.registerWebviewViewProvider(SecHubReportWebViewProvider.viewType, provider));
	context.reportWebViewProvider = provider;
}

export class SecHubContext {
	callHierarchyView: vscode.TreeView<HierarchyItem|undefined> | undefined = undefined;

	private report: SecHubReport | undefined;

	findingNodeLinkBuilder: InfoViewItemMitreCWELinkBuilder;
	callHierarchyTreeDataProvider: SecHubCallHierarchyTreeDataProvider;
	infoTreeProvider: SecHubInfoTreeDataProvider;
	extensionContext: vscode.ExtensionContext;
	fileLocationExplorer: FileLocationExplorer;
	serverWebViewProvider: SecHubServerWebviewProvider;
	reportWebViewProvider: SecHubReportWebViewProvider;

	constructor(extensionContext: vscode.ExtensionContext,
	) {
		this.callHierarchyTreeDataProvider = new SecHubCallHierarchyTreeDataProvider(undefined);
		this.infoTreeProvider = new SecHubInfoTreeDataProvider(undefined, undefined);
		this.extensionContext = extensionContext;
		this.fileLocationExplorer = new FileLocationExplorer();
		this.findingNodeLinkBuilder = new InfoViewItemMitreCWELinkBuilder();
		this.serverWebViewProvider = new SecHubServerWebviewProvider(extensionContext.extensionUri, this);
		this.reportWebViewProvider = new SecHubReportWebViewProvider(extensionContext.extensionUri, this);

		/* setup search folders for explorer */
		let workspaceFolders = vscode.workspace.workspaceFolders; // get the open folder path
		workspaceFolders?.forEach((workspaceFolder) => {
			this.fileLocationExplorer.searchFolders.add(workspaceFolder.uri.fsPath);
		});
	}

	public getReport(): SecHubReport | undefined {
		return this.report;
	}

	public setReport(report: SecHubReport | undefined) {
		try{
			this.checkReport(report);
			this.report = report;
			this.reportWebViewProvider.refresh();
		}catch (error) {
			this.report = undefined;
			this.reportWebViewProvider.refresh();
		}

		this.checkForUnsyncedFalsePositives(report?.jobUUID || '');
		this.callHierarchyTreeDataProvider.update(undefined);
		this.infoTreeProvider.update(undefined, undefined);
	}

	private checkReport(report: SecHubReport | undefined) {
		if (!report) {
			throw new Error("Report is undefined");
		}

		const scanTypes: Array<ScanType> = report.metaData?.executed || [];

		if (scanTypes.includes(ScanType.LicenseScan) && scanTypes.length === 1) {
			const message = "LicenseScan is not supported in this IDE plugin.";
			vscode.window.showErrorMessage(message);		
		}
	}

	private checkForUnsyncedFalsePositives(jobUUID: string) {
		const entry = FalsePositiveCache.getEntryByJobUUID(this.extensionContext, jobUUID);
		if (entry) {
			vscode.window.showWarningMessage(`There are unsynced false positives for job UUID: ${jobUUID}. Please synchronize them.`);
		}
	}
}

export function deactivate() { }

function resolveFileLocation(testfile: string): string {
	let testReportLocation = path.dirname(__filename) + "/../src/test/suite/" + testfile;
	return testReportLocation;
}
