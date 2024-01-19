// SPDX-License-Identifier: MIT

import * as core from '@actions/core';


/* ---------------------------------- */
/* -------- Exit codes -------------- */
/* ---------------------------------- */
// This is a mapping for client exit codes - origin can be found at constants.go
const exitCodeMap = new Map();
exitCodeMap.set(0, 'OK');
exitCodeMap.set(1, 'FAILED');
exitCodeMap.set(3, 'ERROR - Missing parameters');
exitCodeMap.set(4, 'ERROR - Config file does not exist or is not valid');
exitCodeMap.set(5, 'ERROR - HTTP error has occurred');
exitCodeMap.set(6, 'ERROR - Action was illegal');
exitCodeMap.set(7, 'ERROR - Missing configuration parts');
exitCodeMap.set(8, 'ERROR - IO error');
exitCodeMap.set(9, 'ERROR - Config file not in expected format');
exitCodeMap.set(10, 'ERROR - Job has been canceld on SecHub server');


/**
 * Logs the exit code and uses error method if not 0.
 * @param code The given exit code
 */
export function logExitCode(code: number) {
    const message = 'Exit code: ' + code+' . Description: '+exitCodeMap.get(code);
    if (code === 0) {
        core.info(message);
    } else {
        core.error(message);
    }
}
