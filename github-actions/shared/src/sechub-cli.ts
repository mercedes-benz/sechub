// SPDX-License-Identifier: MIT

import * as shell from 'shelljs';
import { ShellString } from 'shelljs';
import {getWorkspaceParentDir} from "./fs-helper";

export const secHubCli = `${getWorkspaceParentDir()}/platform/linux-386/sechub`;

/**
 * Executes the scan method of the SecHub CLI.
 * @param parameter Parameters to execute the scan with
 * @param format Report format that should be fetched
 */
export function scan(parameter: string | null, format: string): ShellString {
    return shell.exec(`${secHubCli} ${parameter} -reportformat ${format} scan`);
}

/**
 * Executes the getReport method of the SecHub CLI.
 * @param jobUUID job UUID for which the report should be downloaded
 * @param projectName name of the project for which the report should be downloaded
 * @param format format in which the report should be downloaded
 */
export function getReport(jobUUID: string, projectName: string, format: string): ShellString {
    return shell.exec(`${secHubCli} -jobUUID ${jobUUID} -project ${projectName} --reportformat ${format} getReport`);
}

/**
 * Executes the markFalsePositives method of the SecHub CLI.
 * @param falsePositivePath path to the false positive file
 */
export function markFalsePositives(falsePositivePath: string): ShellString {
    return shell.exec(`${secHubCli} -file ${falsePositivePath} markFalsePositives`);
}
