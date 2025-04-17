// SPDX-License-Identifier: MIT

import * as fs from 'fs';

/**
 * This is a wrapper function - makes fs parts testable with jest without problems.
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

/**  
 * This is a wrapper function - makes fs parts testable with jest without problems.
 * 
 * @return an integer representing the file descriptor. */
export function openSync(path: fs.PathLike, flags: fs.OpenMode, mode?: fs.Mode | null): number {
    return fs.openSync(path, flags, mode);
}

/**
 * This is a wrapper function - makes fs parts testable with jest without problems. 
 * @param fd file descriptor to close
 */
export function closeSync(fd: number): void {
    fs.closeSync(fd);
}

/**
 * This is a wrapper function - makes fs parts testable with jest without problems. 
 * For detailed information, see the documentation of the asynchronous version of
 * this API: {@link fs.mkdtemp}.
 *
 * The optional `options` argument can be a string specifying an encoding, or an
 *  object with an `encoding` property specifying the character encoding to use.
 * @return the created directory path.
 */
export function mkdtempSync(prefix: string, options?: fs.EncodingOption): string {
    return fs.mkdtempSync(prefix, options);
}