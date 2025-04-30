// SPDX-License-Identifier: MIT

import * as core from '@actions/core';
import * as fs from 'fs';
import { GitHubInputData } from './github-input';
import * as jsonHelper from './json-helper';

/**
 * Creates the initial launch context
 * @returns launch context
 */
export function resolveProjectName(gitHubInputData: GitHubInputData, configFileLocation: string): string {

    let projectName = '';
    projectName = gitHubInputData.projectName;

    if (!projectName || projectName.length === 0) {
        if (core.isDebug()) {
            core.debug('Project name not defined as parameter - so start resolving from config:' + configFileLocation);
        }
        const secHubConfigurationJson = fs.readFileSync(configFileLocation, 'utf8');
        if (core.isDebug()) {
            core.debug('Loaded config file:' + configFileLocation + '\nContent:\n' + secHubConfigurationJson);
        }
        const jsonObj = asJsonObject(secHubConfigurationJson);
        if (jsonObj) {
            const projectData = jsonHelper.getFieldFromJson('project', jsonObj);
            if (typeof projectData === 'string') {
                projectName = projectData;
            }
        } else {
            throw new Error('SecHub configuration not available as object!');
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
