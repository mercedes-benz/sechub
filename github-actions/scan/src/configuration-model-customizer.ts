// SPDX-License-Identifier: MIT

const ADDITIONAL_EXCLUDES: string[] = ['**/.sechub-gha/**']

/**
 * Adds additional exclude patterns to the provided JSON data.
 * 
 * If a 'data' section is present the additional exclude patterns will be added to each entry's 'excludes' array (sources and binaries).
 * In case the 'data' section is not present, the deprecated 'codeScan.excludes' will be checked instead.
 * If no sources or binaries are specified at all, no default exclude patterns will be added.
 * 
 * @param sechubConfigJson - The JSON data to be updated.
 */
export function addAdditonalExcludes(sechubConfigJson: any): void {
    handleDataSection(sechubConfigJson.data);
    handleLegacyCodeScanSection(sechubConfigJson.codeScan);
}

function handleDataSection(dataSection: any) {
    if (dataSection === undefined) {
        return;
    }
    const processEntries = (entries: { excludes?: string[] }[]) => {
        entries.forEach(entry => {
            entry.excludes = entry.excludes || [];
            entry.excludes.push(...ADDITIONAL_EXCLUDES);
        });
    };

    if (dataSection.sources) {
        processEntries(dataSection.sources);
    }

    if (dataSection.binaries) {
        processEntries(dataSection.binaries);
    }
}

function handleLegacyCodeScanSection(codeScan: any){
    if (codeScan === undefined) {
        return;
    }
    if (codeScan.fileSystem === undefined) {
        return;
    }
    codeScan.excludes = codeScan.excludes || [];
    codeScan.excludes.push(...ADDITIONAL_EXCLUDES);
}
