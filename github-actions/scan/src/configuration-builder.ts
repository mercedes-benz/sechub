// SPDX-License-Identifier: MIT

import * as core from '@actions/core';
import * as shell from 'shelljs';
import { ScanType, SecHubConfiguration, SecHubCodeScanConfiguration, SecHubLicenseScanConfiguration, SecHubSecretScanConfiguration, SecHubIacScanConfiguration, SecHubSourceDataConfiguration } from 'sechub-openapi-ts-client';
import { ContentType } from './content-type';

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

    static DEFAULT_SCAN_TYPE = ScanType.CodeScan;  // per default only code scan
    static DEFAULT_CONTENT_TYPE = ContentType.SOURCE;  // per default sources

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
export function createSecHubConfigurationModel(builderData: SecHubConfigurationModelBuilderData): SecHubConfiguration {
    let model: SecHubConfiguration = {
        projectId: '',
        apiVersion: '1.0',
        data: {
            sources: undefined,
            binaries: undefined
        },
    };

    const referenceName = 'reference-data-1';

    createSourceOrBinaryDataReference(referenceName, builderData, model);

    if (isStringInArrayIgnoreCase(ScanType.CodeScan, builderData.scanTypes)) {
        const codeScan: SecHubCodeScanConfiguration = {
            use: [referenceName]
        };
        model.codeScan = codeScan;
    }
    if (isStringInArrayIgnoreCase(ScanType.LicenseScan, builderData.scanTypes)) {
        const licenseScan: SecHubLicenseScanConfiguration = {
            use: [referenceName]
        };
        model.licenseScan = licenseScan;
    }
    if (isStringInArrayIgnoreCase(ScanType.SecretScan, builderData.scanTypes)) {
        const secretScan: SecHubSecretScanConfiguration = {
            use: [referenceName]
        };
        model.secretScan = secretScan;
    }
    if (isStringInArrayIgnoreCase(ScanType.IacScan, builderData.scanTypes)) {
        const iacScan: SecHubIacScanConfiguration = {
            use: [referenceName]
        };
        model.iacScan = iacScan;
    }

    return model;
}

function createSourceOrBinaryDataReference(referenceName: string, builderData: SecHubConfigurationModelBuilderData, model: SecHubConfiguration) {
    if (!model.data) {
        model.data = {};
    }
    if (builderData.contentType === ContentType.SOURCE) {
        const sourceData: SecHubSourceDataConfiguration = {
            name: referenceName,
            fileSystem: {
                folders: builderData.includeFolders
            },
            excludes: builderData.excludeFolders
        };
        model.data.sources = [sourceData];
    } else if (builderData.contentType === ContentType.BINARIES) {
        const binaryData: SecHubSourceDataConfiguration = {
            name: referenceName,
            fileSystem: {
                folders: builderData.includeFolders
            },
            excludes: builderData.excludeFolders
        };
        model.data.binaries = [binaryData];
    }    
}

function isStringInArrayIgnoreCase(target: string, array: string[]): boolean {
    const lowerCaseTarget = target.toLowerCase();
    return array.some(item => item.toLowerCase() === lowerCaseTarget);
}