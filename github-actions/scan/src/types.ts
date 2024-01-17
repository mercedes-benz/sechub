// SPDX-License-Identifier: MITs

export type SecHubJson = {
    apiVersion: string;
    codeScan: CodeScan;
};

type CodeScan = {
    fileSystem: FileSystem;
    excludes: string[];
};

type FileSystem = {
    folders: string[];
};

export type Settings = {
    artifactName: string;
    filePattern: string;
    secHubJsonFileName: string;
};
