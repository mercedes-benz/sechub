// SPDX-License-Identifier: MIT

import * as fs from 'fs';
import { GitHubInputData } from './github-input';
import * as jsonHelper from './json-helper';
import * as core from '@actions/core';

/**
 * Creates the initial launch context
 * @returns launch context
 */
export function resolveProjectName(gitHubInputData: GitHubInputData,configFileLocation:string ): string {

    let projectName = '';
    projectName = gitHubInputData.projectName;

    if (!projectName || projectName.length === 0) {
        const secHubConfigurationJson = fs.readFileSync(configFileLocation, 'utf8');
        const jsonObj = asJsonObject(secHubConfigurationJson);
        if (jsonObj) {
            const projectData = jsonHelper.getFieldFromJson('project', jsonObj);
            if (typeof projectData === 'string') {
                projectName = projectData;
            }
        }
    }
    return projectName;
}


function asJsonObject(text: string): object | undefined {
    try {
        return JSON.parse(text);
    } catch (error) {
        core.warning(`Error parsing JSON file: ${error}`);
        return undefined;
    }
}
