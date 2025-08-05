// SPDX-License-Identifier: MIT
import * as vscode from 'vscode';
import { DefaultClient } from '../api/defaultClient';
import { SECHUB_CONTEXT_STORAGE_KEYS, SECHUB_COMMANDS } from '../utils/sechubConstants';
import { SecHubContext } from '../extension';

export async function selectProject(sechubContext: SecHubContext): Promise<void> {
    const client = await DefaultClient.getInstance(sechubContext.extensionContext);

    try {
        await client.isAlive();
    } catch (error) {
        vscode.window.showErrorMessage('SecHub client is not alive. Please check your connection or credentials.');
        return;
    }

    try {
        const projects = await client.getAssignedProjectDataList();
        if (!projects){
            vscode.window.showErrorMessage('Failed to fetch projects from the server.');
            return;
            
        } else if(projects.length === 0) {
            vscode.window.showInformationMessage('No projects available.');
            return;
        }

        const projectNames = projects.map(project => project.projectId);
        const selectedProject = await vscode.window.showQuickPick(projectNames, {
            placeHolder: 'Select a project',
            canPickMany: false
        });

        if (selectedProject) {
            const projectData = projects.find(p => p.projectId === selectedProject);
            if (projectData) {
                vscode.window.showInformationMessage(`Selected Project: ${projectData.projectId}`);
                sechubContext.extensionContext.globalState.update(SECHUB_CONTEXT_STORAGE_KEYS.selectedProject, projectData);
                sechubContext.serverWebViewProvider.refresh();
                vscode.commands.executeCommand(SECHUB_COMMANDS.fetchFalsePositives);
            }
        }
    } catch (error) {
        console.error('Error fetching projects:', error);
        vscode.window.showErrorMessage('Failed to fetch projects. Please check your connection and credentials.');
    }
}