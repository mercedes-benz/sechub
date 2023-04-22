// SPDX-License-Identifier: MIT

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
