// SPDX-License-Identifier: MIT

import { LaunchContext } from './launcher';
import * as core from '@actions/core';
import {execFileSync} from 'child_process';
import {sanitize} from './shell-arg-sanitizer';


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
            /* parameters */
            [
                '-configfile', configFileArgValue, 
                '-output', outputArgValue, addScmHistoryArg, 'scan'],

            /* options*/
            {
                env: process.env, // Pass all environment variables
                encoding: 'utf-8',
                stdio: 'pipe'
            }
        );

        core.info(output);

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
        const output = execFileSync(clientExecutablePath,
            ['-jobUUID', jobUUIDArgValue, '-project', projectArgValue, '--reportformat', reportFormatArgValue, 'getReport'],
            {
                env: process.env, // Pass all environment variables
                encoding: 'utf-8',
                stdio : 'pipe'
            }
        );
        core.info(output);
        core.debug('Get report executed successfully');
        context.lastClientExitCode = 0;
    } catch (error: any) {
        core.error(`Error executing getReport command: ${error.message}`);
        core.error(`Standard error: ${error.stderr}`);
        context.lastClientExitCode= error.status;
    }
}

/**
 * Executes the defineFalsePositives method of the SecHub CLI. Sets the client exitcode inside context.
 * @param context launch context
 */
export function defineFalsePositives(context: LaunchContext) {
    if (!context.defineFalsePositivesFile) {
        core.info('No define-false-positive file was specified. Skipping step defineFalsePositives...');
        context.lastClientExitCode = 0;
        return;
    }
    
    const clientExecutablePath = sanitize(context.clientExecutablePath);
    const projectIdValue = sanitize(context.projectName); 
    const defineFalsePositivesFile = sanitize(context.defineFalsePositivesFile);
    
    try {
        const output = execFileSync(clientExecutablePath,
            ['-project', projectIdValue, '-file', defineFalsePositivesFile, 'defineFalsePositives'],
            {
                env: process.env, // Pass all environment variables
                encoding: 'utf-8'
            }
        );
        core.info(output);

        core.info('defineFalsePositives executed successfully');
        context.lastClientExitCode = 0;
    } catch (error: any) {
        core.error(`Error executing defineFalsePositives command: ${error.message}`);
        core.error(`Standard error: ${error.stderr}`);
        context.lastClientExitCode = error.status;
    }
}