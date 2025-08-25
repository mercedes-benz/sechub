// SPDX-License-Identifier: MIT
export const SECHUB_API_CLIENT_CONFIG_KEYS = {
	username: 'sechubUsername',
	apiToken: 'sechubApiToken',
	serverUrl: 'sechubServerUrl',
};

export const SECHUB_CONTEXT_STORAGE_KEYS = {
	selectedProject: 'selectedProject',
	webUiUrl: 'sechubWebUiUrl',
	falsePositiveConfiguration: 'selectedProjectFPConfiguration',
	falsePositiveCache: 'falsePositiveCache',
};

export const SECHUB_COMMANDS = {
	changeCredentials: 'sechub.changeCredentials',
	changeServerUrl: 'sechub.changeServerUrl',
	selectProject: 'sechub.selectProject',
	refreshServerView: 'sechub.refreshServerView',
	fetchReportByUUID: 'sechub.fetchReportByUUID',
	clearSecHubData: 'sechub.clearSecHubData',
	importReport: 'sechub.importReportFile',
	changeWebUiUrl: 'sechub.changeWebUiUrl',
	// internal commands which can not be used by users directly, but are used in the extension
	fetchFalsePositives: 'sechub-intern.fetchFalsePositives',
	markFalsePositives: 'sechub-intern.markFalsePositives',
	openFindingCallStack: 'sechub-intern.openFindingCallStack',
	openFinding: 'sechub-intern.openFinding',
	openWebScanInInfoview: 'sechub-intern.openWebScanInInfoview',
	explainVulnerabilityByAi: 'sechub-intern.explainVulnerabilityByAi',
};

export const SECHUB_VIEW_IDS = {
	serverView: 'sechub.serverWebView',
	reportView: 'sechub.reportView',
	callHierarchyView: 'sechub.callHierarchyView',
	infoView: 'sechub.infoView',
};
