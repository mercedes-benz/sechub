// SPDX-License-Identifier: MIT

import * as core from '@actions/core';

export const PARAM_CONFIG_PATH = 'config-path';
export const PARAM_SECHUB_SERVER_URL = 'url';
export const PARAM_API_TOKEN = 'api-token';
export const PARAM_SECHUB_USER = 'user';
export const PARAM_PROJECT_NAME = 'project-name';
export const PARAM_CLIENT_VERSION = 'version';
export const PARAM_ADD_SCM_HISTORY = 'add-scm-history';
export const PARAM_DEBUG = 'debug';
export const PARAM_INCLUDED_FOLDERS = 'include-folders';
export const PARAM_EXCLUDED_FOLDERS = 'exclude-folders';
export const PARAM_REPORT_FORMATS = 'report-formats';
export const PARAM_FAIL_JOB_ON_FINDING = 'fail-job-with-findings';
export const PARAM_TRUST_ALL = 'trust-all';

export const PARAM_SCAN_TYPES = 'scan-types';
export const PARAM_CONTENT_TYPE = 'content-type';

export interface GitHubInputData {
    configPath: string;
    url: string;
    apiToken: string;
    user: string;
    projectName: string;
    sechubCLIVersion: string;
    addScmHistory: string;
    debug: string;
    includeFolders: string;
    excludeFolders: string;
    reportFormats: string;
    failJobOnFindings: string;
    trustAll: string;
    scanTypes: string;
    contentType: string;

}

export const INPUT_DATA_DEFAULTS: GitHubInputData = {
    configPath: '',
    url: '',
    apiToken: '',
    user: '',
    projectName: '',
    sechubCLIVersion: 'latest',
    addScmHistory: 'false',
    debug: '',
    includeFolders: '',
    excludeFolders: '',
    reportFormats: '',
    failJobOnFindings: '',
    trustAll: '',
    scanTypes: '',
    contentType: '',

};

export function resolveGitHubInputData(): GitHubInputData {
    return {
        configPath: getParam(PARAM_CONFIG_PATH),
        url: getParam(PARAM_SECHUB_SERVER_URL),
        apiToken: getParam(PARAM_API_TOKEN),
        user: getParam(PARAM_SECHUB_USER),
        projectName: getParam(PARAM_PROJECT_NAME),
        sechubCLIVersion: getParam(PARAM_CLIENT_VERSION),
        addScmHistory: getParam(PARAM_ADD_SCM_HISTORY),
        debug: getParam(PARAM_DEBUG),
        includeFolders: getParam(PARAM_INCLUDED_FOLDERS),
        excludeFolders: getParam(PARAM_EXCLUDED_FOLDERS),
        reportFormats: getParam(PARAM_REPORT_FORMATS),
        failJobOnFindings: getParam(PARAM_FAIL_JOB_ON_FINDING),
        trustAll: getParam(PARAM_TRUST_ALL),
        scanTypes: getParam(PARAM_SCAN_TYPES),
        contentType: getParam(PARAM_CONTENT_TYPE),
    };
}

/**
 * Get the value for the given parameter from the environment variables or the GitHub Action input.
 * Returns an empty string if no value is found.
 *
 * @param {string} param - The name of the parameter to search for
 * @returns {string} - The value of the parameter (empty if not present)
 */
function getParam(param: string): string {
    const envVar =  process.env[param];

    if (envVar && envVar.length > 0) {
        return envVar;
    }

    return core.getInput(param);
}

