// SPDX-License-Identifier: MIT

import * as os from 'os';

export function getPlatform() {
    return os.platform();
}

export function getPlatformDirectory(): string {
    const platform = getPlatform();
    const arch = os.arch();
    let platformDirectory = '';

    if (platform === 'darwin') {
        platformDirectory = arch === 'arm64' ? 'darwin-arm64' : 'darwin-amd64';
    } else if (platform === 'linux') {
        switch (arch) {
        case 'x32':
            platformDirectory = 'linux-386';
            break;
        case 'x64':
            platformDirectory = 'linux-amd64';
            break;
        case 'arm':
            platformDirectory = 'linux-arm';
            break;
        case 'arm64':
            platformDirectory = 'linux-arm64';
            break;
        }
    } else if (platform === 'win32') {
        platformDirectory = arch === 'x32' ? 'windows-386' : 'windows-amd64';
    } else {
        throw new Error(`Unsupported platform: ${platform}`);
    }

    return platformDirectory;
}