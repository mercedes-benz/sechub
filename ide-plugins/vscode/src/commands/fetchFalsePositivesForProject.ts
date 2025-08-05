import * as vscode from 'vscode';
import { SecHubContext } from "../extension";
import { SECHUB_CONTEXT_STORAGE_KEYS } from "../utils/sechubConstants";
import { ProjectData } from "sechub-openapi-ts-client";
import { DefaultClient } from "../api/defaultClient";

export async function fetchFalsePositivesForProject(sechubContext: SecHubContext): Promise<void> {

    const project = sechubContext.extensionContext.globalState.get<ProjectData>(SECHUB_CONTEXT_STORAGE_KEYS.selectedProject);

    if (!project) {
        vscode.window.showErrorMessage("No project selected. Please select a project first.");
        return;
    }

    const client = await DefaultClient.getInstance(sechubContext.extensionContext);
    const response = await client.userFetchFalsePositiveConfigurationOfProject(project.projectId);

    if (!response) {
        vscode.window.showErrorMessage('Failed to fetch false positives project configuration from the server.');
        return;
    } else {        
        sechubContext.extensionContext.globalState.update(SECHUB_CONTEXT_STORAGE_KEYS.falsePositiveConfiguration, response);
    }
}