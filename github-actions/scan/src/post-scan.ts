// SPDX-License-Identifier: MIT

import * as core from '@actions/core';
import * as artifact from '@actions/artifact';
import * as shell from 'shelljs';
import { getReport } from './sechub-cli';
import {getWorkspaceDir} from './fs-helper';
import { logExitCode } from './log-helper';
import * as input from './input';
import * as fs from 'fs';

/**
 * Downloads the reports for the given formats.
 * @param formats formats in which the report should be downloaded
 */
export function downloadReports(formats: string[]): void {
    core.startGroup('Download Reports');
    if (formats.length === 0) {
        core.info('No more formats');
        return;
    }

    const jobUUID = getJobUUID();
    core.debug(jobUUID);
    formats.forEach((format) => {
        core.info(`Get Report as ${format}`);
        const exitCode = getReport(jobUUID, input.projectName, format).code;
        logExitCode(exitCode);
    });
    core.endGroup();
}

/**
 * Reads the job uuid from the json report.
 */
function getJobUUID(): string {
    const fileName = shell.exec(`ls ${getWorkspaceDir()} | grep sechub_report`).trim();
    core.debug('File name: ' + fileName);

    const filePath = `${getWorkspaceDir()}/${fileName}`;
    const json = JSON.parse(fs.readFileSync(filePath, 'utf8'));
    const jobUUID = json.jobUUID;
    core.debug('JobUUID: ' + jobUUID);

    return jobUUID;
}

/**
 * Uploads all given files as artifact
 * @param name Name for the zip file.
 * @param paths All file paths to include into the artifact.
 */
export async function uploadArtifact(name: string, paths: string[]) {
    core.startGroup('Upload artifacts');
    try {
        const artifactClient = artifact.create();
        const artifactName = name;
        const options = { continueOnError: true };

        const workspace = getWorkspaceDir();
        shell.exec(`ls ${workspace}`);
        core.debug('rootDirectory: ' + workspace);
        core.debug('files: ' + paths);

        await artifactClient.uploadArtifact(artifactName, paths, workspace, options);
    } catch (e: unknown) {
        const message = e instanceof Error ? e.message : 'Unknown error';
        core.error(`ERROR while uploading artifacts: ${message}`);
    }
    core.endGroup();
}
