// SPDX-License-Identifier: MIT
// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below
import * as path from 'path';
import * as vscode from 'vscode';
import { FileLocationExplorer } from './utils/fileLocationExplorer';
import { FindingNodeLinkBuilder } from './utils/findingNodeLinkBuilder';
import { SecHubCallHierarchyTreeDataProvider } from './provider/secHubCallHierarchyTreeDataProvider';
import { HierarchyItem } from './provider/items/hierarchyItems';
import { SecHubInfoTreeDataProvider } from './provider/secHubInfoTreeDataProvider';
import { SecHubReportTreeDataProvider } from './provider/secHubReportTreeDataProvider';
import { ReportItem } from './provider/items/reportItems';

import { loadFromFile } from './utils/sechubUtils';
import { SecHubReport, ScanType, ProjectData } from 'sechub-openapi-ts-client';
import { multiStepInput } from './utils/sechubCredentialsMultistepInput';
import { SECHUB_CREDENTIAL_KEYS, SECHUB_REPORT_KEYS, SECHUB_VIEW_IDS } from './utils/sechubConstants';
import { DefaultClient } from './api/defaultClient';
import { SecHubServerWebviewProvider } from './provider/SecHubServerWebviewProvider';
import { commands, hierachyCommands, reportItemCommands } from './commands/commands';

export async function activate(context: vscode.ExtensionContext) {
	console.log('SecHub plugin activation requested.');
	
	let loadTestData = context.extensionMode === vscode.ExtensionMode.Development;
	let report: SecHubReport | undefined = undefined;
	if (loadTestData) {
		report = loadFromFile(resolveFileLocation("test_sechub_report-1.json"));
	}

	await setUpApiClient(context);
	await preSelectedProjectValid(context);

	let secHubContext: SecHubContext = new SecHubContext(report, context);

	buildServerWebview(secHubContext);
	buildReportView(secHubContext);
	buildCallHierarchyView(secHubContext);
	buildInfoView(secHubContext);

	registerCommands(secHubContext);

	console.log('SecHub plugin has been activated.');
}

function registerCommands(sechubContext: SecHubContext) {

	const registeredCommands = commands.map(({ command, action}) =>
		vscode.commands.registerCommand(command, () => action(sechubContext))
	);

	const registeredHierachyCommands = hierachyCommands.map(({ command, action }) =>
		vscode.commands.registerCommand(command, (hierarchyItem: HierarchyItem) => action(sechubContext, hierarchyItem))
	);

	const registerReportItemCommands = reportItemCommands.map(({ command, action }) =>
		vscode.commands.registerCommand(command, (reportItem: ReportItem) => action(sechubContext, reportItem))
	);

	sechubContext.extensionContext.subscriptions.push(
		...registeredCommands,
		...registeredHierachyCommands,
		...registerReportItemCommands);
}

async function setUpApiClient(context: vscode.ExtensionContext) {
	// Check if SecHub credentials are already set
	// If not, prompt the user to set them up
    const serverUrl = context.globalState.get<string>(SECHUB_CREDENTIAL_KEYS.serverUrl);
    const username = context.secrets.get(SECHUB_CREDENTIAL_KEYS.username);
    const apiToken = context.secrets.get(SECHUB_CREDENTIAL_KEYS.apiToken);
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

async function preSelectedProjectValid(context: vscode.ExtensionContext): Promise<void> {
	const project = context.globalState.get<ProjectData>(SECHUB_REPORT_KEYS.selectedProject);
	if (!project) {
		return;
	}

	const client = await DefaultClient.getInstance(context);
	const projects = await client.getAssignedProjectDataList();

	if(!projects || !projects.some(p => p.projectId === project.projectId)) {
		vscode.window.showErrorMessage(`Selected project ${project.projectId} is not valid. Please select a valid project.`);
		await context.globalState.update(SECHUB_REPORT_KEYS.selectedProject, undefined);
		return;
	}
}

function buildReportView(context: SecHubContext) {
	const view =vscode.window.createTreeView(SECHUB_VIEW_IDS.reportView, {
		treeDataProvider: context.reportTreeProvider
	});
	context.reportView=view;
}

function buildCallHierarchyView(context: SecHubContext) {
	const view = vscode.window.createTreeView(SECHUB_VIEW_IDS.callHierarchyView, {
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

export class SecHubContext {
	callHierarchyView: vscode.TreeView<HierarchyItem|undefined> | undefined = undefined;
	reportView: vscode.TreeView<ReportItem> | undefined = undefined;

	private report: SecHubReport | undefined;

	findingNodeLinkBuilder: FindingNodeLinkBuilder;
	callHierarchyTreeDataProvider: SecHubCallHierarchyTreeDataProvider;
	reportTreeProvider: SecHubReportTreeDataProvider;
	infoTreeProvider: SecHubInfoTreeDataProvider;
	extensionContext: vscode.ExtensionContext;
	fileLocationExplorer: FileLocationExplorer;
	serverWebViewProvider: SecHubServerWebviewProvider;

	constructor(report: SecHubReport| undefined, extensionContext: vscode.ExtensionContext,
	) {
		this.reportTreeProvider = new SecHubReportTreeDataProvider(report);
		this.callHierarchyTreeDataProvider = new SecHubCallHierarchyTreeDataProvider(undefined);
		this.infoTreeProvider = new SecHubInfoTreeDataProvider(undefined, undefined);
		this.extensionContext = extensionContext;
		this.fileLocationExplorer = new FileLocationExplorer();
		this.findingNodeLinkBuilder = new FindingNodeLinkBuilder();
		this.serverWebViewProvider = new SecHubServerWebviewProvider(extensionContext.extensionUri, this);

		/* setup search folders for explorer */
		let workspaceFolders = vscode.workspace.workspaceFolders; // get the open folder path
		workspaceFolders?.forEach((workspaceFolder) => {
			this.fileLocationExplorer.searchFolders.add(workspaceFolder.uri.fsPath);
		});
	}

	public setReport(report: SecHubReport | undefined) {
		try{
			this.checkReport(report);
			this.report = report;
			this.reportTreeProvider.update(report);
		}catch (error) {
			this.report = undefined;
			this.reportTreeProvider.update({});
		}

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
}

export function deactivate() { }

function resolveFileLocation(testfile: string): string {
	let testReportLocation = path.dirname(__filename) + "/../src/test/suite/" + testfile;
	return testReportLocation;
}
