import * as vscode from 'vscode';
import { FalsePositives, FalsePositiveJobData, ProjectData } from 'sechub-openapi-ts-client';
import { DefaultClient } from '../api/defaultClient';
import { SecHubContext } from '../extension';
import { SECHUB_COMMANDS, SECHUB_CONTEXT_STORAGE_KEYS } from '../utils/sechubConstants';

export async function markFalsePositivesForProject(context: SecHubContext, findingIds: number[]): Promise<void> {

    const comment = await vscode.window.showInputBox({
        prompt: 'Enter a comment for marking as false positive:',
        placeHolder: 'e.g., Not applicable in this context',
    });

    if (comment === undefined) {
        vscode.window.showInformationMessage('No comment provided. Operation cancelled.');
        return;
    }

    const project = context.extensionContext.globalState.get<ProjectData>(SECHUB_CONTEXT_STORAGE_KEYS.selectedProject);
    
    
    if (!project || !project.projectId) {
        vscode.window.showErrorMessage('Project ID not found in context. Please select a project first.');
        return;
    }

    const jobUUID = context.getReport()?.jobUUID;
    if (!jobUUID) {
        vscode.window.showErrorMessage('No job UUID found in the report. Please ensure a report is available.');
        return;
    }
    const falsePositives: FalsePositives = createFalsePositives(findingIds, jobUUID, comment);
    const client = await DefaultClient.getInstance(context.extensionContext);
    await client.markFalsePositivesForProject(falsePositives, project.projectId);
    vscode.commands.executeCommand(SECHUB_COMMANDS.fetchFalsePositives, context, project.projectId);
    context.reportWebViewProvider?.refresh();
}

function createFalsePositives (selectedFindings: number[], jobUUID: string, comment: string): FalsePositives {
    const falsePositiveJobData: Array<FalsePositiveJobData> = [];
    selectedFindings.forEach(finding => {
      const data: FalsePositiveJobData = {
        findingId: finding,
        jobUUID,
        comment: comment,
      };
      falsePositiveJobData.push(data);
    });

    const falsePositives: FalsePositives = {
      apiVersion: '1.0',
      type: 'falsePositiveDataList',
      jobData: falsePositiveJobData,
    };

    return falsePositives;
  }