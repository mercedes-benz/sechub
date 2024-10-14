// SPDX-License-Identifier: MIT

import * as shellSanitizer from '../src/shell-cmd-sanitizer';

describe('sanitizeShellCommand', () => {
    test.each([
        ['rm -rf /; echo hacked'], // Command chaining
        ['echo $(whoami)'], // Command substitution
        ['cat /etc/passwd | grep root'], // Piping
        ['touch /tmp/test && ls /tmp'], // Logical AND
        ['echo hello > /tmp/test'], // Redirection
        ['`reboot`'], // Backticks
        ['$(reboot)'], // Subshell
        ['; reboot'], // Semicolon
        ['| reboot'], // Pipe
        ['& reboot'], // Background process
        ['> /dev/null'], // Redirection to null
        ['< /dev/null'], // Input redirection
        ['|| reboot'], // Logical OR
        ['&& reboot'], // Logical AND
        ['$(< /etc/passwd)'], // Command substitution with input redirection
        ['$(cat /etc/passwd)'], // Command substitution with cat
        ['$(echo hello > /tmp/test)'], // Command substitution with redirection
        ['$(touch /tmp/test && ls /tmp)'], // Command substitution with logical AND
        ['$(cat /etc/passwd | grep root)'], // Command substitution with pipe
        ['$(rm -rf /; echo hacked)']
    ])(
        '%s throws CommandInjectionError',
        (shellCommand) => {
            /* test */
            expect(() => shellSanitizer.sanitize(shellCommand)).toThrow('Command injection detected in shell command: ' + shellCommand);
        }
    );

    it('removes duplicate whitespaces', function () {
        /* prepare */
        const shellCommand = 'this is   a test';

        /* execute */
        const sanitized = shellSanitizer.sanitize(shellCommand);

        /* test */
        expect(sanitized).toEqual('this is a test');
    });
});