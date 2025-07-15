import { SECHUB_COMMANDS } from '../utils/sechubConstants';
import { changeServerUrl } from './changeServerUrl';
import { changeCredentials } from './changeCredentials';
import { selectProject } from './selectProject';
import { refreshServerView } from './refreshServerView';
import { fetchReportByUUID } from './fetchReportByUUID';
import { clearSecHubData } from './clearSecHubData';

export const commands = [
        { command: SECHUB_COMMANDS.changeServerUrl, action: changeServerUrl },
        { command: SECHUB_COMMANDS.changeCredentials, action: changeCredentials },
        { command: SECHUB_COMMANDS.selectProject, action: selectProject },
        { command: SECHUB_COMMANDS.refreshServerView, action: refreshServerView },
        { command: SECHUB_COMMANDS.fetchReportByUUID, action: fetchReportByUUID },
        { command: SECHUB_COMMANDS.clearSecHubData, action: clearSecHubData }
    ];
