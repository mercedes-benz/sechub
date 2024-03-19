// SPDX-License-Identifier: MIT

export enum ContentType{
    
    SOURCE = 'source',

    BINARIES = 'binaries'
}

export enum ScanType{
    
    CODE_SCAN = 'code-scan',

    LICENSE_SCAN = 'license-scan',

    SECRET_SCAN = 'secret-scan',

}

/**
 * SecHub configuration model
 */
export class SecHubConfigurationModel {
    apiVersion='1.0';

    data = new DataSection();

    codeScan?: CodeScan;
    secretScan?: SecretScan;
    licenseScan?: LicenseScan;
}

export class DataSection {
    sources: SourceData[]|undefined;
    binaries: BinaryData[]|undefined;
}

export class SourceData {
    name = '';
    fileSystem= new FileSystem();
    
    excludes: string[]|undefined;
    additionalFilenameExtensions: string[]|undefined;
}

export class BinaryData {
    name = '';
    fileSystem= new FileSystem();
    
    excludes: string[]|undefined;
    additionalFilenameExtensions: string[]|undefined;
}

export class CodeScan {
    use: string[] = [];
}

export class SecretScan {
    use: string[] = [];
}

export class LicenseScan {
    use: string[] = [];
}

export class FileSystem{
    folders?: string[] = [];
}
