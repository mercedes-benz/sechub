// SPDX-License-Identifier: MIT
import * as vscode from 'vscode';
import { SecHubContext } from "../extension";
import { SECHUB_COMMANDS, SECHUB_CREDENTIAL_KEYS, SECHUB_REPORT_KEYS } from "../utils/sechubConstants";
import { DefaultClient } from "../api/defaultClient";
import { ProjectData, ScanType, SecHubReport } from 'sechub-openapi-ts-client';
import { multiStepInput } from '../utils/sechubCredentialsMultistepInput';

export async function fetchReportByUUID(sechubContext: SecHubContext): Promise<void>{

    const serverUrl = sechubContext.extensionContext.globalState.get<string>(SECHUB_CREDENTIAL_KEYS.serverUrl);
    if (!serverUrl) {
        vscode.window.showErrorMessage('Please configure SecHubServer URL first.');
        multiStepInput(sechubContext.extensionContext).
            then(() => {
                vscode.commands.executeCommand(SECHUB_COMMANDS.fetchReportByUUID);
            });
        return;
    }

    const project = sechubContext.extensionContext.globalState.get<ProjectData>(SECHUB_REPORT_KEYS.selectedProject);

    if (!project) {
        vscode.commands.executeCommand(SECHUB_COMMANDS.selectProject);
        vscode.window.showErrorMessage('Please select a project first.');
        return;
    }

    const jobUUID = await vscode.window.showInputBox({
        prompt: `Project: ${project.projectId}. Enter SecHub Job UUID`,
        validateInput: (value: string) => !value ? 'Job UUID cannot be empty' : null
    });

    if( !jobUUID) {
        vscode.window.showErrorMessage('Job UUID is required to fetch the report.');
        return;
    }

    const client = await DefaultClient.getInstance(sechubContext.extensionContext);

    try {
        const data = await client.fetchReport(project.projectId, jobUUID);
        if (data) {
            sechubContext.setReport(data);
            vscode.window.showInformationMessage(`Report for job ${jobUUID} fetched successfully.`);
        } else {
            vscode.window.showErrorMessage(`No report found for job ${jobUUID}.`);
        }

    } catch (error) {
        console.error('Error fetching report:', error);
        vscode.window.showErrorMessage(`Failed to fetch report for job ${jobUUID}. Please check your connection and credentials.`);
    }
}