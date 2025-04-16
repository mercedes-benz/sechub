// SPDX-License-Identifier: MIT

import * as fs from 'fs';

/**
 * This is a wrapper function - necessary make callers testable with jest.
 * "fs" seems to be nolonger mockable any more correctly with jest, so this 
 * wrapper class was introduced. We can mock the wrapper without any problems.
 * 
 * @param filePath 
 * @returns file content as string
 */
export function readFileSync(filePath: string, options:
    | {
        encoding: BufferEncoding;
        flag?: string | undefined;
    }
    | BufferEncoding): string {
    return fs.readFileSync(filePath, options);
}