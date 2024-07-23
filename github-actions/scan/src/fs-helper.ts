// SPDX-License-Identifier: MIT

import * as shell from 'shelljs';
import * as core from '@actions/core';

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
