// SPDX-License-Identifier: MIT

import * as shell from 'shelljs';

export class IntegrationTestContext {
   
    workspaceDir: string|undefined;
    
    serverPort: number|undefined;
    serverUserId: string|undefined;
    serverApiToken: string|undefined;
    
    public finish() {
        shell.env['SECHUB_SERVER'] = `https://localhost:${this.serverPort}`;
        shell.env['SECHUB_USERID'] = `${this.serverUserId}`;
        shell.env['SECHUB_APITOKEN'] = `${this.serverApiToken}`;
    }

}
