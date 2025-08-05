import * as vscode from 'vscode';
import { FalsePositives, FalsePositiveJobData, ProjectData } from 'sechub-openapi-ts-client';
import { DefaultClient } from '../api/defaultClient';
import { SecHubContext } from '../extension';
import { SECHUB_COMMANDS, SECHUB_CONTEXT_STORAGE_KEYS } from '../utils/sechubConstants';
import { FalsePositiveCache } from '../cache/falsePositiveCache';

export async function markFalsePositivesForProject(context: SecHubContext): Promise<void> {

  const jobUUID = context.getReport()?.jobUUID;

  const project = context.extensionContext.globalState.get<ProjectData>(SECHUB_CONTEXT_STORAGE_KEYS.selectedProject);

  if (!jobUUID) {
      vscode.window.showErrorMessage('No job UUID found in the report. Please ensure a report jobUUID is available.');
      return;
  }
    
  if (!project || !project.projectId) {
      return;
  }

  const findingIds = FalsePositiveCache.getEntryByJobUUID(context.extensionContext, jobUUID)?.findingIDs;

  if (!findingIds || findingIds.length === 0) {
    return; 
  }

  const falsePositiveReasons = [
      { label: 'Fix Already Started', description: 'A fix has already been started.' },
      { label: 'No Bandwidth', description: 'No bandwidth to fix this.' },
      { label: 'Tolerable Risk', description: 'Risk is tolerable to this project.' },
      { label: 'Inaccurate Alert', description: 'This alert is inaccurate or incorrect.' },
      { label: 'Unused Code', description: 'Vulnerable code is not actually used.' }
  ];

  const selectedReason = await vscode.window.showQuickPick(falsePositiveReasons, {
      placeHolder: 'Select a reason for marking as false positive: (cancel with ESC)',
      canPickMany: false
  });

  const customComment = await vscode.window.showInputBox({
      prompt: 'Enter a custom comment (optional):',
      placeHolder: 'Enter your comment here...',
  });

  if (!selectedReason) {
      vscode.window.showErrorMessage('You must select a reason to mark as false positive.');
      return; 
  }

  if (customComment === undefined) {
    return;
  }

  let comment = '';
  if (selectedReason && customComment !== undefined) {
    comment = `${selectedReason.label}: ${selectedReason.description}, ${customComment}`;
  } else {
    comment = `${selectedReason.label}: ${selectedReason.description}`;
  }

  const falsePositives: FalsePositives = createFalsePositives(findingIds, jobUUID, comment);
  const client = await DefaultClient.getInstance(context.extensionContext);

  const alive = await client.isAlive();
  if(!alive) {
      vscode.window.showErrorMessage('SecHub client is not alive. Please check your connection or credentials.');
      return;
  }

  const success = await client.markFalsePositivesForProject(falsePositives, project.projectId);

  if (!success) {
    vscode.window.showErrorMessage(`Failed to mark findings as false positive. Please try synchronizing later.`);
    return;
  }

  FalsePositiveCache.removeEntryByJobUUID(context.extensionContext, jobUUID);
  await vscode.commands.executeCommand(SECHUB_COMMANDS.fetchFalsePositives, context, project.projectId);
  await context.reportWebViewProvider?.refresh();
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