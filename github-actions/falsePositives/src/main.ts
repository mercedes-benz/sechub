// SPDX-License-Identifier: MIT

import { failAction, handleError } from '../../shared/src/action-helper';
import { setupSecHubCli } from '../../shared/src/cli-helper';
import { defineFalsePositives, markFalsePositives, unmarkFalsePositives } from '../../shared/src/sechub-cli';
import { logExitCode } from '../../shared/src/log-helper';
import {
    FalsePositivesActionType,
    FalsePositivesSettings,
    checkActionInput,
    initEnvironmentVariables,
    checkFileInput,
    initSecHubJson,
} from './init-operation';
import * as input from './input';
import { ShellString } from 'shelljs';

main().catch(handleError);

async function main(): Promise<void> {
    const settings = await initOperation();
    const exitCode = executeOperation(settings);
    await postOperation(exitCode);
}

/**
 * Initializes defineFalsePositives operation and returns required settings.
 */
async function initOperation(): Promise<FalsePositivesSettings> {
    initEnvironmentVariables();
    await setupSecHubCli(input.sechubCLIVersion);
    const configFile = initSecHubJson(input.configPath);
    const action = checkActionInput(input.action);
    const file = checkFileInput(action, input.file);

    return {
        configFile,
        file,
        action,
    };
}

/**
 * Executes defineFalsePositives.
 * @param settings The settings for defineFalsePositives
 */
function executeOperation(settings: FalsePositivesSettings): number {
    let result: ShellString;
    switch (settings.action) {
        case FalsePositivesActionType.DEFINE:
            result = defineFalsePositives(settings.configFile, settings.file);
            break;
        case FalsePositivesActionType.MARK:
            result = markFalsePositives(settings.configFile, settings.file!);
            break;
        case FalsePositivesActionType.UNMARK:
            result = unmarkFalsePositives(settings.configFile, settings.file!);
            break;
    }
    const exitCode = result.code;
    logExitCode(exitCode);
    return exitCode;
}

/**
 * Executes actions after the defineFalsePositives finished.
 * @param exitCode exit code from defineFalsePositives
 */
async function postOperation(exitCode: number): Promise<void> {
    if (exitCode !== 0) {
        failAction(exitCode);
    }
}
