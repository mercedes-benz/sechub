// SPDX-License-Identifier: MIT

import * as core from '@actions/core';

export const PARAM_CONFIG_PATH = 'config-path';
export const PARAM_SECHUB_SERVER_URL = 'url';
export const PARAM_API_TOKEN = 'api-token';
export const PARAM_SECHUB_USER = 'user';
export const PARAM_PROJECT_NAME = 'project-name';
export const PARAM_CLIENT_VERSION = 'version';
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
        configPath: core.getInput(PARAM_CONFIG_PATH),
        url: core.getInput(PARAM_SECHUB_SERVER_URL),
        apiToken: core.getInput(PARAM_API_TOKEN),
        user: core.getInput(PARAM_SECHUB_USER),
        projectName: core.getInput(PARAM_PROJECT_NAME),
        sechubCLIVersion: core.getInput(PARAM_CLIENT_VERSION),
        debug: core.getInput(PARAM_DEBUG),
        includeFolders: core.getInput(PARAM_INCLUDED_FOLDERS),
        excludeFolders: core.getInput(PARAM_EXCLUDED_FOLDERS),
        reportFormats: core.getInput(PARAM_REPORT_FORMATS),
        failJobOnFindings: core.getInput(PARAM_FAIL_JOB_ON_FINDING),
        trustAll: core.getInput(PARAM_TRUST_ALL),
        scanTypes: core.getInput(PARAM_SCAN_TYPES),
        contentType: core.getInput(PARAM_CONTENT_TYPE),
    };
}


