// SPDX-License-Identifier: MIT

import * as shell from 'shelljs';
import * as core from '@actions/core';
import * as path from 'path';
import * as fs_extra from 'fs-extra';
import axios from 'axios';
import * as util from 'util';
import * as extract from 'extract-zip';

const writeFile = util.promisify(fs_extra.writeFile);

/**
 * Get workspace directory.
 */
export function getWorkspaceDir(): string {
    /* github workspace is something like:
    * /home/runner/work/sechub/
     * means we are at root level when the action is used
     * from outside
     * Means: we have a /home/runner/work/sechub/runtime folder
     *                  /home/runner/work/other-repo/runtime
     *
     * For local builds/runs this must be done as well.
     *
     */
    const workspace=shell.env['GITHUB_WORKSPACE'];
    if (workspace==null){
        /* not set, means local,we are inside github-actions/scan */
        return '../..';
    }else{
        return `${workspace}`;
    }

}

export function ensureDirectorySync(path: string) {
    try {
        fs_extra.ensureDirSync(path);
        core.debug(`Ensured directory at path: ${path}`)
    } catch (error) {
        throw new Error(`Error ensuring directory at path: ${path} with error: ${error}`);
    }
}

/**
 * Grant read, write, and execute permissions to the file.
 * @param path
 */
export function chmodSync(path: string) {
    try {
        fs_extra.chmodSync(path, 0o755);
        core.debug(`Grant permissions for file at path: ${path}`)
    } catch (error) {
        throw new Error(`Error granting permission for file at path: ${path} with error: ${error}`);
    }
}

export async function downloadFile(url: string, dest: string) {
    try {
        const response = await axios.get(url, { responseType: 'arraybuffer' });
        await writeFile(dest, response.data);
    } catch (err) {
        throw new Error(`Error downloading file from url: ${url} to destination: ${dest} with error: ${err}`);
    }
}

export async function unzipFile(zipPath: string, dest: string) {
    try {
        await extract(zipPath, { dir: path.resolve(dest) });
        core.debug(`Extracted zip file to: ${dest}`);
    } catch (err) {
        throw new Error(`Error extracting zip file: ${zipPath} to: ${dest} with error: ${err}`);
    }
}

/**
 * Get all files in current directory for given pattern.
 * @param pattern Used to filter files
 */
export function getFiles(pattern: string): string[] {
    const reportFiles: string[] = [];

    shell.ls(pattern).forEach(function (file) {
        core.debug('file: ' + file);
        reportFiles.push(`${file}`);
    });

    return reportFiles;
}