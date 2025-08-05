// SPDX-License-Identifier: MIT
import { FalsePositiveProjectConfiguration, ProjectData, SecHubReport } from 'sechub-openapi-ts-client';
import * as fs from 'fs';
import * as vscode from 'vscode';
import { DefaultClient } from '../api/defaultClient';
import { SECHUB_CONTEXT_STORAGE_KEYS } from './sechubConstants';

export function loadFromFile(location: string): SecHubReport {

    const rawReport = fs.readFileSync(location, 'utf8');
    return JSON.parse(rawReport) as SecHubReport;
}

export function openCWEIDInBrowser(cweId: string | undefined): void {
    if (!cweId || cweId === 'undefined') {
        return;
    }
    const uri = vscode.Uri.parse(`https://cwe.mitre.org/data/definitions/${cweId}.html`);
    vscode.commands.executeCommand("vscode.open", uri);
}

export function getFalsePositivesByIDForJobReport(falsePositiveConfig: FalsePositiveProjectConfiguration, jobUUID: string): number[] {
    const falsePositivesEntrys = falsePositiveConfig.falsePositives || [];

    const falsePositivesFindingIDs: number[] = [];
    falsePositivesEntrys.forEach(entry => {
        if (entry.jobData?.jobUUID === jobUUID) {
            if (entry.jobData.findingId) {
                falsePositivesFindingIDs.push(entry.jobData.findingId);
            }
        }
    });

    return falsePositivesFindingIDs;
}

export async function preSelectedProjectValid(context: vscode.ExtensionContext): Promise<void> {
	const project = context.globalState.get<ProjectData>(SECHUB_CONTEXT_STORAGE_KEYS.selectedProject);
	if (!project) {
		return;
	}

	const client = await DefaultClient.getInstance(context);
    const alive = await client.isAlive();
    if(!alive) {
        vscode.window.showErrorMessage('SecHub client is not alive. Please check your connection or credentials.');
        return;
    }
	const projects = await client.getAssignedProjectDataList();

	if (!projects || !projects.some(p => p.projectId === project.projectId)) {
		vscode.window.showErrorMessage(`Selected project ${project.projectId} is not valid. Please select a valid project.`);
		await context.globalState.update(SECHUB_CONTEXT_STORAGE_KEYS.selectedProject, undefined);
		return;
	}
}

export function getNonce() {
	let text = '';
	const possible = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
	for (let i = 0; i < 32; i++) {
		text += possible.charAt(Math.floor(Math.random() * possible.length));
	}
	return text;
}
