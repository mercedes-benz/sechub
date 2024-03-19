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
export function createSecHubConfigJsonFile(secHubJsonFilePath:string, data: SecHubConfigurationModelBuilderData) {
    core.info('Config-Path was not found. Config will be created at '+ secHubJsonFilePath);
    const secHubJson = createSecHubConfigurationModel(data);
    const stringifiedSecHubJson = JSON.stringify(secHubJson);
    core.debug('SecHub-Config: ' + stringifiedSecHubJson);

    shell.ShellString(stringifiedSecHubJson).to(secHubJsonFilePath);
}

export class SecHubConfigurationModelBuilderData{

    includeFolders: string[] = [];
    excludeFolders: string[] = [];
}

/**
 * Creates a sechub configuration model object for given user input values.
 * 
 * @param includeFolders Which folders should be included
 * @param excludeFolders Which folders should be excluded
 * 
 * @returns model
 */
export function createSecHubConfigurationModel(data: SecHubConfigurationModelBuilderData): SecHubConfigurationModel {
    const model: SecHubConfigurationModel = {
        'apiVersion' : '1.0'
    };

    if (model.codeScan==null){
        model.codeScan={
        };
    }
    if (data.includeFolders) {

        if (model.codeScan.fileSystem==null){
            model.codeScan.fileSystem={
            };
        }

        model.codeScan.fileSystem.folders = data.includeFolders;
    }

    if (data.excludeFolders) {
        model.codeScan.excludes = data.excludeFolders;
    }
    return model;
}
