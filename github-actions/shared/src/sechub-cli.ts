// SPDX-License-Identifier: MIT

import * as shell from 'shelljs';
import { ShellString } from 'shelljs';
import {ReportFormat} from "./report-formats";

const secHubCli = 'sechub';

/**
 * Executes the scan method of the SecHub CLI.
 * @param configFile optional path to the sechub configuration file
 * @param format Report format that should be fetched
 */
export function scan(configFile: string | null, format: ReportFormat | null): ShellString {
    const args: string[] = [];
    if (format) {
        args.push('-reportformat', format);
    }
    return executeSecHubCliAction('scan', configFile, args);
}

/**
 * Executes the getReport method of the SecHub CLI.
 * @param configFile optional path to the sechub configuration file
 * @param jobUUID job UUID for which the report should be downloaded
 * @param projectName name of the project for which the report should be downloaded
 */
export function getReport(configFile: string | null, jobUUID: string, projectName: string | null): ShellString {
    const args: string[] = [
        '-jobUUID',
        jobUUID
    ];
    if (projectName) {
        args.push('-project', projectName);
    }
    return executeSecHubCliAction('getReport', configFile, args);
}

/**
 * Executes the markFalsePositives method of the SecHub CLI.
 * @param configFile optional path to the sechub configuration file
 * @param falsePositivePath path to the false positive file
 */
export function markFalsePositives(configFile: string | null, falsePositivePath: string): ShellString {
    const args: string[] = ['-file', falsePositivePath];
    return executeSecHubCliAction('markFalsePositives', configFile, args);
}

/**
 * Executes the umarkFalsePositives method of the SecHub CLI.
 * @param configFile optional path to the sechub configuration file
 * @param falsePositivePath path to the false positive file
 */
export function unmarkFalsePositives(configFile: string | null, falsePositivePath: string): ShellString {
    const args: string[] = ['-file', falsePositivePath];
    return executeSecHubCliAction('unmarkFalsePositives', configFile, args);
}

/**
 * Executes the defineFalsePositives method of the SecHub CLI.
 * @param configFile optional path to the sechub configuration file
 * @param falsePositivePath optional path to the false positive file
 */
export function defineFalsePositives(configFile: string | null, falsePositivePath: string | null): ShellString {
    const args: string[] = [];
    if (falsePositivePath) {
        args.push('-file', falsePositivePath);
    }
    return executeSecHubCliAction('defineFalsePositives', configFile, args);
}

function executeSecHubCliAction(action: string, configFile: string | null, args: string[]): ShellString {
    const resultArgs = [secHubCli, ...args];
    if (configFile) {
        resultArgs.push('-configfile', configFile);
    }
    resultArgs.push(action);
    return shell.exec(resultArgs.join(' '));

}
