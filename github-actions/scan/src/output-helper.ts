// SPDX-License-Identifier: MIT
import { setOutput } from '@actions/core/lib/core';
import { exportVariable } from '@actions/core/lib/core';

/**
 * Sets the value of an output (environment ) variable for the GitHub Action.
 * This method is a workaround because of problems with of core.setOutput(..) method.
 * There were problems with core.setOutput(...), see
 *  - https://github.com/mercedes-benz/sechub/issues/3481#issuecomment-2539015176 and
 *  - https://github.com/actions/toolkit/issues/1218
 *  - https://github.com/actions/toolkit/issues/1906
 * 
 * As a workaround we provide instead of output 
 * special SecHub ouput environment variables with naming convention "SECHUB_OUTPUT_${fieldAdopted}"
 * 
 * `fieldAdopted` is same as `field`, but uppercased and `-` will be replaced by `_`
 * 
 * For example: `scan-readable-summary` will become `SECHUB_OUTPUT_SCAN_READABLE_SUMMARY`
 * 
 * If debugging is enabled in action the setting will be logged.
 */
export function storeOutput(field: string, value: string) {
    // export the output to an "output" variable (this works)
    const envVarName = `SECHUB_OUTPUT_${field.toUpperCase().replace(/-/g, '_')}`;
    exportVariable(envVarName, value);
    if (process.env.ACTIONS_RUNNER_DEBUG === 'true') {
        // Print the environment variable for debugging
        console.log(`Exported environment variable ${envVarName} with value: ${value}`);
    }

    // 1. This following out commented code was thought as a workaround 
    // for https://github.com/actions/toolkit/issues/1218
    // Because the GITHUB_OUTPUT file from a worfklow step (which worked) did not contain 
    // crypto.randomUUID() parts we tried to write the key/value file "normally" without 
    // the crypto parts, but It did not appear inside context output, means it didn't work
    // (even when it the exact file structure as done by an echo ?!?!)
    // But we keep it here for documentation:
    
    // const outputFilePath = process.env['GITHUB_OUTPUT'] || '';
    // if (!outputFilePath) {
    //     throw new Error('GITHUB_OUTPUT environment variable is not set');
    // }

    // const outputLine = `${field}=${value}\n`;
    // fs.appendFileSync(outputFilePath, outputLine, { encoding: 'utf8' });


    // 2. Offical way by core API (does not work)
    // setOutput(field,value);

}
