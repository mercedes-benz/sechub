// SPDX-License-Identifier: MIT

import * as fs from 'fs';

/**
 * Sets the value of an output variable for the GitHub Action.
 * This method is a replacement of usage of core.setOutput(..) method.
 * There were problems with core.setOutput(...), see
 *  - https://github.com/mercedes-benz/sechub/issues/3481#issuecomment-2539015176 and
 *  - https://github.com/actions/toolkit/issues/1218
 * 
 */
export function storeOutput(field: string, value: string) {
    const outputFilePath = process.env['GITHUB_OUTPUT'] || '';

    if (!outputFilePath) {
        throw new Error('GITHUB_OUTPUT environment variable is not set');
    }

    const outputLine = `${field}=${value}\n`;

    fs.appendFileSync(outputFilePath, outputLine, { encoding: 'utf8' });
}
