// SPDX-License-Identifier: MIT

/**
 * SecHub configuration model
 */
export type SecHubConfigurationModel = {
    apiVersion: string;
    codeScan?: CodeScan;
};

type CodeScan = {
    fileSystem?: FileSystem;
    excludes?: string[];
};

type FileSystem = {
    folders?: string[];
};
