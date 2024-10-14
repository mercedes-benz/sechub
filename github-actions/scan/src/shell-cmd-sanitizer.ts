// SPDX-License-Identifier: MIT

const ALLOWED_SHELL_COMMAND_CHARACTERS = /^[a-zA-Z0-9_\- ]+$/;

/**
 * Sanitizes a shell command to prevent command injection attacks.
 *
 * This function performs the following steps:
 * 1. Removes duplicate whitespaces caused by optional arguments.
 * 2. Checks the command against a predefined pattern for potential command injection characters.
 * 3. Throws a `CommandInjectionError` if any injection characters are detected.
 *
 * @param {string} shellCommand - The shell command to be sanitized.
 * @returns {string} - The sanitized shell command.
 * @throws {CommandInjectionError} - If command injection characters are detected in the shell command.
 */
export function sanitize(shellCommand: string): string {
    /* remove duplicate whitespaces caused by optional arguments */
    shellCommand = shellCommand.replace(/\s+/g, ' ');

    if (!ALLOWED_SHELL_COMMAND_CHARACTERS.test(shellCommand)) {
        throw new CommandInjectionError(`Command injection detected in shell command: ${shellCommand}`);
    }

    return shellCommand;
}

export class CommandInjectionError extends Error {
    readonly code: number;

    constructor(msg: string) {
        super(msg);
        this.name = "CommandInjectionError";
        this.code = 666;
    }
}
