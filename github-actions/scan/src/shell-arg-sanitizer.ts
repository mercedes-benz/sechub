// SPDX-License-Identifier: MIT

const ALLOWED_SHELL_COMMAND_CHARACTERS = /^[a-zA-Z0-9._\-\/ ]+$/;

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

    /* remove unnecessary whitespaces */
    arg = arg.replace(/\s+/g, ' ');
    arg = arg.trim();

    if (!ALLOWED_SHELL_COMMAND_CHARACTERS.test(arg)) {
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
