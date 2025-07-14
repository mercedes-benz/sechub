// SPDX-License-Identifier: MIT

import { LaunchContext } from './launcher';
import * as core from '@actions/core';
import { execFileSync, spawn, SpawnOptions } from 'child_process';
import { sanitize } from './shell-arg-sanitizer';
import path from 'path';
import { tmpdir } from 'os';
import { v4 as uuidv4 } from 'uuid';
import { readFileSync, openSync, closeSync, mkdtempSync } from './fs-wrapper';

/**
 * Executes the scan method of the SecHub CLI. Sets the client exitcode inside context.
 * @param context launch context
 */
export async function scan(context: LaunchContext) {
    const clientExecutablePath = sanitize(context.clientExecutablePath);
    const configFileArgValue = sanitize(context.configFileLocation || '');
    const outputArgValue = sanitize(context.workspaceFolder);
    const addScmHistoryArg = sanitize(context.inputData.addScmHistory === 'true' ? '-addScmHistory' : '');

    const tempDir = mkdtempSync(path.join(tmpdir(), 'sechub-scan-temp-dir'));
    const stdoutFile = path.join(tempDir, `output-${uuidv4()}.txt`);
    const stdoutFd = openSync(stdoutFile, 'a');
    const prefix = 'scan output';

    const args = [
        '-configfile', configFileArgValue,
        '-output', outputArgValue,
        ...(addScmHistoryArg ? [addScmHistoryArg] : []),
        'scan',
    ];

    core.info(`Running: ${clientExecutablePath} ${args.join(' ')}`);

    try {
        const exitCode = await spawnAndWait(clientExecutablePath, args, {
            stdio: ['ignore', stdoutFd, stdoutFd],
            env: process.env,
        });

        const output = logAndCloseStdOutFile(prefix, stdoutFd, stdoutFile);
        context.lastClientExitCode = exitCode;
        context.jobUUID = extractJobUUID(output);
        core.info('Scan completed successfully');
    } catch (err: any) {
        const output = logAndCloseStdOutFile(prefix, stdoutFd, stdoutFile);
        context.lastClientExitCode = 1;
        context.jobUUID = extractJobUUID(output);
        core.error(`Scan failed: ${err.message}`);
        throw err;
    }
}

export function spawnAndWait(command: string, args: string[], options: SpawnOptions = {}): Promise<number> {
    return new Promise((resolve, reject) => {
        const child = spawn(command, args, options);

        const handleSignal = (signal: NodeJS.Signals) => {
            console.warn(`Received ${signal}, forwarding to child`);
            child.kill(signal);
        };

        process.once('SIGINT', handleSignal);
        process.once('SIGTERM', handleSignal);

        child.on('exit', (code, signal) => {
            process.removeListener('SIGINT', handleSignal);
            process.removeListener('SIGTERM', handleSignal);
            if (signal) {
                return reject(new Error(`Process terminated by signal: ${signal}`));
            }
            resolve(code ?? 0);
        });

        child.on('error', (err) => {
            process.removeListener('SIGINT', handleSignal);
            process.removeListener('SIGTERM', handleSignal);
            reject(err);
        });
    });
}

function logAndCloseStdOutFile(prefix: string, stdOutFd:number, stdOutFile: string) : string {
    
    closeSync(stdOutFd);

    const output = readFileSync(stdOutFile, 'utf8');
    
    core.info(`${prefix }:\n${ output }`);

    return output;
}

export function extractJobUUID(output: string): string {
    const jobPrefix = 'job:';
    core.debug(`Extracting job uuid from scan output: ${output}`);

    const index1 = output.indexOf(jobPrefix);

    if (index1 > -1) {
        const index2 = output.indexOf('\n', index1);
        if (index2 > -1) {
            const extracted = output.substring(index1 + jobPrefix.length, index2);

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

    const tempDir = mkdtempSync(path.join(tmpdir(), 'sechub-scan-temp-dir'));
    const stdoutFile = path.join(tempDir, `output-${uuidv4()}.txt`);
    const stdoutFd = openSync(stdoutFile, 'a');
    const prefix='get-report-output';

    try {
        execFileSync(clientExecutablePath,
            ['-jobUUID', jobUUIDArgValue, '-project', projectArgValue, '--reportformat', reportFormatArgValue, 'getReport'],
            {
                env: process.env, // Pass all environment variables
                encoding: 'utf-8',
                stdio: ['ignore', stdoutFd, stdoutFd],
            }
        );
        logAndCloseStdOutFile(prefix, stdoutFd, stdoutFile);

        core.debug('Get report executed successfully');
        context.lastClientExitCode = 0;
    } catch (error: any) {
        logAndCloseStdOutFile(prefix, stdoutFd, stdoutFile);
        
        core.error(`Error executing getReport command: ${error.message}`);

        context.lastClientExitCode = error.status;
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

    const tempDir = mkdtempSync(path.join(tmpdir(), 'sechub-define-false-positives-temp-dir'));
    const stdoutFile = path.join(tempDir, `output-${uuidv4()}.txt`);
    const stdoutFd = openSync(stdoutFile, 'a');
    const prefix = 'define-false-positives-output';
    try {
        execFileSync(clientExecutablePath,
            ['-project', projectIdValue, '-file', defineFalsePositivesFile, 'defineFalsePositives'],
            {
                env: process.env, // Pass all environment variables
                encoding: 'utf-8',
                stdio: ['ignore', stdoutFd, stdoutFd],
            }
        );
        logAndCloseStdOutFile(prefix, stdoutFd, stdoutFile);

        core.info('defineFalsePositives executed successfully');
        context.lastClientExitCode = 0;
    } catch (error: any) {
        logAndCloseStdOutFile(prefix, stdoutFd, stdoutFile);

        core.error(`Error executing defineFalsePositives command: ${error.message}`);
        context.lastClientExitCode = error.status;
    }
}