// SPDX-License-Identifier: MIT

import * as shell from 'shelljs';
import * as input from './input';
import * as core from '@actions/core';
import { createSecHubJsonFile } from '../../shared/src/cli-helper';

export enum FalsePositivesActionType {
    DEFINE = 'DEFINE',
    MARK = 'MARK',
    UNMARK = 'UNMARK',
}

export interface FalsePositivesSettings {
    configFile: string | null;
    file: string | null;
    action: FalsePositivesActionType;
}

/**
 * Sets the necessary environment variables with the user input values.
 */
export function initEnvironmentVariables(): void {
    shell.env['SECHUB_USERID'] = input.user ?? (shell.env['SECHUB_USERID'] ?? '')
    shell.env['SECHUB_APITOKEN'] = input.apiToken ?? (shell.env['SECHUB_APITOKEN'] ?? '');
    shell.env['SECHUB_SERVER'] = input.url ?? (shell.env['SECHUB_SERVER'] ?? '');
    shell.env['SECHUB_PROJECT'] = input.projectName ?? (shell.env['SECHUB_PROJECT'] ?? '');
    shell.env['SECHUB_DEBUG'] = input.debug !== null ? input.debug.toString() : (shell.env['SECHUB_DEBUG'] ?? '');
}

/**
 * Returns the parameter to the sechub.json or creates it from the input parameters if configPath is not set.
 * @param configPath Path to the sechub.json
 */
export function initSecHubJson(configPath: string | null): string | null {
    core.startGroup('Set config');
    if (!configPath) {
        createSecHubJsonFile(null, null);
        return null;
    }

    core.info(`Config-Path was found: ${configPath}`);
    core.endGroup();
    return configPath;
}

/**
 * Check if the action is a valid FalsePositivesActionType and returns it.
 * @param action The action from the input
 */
export function checkActionInput(action: string): FalsePositivesActionType {
    // check if input.action is a valid FalsePositivesActionType
    if (!Object.values<string>(FalsePositivesActionType).includes(action)) {
        throw new Error(`Invalid action: ${action}. Valid actions are: ${Object.values(FalsePositivesActionType)}`);
    }
    return action as FalsePositivesActionType;
}

/**
 * Check if the file is required for the action and returns it.
 */
export function checkFileInput(action: FalsePositivesActionType, file: string | null): string | null {
    switch (action) {
        case FalsePositivesActionType.MARK:
        case FalsePositivesActionType.UNMARK:
            if (!file) {
                throw new Error('File is required for action: MARK or UNMARK');
            }
            break;
    }
    return file;
}
