import { changeCredentials } from "src/commands/changeCredentials";

export const SECHUB_CREDENTIAL_KEYS = {
    serverUrl: 'sechubServerUrl',
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
    importReport: 'sechubReportView.importReportFile',
    openCWEinBrowser: 'sechubReportView.openCWEinBrowser',
    showCallHierarchy: 'sechubCallHierarchyView.showInEditor',
};

export const SECHUB_VIEW_IDS = {
    serverView: 'sechub.serverWebView'
};