// SPDX-License-Identifier: MIT

import { LaunchContext } from './launcher';
import * as core from '@actions/core';
import {execFileSync} from 'child_process';
import {sanitize} from "./shell-arg-sanitizer";

/**
 * Executes the scan method of the SecHub CLI. Sets the client exitcode inside context.
 * @param context launch context
 */
export function scan(context: LaunchContext) {

    const clientExecutablePath = sanitize(context.clientExecutablePath);
    const configFileArgValue = sanitize(context.configFileLocation ? context.configFileLocation : '');
    const outputArgValue = sanitize(context.workspaceFolder);
    const addScmHistoryArg = sanitize(context.inputData.addScmHistory === 'true' ? '-addScmHistory' : '');

    try {
        const output = execFileSync(clientExecutablePath,
            ['-configfile', configFileArgValue, '-output', outputArgValue, addScmHistoryArg, 'scan'],
            {
                env: {
                    SECHUB_SERVER: process.env.SECHUB_SERVER,
                    SECHUB_USERID: process.env.SECHUB_USERID,
                    SECHUB_APITOKEN: process.env.SECHUB_APITOKEN,
                    SECHUB_PROJECT: process.env.SECHUB_PROJECT,
                    SECHUB_DEBUG: process.env.SECHUB_DEBUG,
                    SECHUB_TRUSTALL: process.env.SECHUB_TRUSTALL,
                },
                encoding: 'utf-8'
            }
        );

        core.info('Scan executed successfully');
        context.lastClientExitCode = 0;
        context.jobUUID=extractJobUUID(output);
    } catch (error: any) {
        core.error(`Error executing scan command: ${error.message}`);
        core.error(`Standard error: ${error.stderr}`);
        context.lastClientExitCode= error.status;
        context.jobUUID=extractJobUUID(error.stdout);
    }
}

export function extractJobUUID(output: string): string{
    const jobPrefix='job:';
    core.debug(`Extracting job uuid from scan output: ${output}`);

    const index1 =output.indexOf(jobPrefix);
    
    if (index1>-1){
        const index2 = output.indexOf('\n', index1);
        if (index2>-1){
            const extracted=output.substring(index1+jobPrefix.length,index2);

            const jobUUID = extracted.trim();
            core.debug(`Extracted job uuid from scan output: ${jobUUID}`);
            
            return jobUUID;
        }
    }
    core.debug('extractJobUUID: no job uuid found!');
    return '';
}

/**
 * Executes the getReport method of the SecHub CLI. Sets the client exitcode inside context.
 * @param jobUUID job UUID for which the report should be downloaded
 * @param reportFormat format in which the report should be downloaded
 * @param context launch context
*/
export function getReport(jobUUID: string, reportFormat: string, context: LaunchContext) {
    const clientExecutablePath = sanitize(context.clientExecutablePath);
    const jobUUIDArgValue = sanitize(jobUUID);
    const projectArgValue = sanitize(context.projectName);
    const reportFormatArgValue = sanitize(reportFormat);

    try {
        execFileSync(clientExecutablePath,
            ['-jobUUID', jobUUIDArgValue, '-project', projectArgValue, '--reportformat', reportFormatArgValue, 'getReport'],
            {
                env: {
                    SECHUB_SERVER: process.env.SECHUB_SERVER,
                    SECHUB_USERID: process.env.SECHUB_USERID,
                    SECHUB_APITOKEN: process.env.SECHUB_APITOKEN,
                    SECHUB_PROJECT: process.env.SECHUB_PROJECT,
                    SECHUB_DEBUG: process.env.SECHUB_DEBUG,
                    SECHUB_TRUSTALL: process.env.SECHUB_TRUSTALL,
                },
                encoding: 'utf-8'
            }
        );

        core.debug('Get report executed successfully');
        context.lastClientExitCode = 0;
    } catch (error: any) {
        core.error(`Error executing getReport command: ${error.message}`);
        core.error(`Standard error: ${error.stderr}`);
        context.lastClientExitCode= error.status;
    }
}