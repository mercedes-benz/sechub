// SPDX-License-Identifier: MIT

import * as core from '@actions/core';
import * as shell from 'shelljs';
import * as settingsFile from './settings.json';
import { SecHubJson } from './types';
import * as configFile from './sechub.json';
import { secHubCli } from "./sechub-cli";
import { getWorkspaceParentDir } from "./fs-helper";

/**
 * Downloads a release for the SecHub CLI.
 * @param version The version that should be downloaded
 */
export function downloadRelease(version: string): void {
    const zipUrl = `https://github.com/mercedes-benz/sechub/releases/download/v${version}-client/sechub-cli-${version}.zip`;
    core.debug('SecHub-Url: ' + zipUrl);
    const secHubZip = `${getWorkspaceParentDir()}/sechub.zip`;
    shell.exec(`curl -L ${zipUrl} -o ${secHubZip}`);
    shell.exec(`unzip -o ${secHubZip} -d ${getWorkspaceParentDir()}`);
    shell.exec(`chmod +x ${secHubCli}`);
}

/**
 * Creates the sechub.json with the given user input values.
 * @param includeFolders Which folders should be included
 * @param excludeFolders Which folders should be excluded
 */
export function createSecHubJsonFile(includeFolders: string[] | null, excludeFolders: string[] | null) {
    core.info('Config-Path was not found. Config will be manually created...');
    const secHubJson = createSecHubConfigModel(includeFolders, excludeFolders);
    const stringifiedSecHubJson = JSON.stringify(secHubJson);
    core.debug('SecHub-Config: ' + stringifiedSecHubJson);

    shell.ShellString(stringifiedSecHubJson).to(settingsFile.secHubJsonFileName);
}

/**
 * Creates the SecHub configuration model with the given user input values.
 * @param includeFolders Which folders should be included
 * @param excludeFolders Which folders should be excluded
 */
export function createSecHubConfigModel(includeFolders: string[] | null, excludeFolders: string[] | null): SecHubJson {
    const sechubJson: SecHubJson = configFile;
    if (includeFolders) {
        sechubJson.codeScan.fileSystem.folders = includeFolders;
    }

    if (excludeFolders) {
        sechubJson.codeScan.excludes = excludeFolders;
    }
    return sechubJson;
}
