// SPDX-License-Identifier: MIT

import * as shell from 'shelljs';

export class IntegrationTestContext {
   
    serverVersion: string|undefined;
    serverPort: number|undefined;
    serverUserId: string|undefined;
    serverApiToken: string|undefined;

    pathToServerFolder: string|undefined;
    pathToServerExecutable: string|undefined;
    serverExecutableName: string|undefined;
    serverCertFilePath: string|undefined;

    public finish() {
        this.pathToServerFolder = `./runtime/server/${this.serverVersion}`;
        this.serverExecutableName = `sechub-server-${this.serverVersion}.jar`;
        this.pathToServerExecutable = `${this.pathToServerFolder}/${this.serverExecutableName}`;
        this.serverCertFilePath = `${this.pathToServerFolder}/generated-localhost-certificate.p12`;

        shell.env['SECHUB_SERVER'] = `https://localhost:${this.serverPort}`;
        shell.env['SECHUB_USERID'] = `${this.serverUserId}`;
        shell.env['SECHUB_APITOKEN'] = `${this.serverApiToken}`;
    }

}
