const COMMAND_INJECTION_PATTERN = /[;&|`$\\<>"'*()\[\]{}\n\t]/;

export function sanitize(shellCommand: string): string {
    // remove duplicate whitespaces caused by optional arguments
    shellCommand = shellCommand.replace(/\s+/g, ' ');

    if (COMMAND_INJECTION_PATTERN.test(shellCommand)) {
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
