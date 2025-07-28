// SPDX-License-Identifier: MIT
import { SECHUB_COMMANDS } from '../utils/sechubConstants';
import { changeServerUrl } from './changeServerUrl';
import { changeCredentials } from './changeCredentials';
import { selectProject } from './selectProject';
import { refreshServerView } from './refreshServerView';
import { fetchReportByUUID } from './fetchReportByUUID';
import { clearSecHubData } from './clearSecHubData';
import { importReportFromFile } from './importReportFromFile';
import { openFinding } from './openFinding';
import { openCWEinBrowser } from './openCWEinBrowser';
import { openFindingCallStack } from './openFindingCallStack';
import { changeWebUiUrl } from './changeWebUiUrl';
import { openWebScanInInfoview } from './openWebScanInInfoview';

export const commands = [
    { command: SECHUB_COMMANDS.changeServerUrl, action: changeServerUrl },
    { command: SECHUB_COMMANDS.changeCredentials, action: changeCredentials },
    { command: SECHUB_COMMANDS.selectProject, action: selectProject },
    { command: SECHUB_COMMANDS.refreshServerView, action: refreshServerView },
    { command: SECHUB_COMMANDS.fetchReportByUUID, action: fetchReportByUUID },
    { command: SECHUB_COMMANDS.clearSecHubData, action: clearSecHubData },
    { command: SECHUB_COMMANDS.importReport, action: importReportFromFile },
    { command: SECHUB_COMMANDS.changeWebUiUrl, action: changeWebUiUrl }
];

export const hierachyCommands = [
        { command: SECHUB_COMMANDS.openFinding, action: openFinding }
];

export const reportItemCommands = [
    { command: SECHUB_COMMANDS.openCWEinBrowser, action: openCWEinBrowser },
    { command: SECHUB_COMMANDS.openFindingCallStack, action: openFindingCallStack },
    { command: SECHUB_COMMANDS.openWebScanInInfoview, action: openWebScanInInfoview }
];