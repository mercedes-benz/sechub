import { changeWebUiUrl } from "src/commands/changeWebUiUrl";

// SPDX-License-Identifier: MIT
export const SECHUB_CREDENTIAL_KEYS = {
    serverUrl: 'sechubServerUrl',
    webUiUrl: 'sechubWebUiUrl',
    username: 'sechubUsername',
    apiToken: 'sechubApiToken',
};

export const SECHUB_REPORT_KEYS = {
    selectedProject: 'selectedProject',
};

export const SECHUB_COMMANDS = {
    changeCredentials: 'sechub.changeCredentials',
    changeServerUrl: 'sechub.changeServerUrl',
    selectProject: 'sechub.selectProject',
    refreshServerView: 'sechub.refreshServerView',
    fetchReportByUUID: 'sechub.fetchReportByUUID',
    clearSecHubData: 'sechub.clearSecHubData',
    importReport: 'sechub.importReportFile',
    openCWEinBrowser: 'sechub.openCWEinBrowser',
    changeWebUiUrl: 'sechub.changeWebUiUrl',
    // internal commands which can not be used by users directly, but are used in the extension
    openFindingCallStack: 'sechub-intern.openFindingCallStack',
    openFinding: 'sechub-intern.openFinding',
    openWebScanInInfoview: 'sechub-intern.openWebScanInInfoview',
};

export const SECHUB_VIEW_IDS = {
    serverView: 'sechub.serverWebView',
    reportView: 'sechub.reportView',
    callHierarchyView: 'sechub.callHierarchyView',
    infoView: 'sechub.infoView',
};