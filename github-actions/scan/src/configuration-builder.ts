// SPDX-License-Identifier: MIT

import * as core from '@actions/core';
import * as shell from 'shelljs';
import { SecHubConfigurationModel } from './configuration-model';

/**
 * Creates the sechub.json configuration file with the given user input values.
 * 
 * @param includeFolders Which folders should be included
 * @param excludeFolders Which folders should be excluded
 */
export function createSecHubConfigJsonFile(secHubJsonFilePath:string, includeFolders: string[] | null, excludeFolders: string[] | null) {
    core.info('Config-Path was not found. Config will be created at '+ secHubJsonFilePath);
    const secHubJson = createSecHubConfigurationModel(includeFolders, excludeFolders);
    const stringifiedSecHubJson = JSON.stringify(secHubJson);
    core.debug('SecHub-Config: ' + stringifiedSecHubJson);

    shell.ShellString(stringifiedSecHubJson).to(secHubJsonFilePath);
}

/**
 * Creates a sechub configuration model object for given user input values.
 * 
 * @param includeFolders Which folders should be included
 * @param excludeFolders Which folders should be excluded
 * 
 * @returns model
 */
export function createSecHubConfigurationModel(includeFolders: string[] | null, excludeFolders: string[] | null): SecHubConfigurationModel {
    const sechubJson: SecHubConfigurationModel = {
        'apiVersion' : '1.0'
    };

    if (sechubJson.codeScan==null){
        sechubJson.codeScan={
        };
    }
    if (includeFolders) {

        if (sechubJson.codeScan.fileSystem==null){
            sechubJson.codeScan.fileSystem={
            };
        }

        sechubJson.codeScan.fileSystem.folders = includeFolders;
    }

    if (excludeFolders) {
        sechubJson.codeScan.excludes = excludeFolders;
    }
    return sechubJson;
}
