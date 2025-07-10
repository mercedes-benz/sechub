// SPDX-License-Identifier: MIT

import { SecHubConfigurationModelBuilderData } from './configuration-builder';

export class ContentType {

    static SOURCE = 'source';

    static BINARIES = 'binaries';

    public static isSource(data: string | undefined): boolean {
        if (!data) {
            return false;
        }
        return data.toLowerCase() == this.SOURCE;
    }

    public static isBinary(data: string | undefined): boolean {
        if (!data) {
            return false;
        }
        return data.toLowerCase() == this.BINARIES;
    }

    static ensureAccepted(contentType: string): string {
        if (ContentType.isSource(contentType)) {
            return ContentType.SOURCE;
        }
        if (ContentType.isBinary(contentType)) {
            return ContentType.BINARIES;
        }
        return SecHubConfigurationModelBuilderData.DEFAULT_CONTENT_TYPE;
    }

}

export class ScanType {

    static CODE_SCAN = 'codescan';
    static LICENSE_SCAN = 'licensescan';
    static SECRET_SCAN = 'secretscan';
    static IAC_SCAN = 'iacscan';

    public static isCodeScan(data: string | undefined): boolean {
        if (!data) {
            return false;
        }
        return data.toLowerCase() == this.CODE_SCAN;
    }

    public static isLicenseScan(data: string | undefined): boolean {
        if (!data) {
            return false;
        }
        return data.toLowerCase() == this.LICENSE_SCAN;
    }
    public static isSecretScan(data: string | undefined): boolean {
        if (!data) {
            return false;
        }
        return data.toLowerCase() == this.SECRET_SCAN;
    }
    public static isIacScan(data: string | undefined): boolean {
        if (!data) {
            return false;
        }
        return data.toLowerCase() == this.IAC_SCAN;
    }

    public static ensureAccepted(data: string[]): string[] {
        const accepted: string[] = [];
        if (data){
            for (const entry of data) {
    
                if (ScanType.isCodeScan(entry)) {
                    accepted.push(ScanType.CODE_SCAN);
                } else if (ScanType.isLicenseScan(entry)) {
                    accepted.push(ScanType.LICENSE_SCAN);
                } else if (ScanType.isSecretScan(entry)) {
                    accepted.push(ScanType.SECRET_SCAN);
                } else if (ScanType.isIacScan(entry)){
                    accepted.push(ScanType.IAC_SCAN);
                }
            }
        }
        if (accepted.length == 0) {
            accepted.push(SecHubConfigurationModelBuilderData.DEFAULT_SCAN_TYPE);
        }
        return accepted;
    }
}

/**
 * SecHub configuration model
 */
export class SecHubConfigurationModel {
    apiVersion = '1.0';

    data = new DataSection();

    project = '';

    codeScan?: CodeScan;
    secretScan?: SecretScan;
    licenseScan?: LicenseScan;
    iacScan?: IacScan;
}

export class DataSection {
    sources: SourceData[] | undefined;
    binaries: BinaryData[] | undefined;
}

export class SourceData {
    name = '';
    fileSystem = new FileSystem();

    excludes: string[] | undefined;
    additionalFilenameExtensions: string[] | undefined;
}

export class BinaryData {
    name = '';
    fileSystem = new FileSystem();

    excludes: string[] | undefined;
    additionalFilenameExtensions: string[] | undefined;
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

export class IacScan {
    use: string[] = [];
}

export class FileSystem {
    folders?: string[] = [];
}
