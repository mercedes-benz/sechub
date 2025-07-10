// SPDX-License-Identifier: MIT

import * as configBuilder from '../src/configuration-builder';
import { SecHubConfigurationModel, ContentType, ScanType } from '../src/configuration-model';
import { SecHubConfigurationModelBuilderData } from '../src/configuration-builder';

jest.mock('@actions/core');

const debugEnabled = false;

function logDebug(model: SecHubConfigurationModel){
    if (! debugEnabled){
        return;
    }
    const json = JSON.stringify(model, null, 2); // pretty printed output

    console.log('json='+json);
}

describe('configuration-builder', function() {
    test('null parameters - a model is created with api version 1.0.0', function () {
        /* execute */
        const builderData = new SecHubConfigurationModelBuilderData();
        const model= configBuilder.createSecHubConfigurationModel(builderData);

        /* test */
        expect(model.apiVersion).toEqual('1.0');
        expect(model.data.sources).toBeDefined();
        expect(model.data.binaries).toBeUndefined();
    });

    test('codescan generated per default - source,one folder defined', () => {

        /* prepare */
        const builderData = new SecHubConfigurationModelBuilderData();
        builderData.includeFolders= ['folder1'];

        /* execute */
        const model= configBuilder.createSecHubConfigurationModel(builderData);

        /* test */
        logDebug(model);

        expect(model.apiVersion).toEqual('1.0');
        
        expect(model.data.sources).toBeDefined();
        expect(model.data.binaries).toBeUndefined();

        expect(model.data.sources?.length).toEqual(1);

        const firstSource = model.data.sources?.[0];
        expect(firstSource?.fileSystem.folders?.length).toEqual(1);
        expect(firstSource?.fileSystem.folders?.[0]).toEqual('folder1');

        expect(model.codeScan).toBeDefined();
        expect(model.codeScan?.use.length).toEqual(1);
        expect(model.codeScan?.use[0]).toEqual('reference-data-1');

        expect(model.licenseScan).toBeUndefined();
        expect(model.secretScan).toBeUndefined();
        expect(model.iacScan).toBeUndefined();

    });

    test('codescan generated per default - source, two folders defined', () => {

        /* prepare */
        const builderData = new SecHubConfigurationModelBuilderData();
        builderData.includeFolders= ['folder1','folder2'];

        /* execute */
        const model= configBuilder.createSecHubConfigurationModel(builderData);

        /* test */
        logDebug(model);

        expect(model.apiVersion).toEqual('1.0');
        
        expect(model.data.sources).toBeDefined();
        expect(model.data.binaries).toBeUndefined();
      
        expect(model.data.sources?.length).toEqual(1);

        const firstSource = model.data.sources?.[0];
        expect(firstSource?.fileSystem.folders?.length).toEqual(2);
        expect(firstSource?.fileSystem.folders?.[0]).toEqual('folder1');
        expect(firstSource?.fileSystem.folders?.[1]).toEqual('folder2');
        expect(firstSource?.excludes?.length).toEqual(0);

        expect(model.codeScan).toBeDefined();
        expect(model.codeScan?.use.length).toEqual(1);
        expect(model.codeScan?.use[0]).toEqual('reference-data-1');

        expect(model.licenseScan).toBeUndefined();
        expect(model.secretScan).toBeUndefined();
        expect(model.iacScan).toBeUndefined();

    });

    test('codescan generated per default - source, two folders defined, one excluded', () => {

        /* prepare */
        const builderData = new SecHubConfigurationModelBuilderData();
        builderData.includeFolders= ['folder1','folder2'];
        builderData.excludeFolders= ['folder3'];

        /* execute */
        const model= configBuilder.createSecHubConfigurationModel(builderData);

        /* test */
        logDebug(model);

        expect(model.apiVersion).toEqual('1.0');
        
        expect(model.data.sources).toBeDefined();
        expect(model.data.binaries).toBeUndefined();
      
        expect(model.data.sources?.length).toEqual(1);

        const firstSource = model.data.sources?.[0];
        expect(firstSource?.fileSystem.folders?.length).toEqual(2);
        expect(firstSource?.fileSystem.folders?.[0]).toEqual('folder1');
        expect(firstSource?.fileSystem.folders?.[1]).toEqual('folder2');

        expect(firstSource?.excludes?.length).toEqual(1);
        expect(firstSource?.excludes?.[0]).toEqual('folder3');

        expect(model.codeScan).toBeDefined();
        expect(model.codeScan?.use.length).toEqual(1);
        expect(model.codeScan?.use[0]).toEqual('reference-data-1');

        expect(model.secretScan).toBeUndefined();
        expect(model.licenseScan).toBeUndefined();
        expect(model.iacScan).toBeUndefined();

    });

    test('codescan generated per default - binaries, two folders defined', () => {

        /* prepare */
        const builderData = new SecHubConfigurationModelBuilderData();
        builderData.includeFolders= ['folder1','folder2'];
        builderData.contentType=ContentType.BINARIES;

        /* execute */
        const model= configBuilder.createSecHubConfigurationModel(builderData);

        /* test */
        logDebug(model);

        expect(model.apiVersion).toEqual('1.0');
        
        expect(model.data.sources).toBeUndefined();
        expect(model.data.binaries).toBeDefined();
      
        expect(model.data.binaries?.length).toEqual(1);

        const firstBinary = model.data.binaries?.[0];
        expect(firstBinary?.fileSystem.folders?.length).toEqual(2);
        expect(firstBinary?.fileSystem.folders?.[0]).toEqual('folder1');
        expect(firstBinary?.fileSystem.folders?.[1]).toEqual('folder2');
        expect(firstBinary?.excludes?.length).toEqual(0);

        expect(model.codeScan).toBeDefined();
        expect(model.codeScan?.use.length).toEqual(1);
        expect(model.codeScan?.use[0]).toEqual('reference-data-1');

        expect(model.secretScan).toBeUndefined();
        expect(model.licenseScan).toBeUndefined();
        expect(model.iacScan).toBeUndefined();
    });

    test('codescan and license scan - two folders defined', () => {

        /* prepare */
        const builderData = new SecHubConfigurationModelBuilderData();
        builderData.includeFolders= ['folder1','folder2'];
        builderData.scanTypes=[ScanType.CODE_SCAN,ScanType.LICENSE_SCAN];

        /* execute */
        const model= configBuilder.createSecHubConfigurationModel(builderData);

        /* test */
        logDebug(model);

        expect(model.apiVersion).toEqual('1.0');
        
        expect(model.data.sources);
        expect(model.data.sources?.length).toEqual(1);

        const firstSource = model.data.sources?.[0];
        expect(firstSource?.fileSystem.folders?.length).toEqual(2);
        expect(firstSource?.fileSystem.folders?.[0]).toEqual('folder1');
        expect(firstSource?.fileSystem.folders?.[1]).toEqual('folder2');

        expect(model.codeScan).toBeDefined();
        expect(model.codeScan?.use.length).toEqual(1);
        expect(model.codeScan?.use[0]).toEqual('reference-data-1');

        expect(model.licenseScan).toBeDefined();
        expect(model.licenseScan?.use.length).toEqual(1);
        expect(model.licenseScan?.use[0]).toEqual('reference-data-1'); // same data refererenced
        
        expect(model.secretScan).toBeUndefined();
        expect(model.iacScan).toBeUndefined();

    });
    test('codescan and secret scan - two folders defined', () => {

        /* prepare */
        const builderData = new SecHubConfigurationModelBuilderData();
        builderData.includeFolders= ['folder1','folder2'];
        builderData.scanTypes=[ScanType.CODE_SCAN,ScanType.SECRET_SCAN];

        /* execute */
        const model= configBuilder.createSecHubConfigurationModel(builderData);

        /* test */
        logDebug(model);

        expect(model.apiVersion).toEqual('1.0');
        
        expect(model.data.sources);
        expect(model.data.sources?.length).toEqual(1);

        const firstSource = model.data.sources?.[0];
        expect(firstSource?.fileSystem.folders?.length).toEqual(2);
        expect(firstSource?.fileSystem.folders?.[0]).toEqual('folder1');
        expect(firstSource?.fileSystem.folders?.[1]).toEqual('folder2');

        expect(model.codeScan).toBeDefined();
        expect(model.codeScan?.use.length).toEqual(1);
        expect(model.codeScan?.use[0]).toEqual('reference-data-1');

        expect(model.secretScan).toBeDefined();
        expect(model.secretScan?.use.length).toEqual(1);
        expect(model.secretScan?.use[0]).toEqual('reference-data-1'); // same data refererenced
        
        expect(model.licenseScan).toBeUndefined();
        expect(model.iacScan).toBeUndefined();
        
    });
    test('secret scan standalone - source, one folder defined, one excluded', () => {

        /* prepare */
        const builderData = new SecHubConfigurationModelBuilderData();
        builderData.includeFolders= ['folder1'];
        builderData.excludeFolders= ['folderX'];
        builderData.scanTypes=[ScanType.SECRET_SCAN];

        /* execute */
        const model= configBuilder.createSecHubConfigurationModel(builderData);

        /* test */
        logDebug(model);

        expect(model.apiVersion).toEqual('1.0');
        
        expect(model.data.sources);
        expect(model.data.sources?.length).toEqual(1);

        const firstSource = model.data.sources?.[0];
        expect(firstSource?.fileSystem.folders?.length).toEqual(1);
        expect(firstSource?.fileSystem.folders?.[0]).toEqual('folder1');
        expect(firstSource?.excludes?.length).toEqual(1);
        expect(firstSource?.excludes?.[0]).toEqual('folderX');

        expect(model.codeScan).toBeUndefined();
        
        expect(model.secretScan).toBeDefined();
        expect(model.secretScan?.use.length).toEqual(1);
        expect(model.secretScan?.use[0]).toEqual('reference-data-1'); // same data refererenced
        
        expect(model.licenseScan).toBeUndefined();
        expect(model.codeScan).toBeUndefined();
        expect(model.iacScan).toBeUndefined();
        
    });

    test('iacScan standalone - source, one folder defined, one excluded', () => {

        /* prepare */
        const builderData = new SecHubConfigurationModelBuilderData();
        builderData.includeFolders= ['folder1'];
        builderData.excludeFolders= ['folderX'];
        builderData.scanTypes=[ScanType.IAC_SCAN];

        /* execute */
        const model= configBuilder.createSecHubConfigurationModel(builderData);

        /* test */
        logDebug(model);

        expect(model.apiVersion).toEqual('1.0');
        
        expect(model.data.sources);
        expect(model.data.sources?.length).toEqual(1);

        const firstSource = model.data.sources?.[0];
        expect(firstSource?.fileSystem.folders?.length).toEqual(1);
        expect(firstSource?.fileSystem.folders?.[0]).toEqual('folder1');
        expect(firstSource?.excludes?.length).toEqual(1);
        expect(firstSource?.excludes?.[0]).toEqual('folderX');

        expect(model.codeScan).toBeUndefined();
        
        expect(model.iacScan).toBeDefined();
        expect(model.iacScan?.use.length).toEqual(1);
        expect(model.iacScan?.use[0]).toEqual('reference-data-1'); // same data refererenced
        
        expect(model.licenseScan).toBeUndefined();
        expect(model.secretScan).toBeUndefined();
        expect(model.codeScan).toBeUndefined();
    });

    test('codeScan, iacScan and secretScan - two folders defined', () => {

        /* prepare */
        const builderData = new SecHubConfigurationModelBuilderData();
        builderData.includeFolders= ['folder1','folder2'];
        builderData.scanTypes=[ScanType.CODE_SCAN,ScanType.IAC_SCAN,ScanType.SECRET_SCAN];

        /* execute */
        const model= configBuilder.createSecHubConfigurationModel(builderData);

        /* test */
        logDebug(model);

        expect(model.apiVersion).toEqual('1.0');
        
        expect(model.data.sources);
        expect(model.data.sources?.length).toEqual(1);

        const firstSource = model.data.sources?.[0];
        expect(firstSource?.fileSystem.folders?.length).toEqual(2);
        expect(firstSource?.fileSystem.folders?.[0]).toEqual('folder1');
        expect(firstSource?.fileSystem.folders?.[1]).toEqual('folder2');

        expect(model.codeScan).toBeDefined();
        expect(model.codeScan?.use.length).toEqual(1);
        expect(model.codeScan?.use[0]).toEqual('reference-data-1');

        expect(model.iacScan).toBeDefined();
        expect(model.iacScan?.use.length).toEqual(1);
        expect(model.iacScan?.use[0]).toEqual('reference-data-1'); // same data refererenced

        expect(model.secretScan).toBeDefined();
        expect(model.secretScan?.use.length).toEqual(1);
        expect(model.secretScan?.use[0]).toEqual('reference-data-1'); // same data refererenced
        
        expect(model.licenseScan).toBeUndefined();        
    });

});