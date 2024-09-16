// SPDX-License-Identifier: MIT

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
        configPath: getEnvVar(PARAM_CONFIG_PATH),
        url: getEnvVar(PARAM_SECHUB_SERVER_URL),
        apiToken: getEnvVar(PARAM_API_TOKEN),
        user: getEnvVar(PARAM_SECHUB_USER),
        projectName: getEnvVar(PARAM_PROJECT_NAME),
        sechubCLIVersion: getEnvVar(PARAM_CLIENT_VERSION),
        addScmHistory: getEnvVar(PARAM_ADD_SCM_HISTORY),
        debug: getEnvVar(PARAM_DEBUG),
        includeFolders: getEnvVar(PARAM_INCLUDED_FOLDERS),
        excludeFolders: getEnvVar(PARAM_EXCLUDED_FOLDERS),
        reportFormats: getEnvVar(PARAM_REPORT_FORMATS),
        failJobOnFindings: getEnvVar(PARAM_FAIL_JOB_ON_FINDING),
        trustAll: getEnvVar(PARAM_TRUST_ALL),
        scanTypes: getEnvVar(PARAM_SCAN_TYPES),
        contentType: getEnvVar(PARAM_CONTENT_TYPE),
    };
}

/**
 * Retrieves the value of an environment variable.
 * @param {string} variableName - The name of the environment variable.
 * @returns {string} - The value of the environment variable (empty if not present)
 */
function getEnvVar(variableName: string): string {
    return process.env[variableName] || '';
}

