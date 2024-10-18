// SPDX-License-Identifier: MIT

import * as core from '@actions/core';
const commandExistsSync = require('command-exists').sync

const SHELL_ARGUMENT_CHARACTER_WHITELIST = /^[a-zA-Z0-9._\-\/ ]+$/;
const FULL_WORD = /^[a-zA-Z]+$/;

/**
 * Sanitizes a shell arg to prevent command injection attacks.
 *
 * This function performs the following steps:
 * 1. Removes duplicate whitespaces.
 * 2. Checks if the arg contains any characters that are not in the character whitelist.
 * 3. Checks if the arg is an executable shell command.
 * 3. Throws a `CommandInjectionError` if any of the above checks are true.
 *
 * @param {string} arg - The shell argument to be sanitized.
 * @returns {string} - The sanitized shell command.
 * @throws {CommandInjectionError} - If command injection characters are detected in the shell command.
 */
export function sanitize(arg: string): string {
    if (!arg) {
        return arg;
    }

    /*
        remove all whitespaces
        a single argument should not have any whitespaces so this should never cause any issues
    */
    arg = arg.replace(/\s+/g, '')

    if (!SHELL_ARGUMENT_CHARACTER_WHITELIST.test(arg)) {
        core.error(`Argument has invalid characters: ${arg}`);
        throw new CommandInjectionError(`Command injection detected in shell argument: ${arg}`);
    }

    if (FULL_WORD.test(arg) && commandExistsSync(arg)) {
        core.error(`Argument is a command: ${arg}`);
        throw new CommandInjectionError(`Command injection detected in shell argument: ${arg}`);
    }

    return arg;
}

export class CommandInjectionError extends Error {
    readonly code: number;

    constructor(msg: string) {
        super(msg);
        this.name = "CommandInjectionError";
        this.code = 666;
    }
}
