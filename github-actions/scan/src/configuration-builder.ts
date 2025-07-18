// SPDX-License-Identifier: MIT

import * as core from '@actions/core';
import * as shell from 'shelljs';
import { ScanType, ContentType, SecHubConfigurationModel } from './configuration-model';
import * as cm from './configuration-model';

/**
 * Creates the sechub.json configuration file with the given user input values.
 *
 * @param secHubJsonTargetFilePath The path where the sechub.json file should be created
 * @param data The value to build the json from
 */
export function createSecHubConfigJsonFile(secHubJsonTargetFilePath: string, data: SecHubConfigurationModelBuilderData) {
    core.info('Config-Path was not found. Config will be created at ' + secHubJsonTargetFilePath);
    const stringifiedSecHubJson = createSecHubConfigJsonString(data);
    core.debug('SecHub-Config: ' + stringifiedSecHubJson);

    shell.ShellString(stringifiedSecHubJson).to(secHubJsonTargetFilePath);
}

/**
 * Creates the SecHub configuration as JSON string.
 * 
 * @param data The value to build the json from.
 * @returns  The SecHub configuration as JSON string.
 */
export function createSecHubConfigJsonString(data: SecHubConfigurationModelBuilderData): string{
    const secHubJson = createSecHubConfigurationModel(data);
    return JSON.stringify(secHubJson);
}


export class SecHubConfigurationModelBuilderData {

    static DEFAULT_SCAN_TYPE=ScanType.CODE_SCAN;  // per default only code scan
    static DEFAULT_CONTENT_TYPE=ContentType.SOURCE;  // per default source

    includeFolders: string[] = [];
    excludeFolders: string[] = [];

    contentType: string = SecHubConfigurationModelBuilderData.DEFAULT_CONTENT_TYPE; 
    scanTypes: string[] = [SecHubConfigurationModelBuilderData.DEFAULT_SCAN_TYPE];
}

/**
 * Creates a sechub configuration model object for given user input values.
 * 
 * @param includeFolders Which folders should be included
 * @param excludeFolders Which folders should be excluded
 * 
 * @returns model
 */
export function createSecHubConfigurationModel(builderData: SecHubConfigurationModelBuilderData): SecHubConfigurationModel {
    const model = new SecHubConfigurationModel();

    const referenceName = 'reference-data-1';

    createSourceOrBinaryDataReference(referenceName, builderData, model);

    if (builderData.scanTypes?.indexOf(ScanType.CODE_SCAN) != -1) {
        const codescan = new cm.CodeScan();
        codescan.use = [referenceName];
        model.codeScan = codescan;
    }
    if (builderData.scanTypes?.indexOf(ScanType.LICENSE_SCAN) != -1) {
        const licenseScan = new cm.LicenseScan();
        licenseScan.use = [referenceName];
        model.licenseScan = licenseScan;
    }
    if (builderData.scanTypes?.indexOf(ScanType.SECRET_SCAN) != -1) {
        const secretScan = new cm.SecretScan();
        secretScan.use = [referenceName];
        model.secretScan = secretScan;
    }
    if (builderData.scanTypes?.indexOf(ScanType.IAC_SCAN) != -1) {
        const iacScan = new cm.IacScan();
        iacScan.use = [referenceName];
        model.iacScan = iacScan;
    }

    return model;
}

function createSourceOrBinaryDataReference(referenceName: string, builderData: SecHubConfigurationModelBuilderData, model: SecHubConfigurationModel) {
    if (builderData.contentType == cm.ContentType.SOURCE) {

        const sourceData1 = new cm.SourceData();
        sourceData1.name = referenceName;

        sourceData1.fileSystem.folders = builderData.includeFolders;
        sourceData1.excludes = builderData.excludeFolders;

        model.data.sources = [sourceData1];

    } else if (builderData.contentType == cm.ContentType.BINARIES) {
        const binaryData1 = new cm.BinaryData();
        binaryData1.name = referenceName;

        binaryData1.fileSystem.folders = builderData.includeFolders;
        binaryData1.excludes = builderData.excludeFolders;

        model.data.binaries = [binaryData1];
    }
}

