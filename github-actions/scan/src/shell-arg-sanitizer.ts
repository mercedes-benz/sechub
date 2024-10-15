// SPDX-License-Identifier: MIT

import * as core from '@actions/core';
import {execFileSync} from "child_process";

const SHELL_ARGUMENT_CHARACTER_WHITELIST = /^[a-zA-Z0-9._\-\/ ]+$/;

/**
 * Sanitizes a shell arg to prevent command injection attacks.
 *
 * This function performs the following steps:
 * 1. Removes duplicate whitespaces.
 * 2. Checks the arg against a predefined whitelist of allowed characters.
 * 3. Throws a `CommandInjectionError` if any injection characters are detected.
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
        argument should not have any whitespaces so this should never cause any issues
    */
    arg = arg.replace(/\s+/g, '')

    if (!SHELL_ARGUMENT_CHARACTER_WHITELIST.test(arg)) {
        core.error(`Argument has invalid characters: ${arg}`);
        throw new CommandInjectionError(`Command injection detected in shell argument: ${arg}`);
    }

    if (isShellCommand(arg)) {
        core.error(`Argument is an executable shell command: ${arg}`);
        throw new CommandInjectionError(`Command injection detected in shell argument: ${arg}`);
    }

    return arg;
}

/**
 * Function to check if a given string is a valid shell command using 'execFileSync'
 * @param command - The command to check
 * @returns A boolean indicating if the command is valid
 */
function isShellCommand(command: string): boolean {
    try {
        /* We use `command -v` to check if the command exists */
        execFileSync('command', ['-v', command], { stdio: 'ignore' });
        /* If no error is thrown, the command is valid */
        return true;
    } catch (error) {
        /* If an error is thrown, the command is invalid */
        return false;
    }
}

export class CommandInjectionError extends Error {
    readonly code: number;

    constructor(msg: string) {
        super(msg);
        this.name = "CommandInjectionError";
        this.code = 666;
    }
}
