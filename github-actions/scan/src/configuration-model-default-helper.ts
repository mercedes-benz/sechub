// SPDX-License-Identifier: MIT

export const DEFAULT_EXCLUDES: string[] = ['**/.sechub-gha/**']

/**
 * Adds default exclude patterns to the provided JSON data, without altering the original JSON data.
 * 
 * If a 'data' section is present the default exclude patterns will be added to each entry's 'excludes' array (sources and binaries).
 * In case the 'data' section is not present, the deprecated 'codeScan.excludes' will be checked instead.
 * If no sources or binaries are specified at all, no default exclude patterns will be added.
 * 
 * @param sechubConfigJson - The JSON data to be updated.
 * @returns A copy of the JSON data with the default exclude patterns added.
 */
export function addDefaultExcludesToSecHubConfig(sechubConfigJson: any): any {
    let sechubConfigJsonCopy = { ...sechubConfigJson };

    let dataSection = sechubConfigJsonCopy.data;
    if (dataSection === undefined && sechubConfigJsonCopy.codeScan !== undefined) {

        // check the deprecated old way to specify codeScan as well
        let codeScan = sechubConfigJsonCopy.codeScan;
        if (codeScan.excludes === undefined) {
            codeScan.excludes = [];
        }
        codeScan.excludes.push(...DEFAULT_EXCLUDES);
    } else if (dataSection !== undefined) {
        if (dataSection.sources !== undefined) {

            dataSection.sources.forEach((sourceEntry: { excludes?: string[]; }) => {
                if (sourceEntry.excludes === undefined) {
                    sourceEntry.excludes = [];
                }
                sourceEntry.excludes.push(...DEFAULT_EXCLUDES);
            });
        }
        if (dataSection.binaries !== undefined) {

            dataSection.binaries.forEach((binaryEntry: { excludes?: string[]; }) => {
                if (binaryEntry.excludes === undefined) {
                    binaryEntry.excludes = [];
                }
                binaryEntry.excludes.push(...DEFAULT_EXCLUDES);
            });
        }
    }
    return sechubConfigJsonCopy;
}
