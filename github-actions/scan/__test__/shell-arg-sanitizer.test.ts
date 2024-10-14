// SPDX-License-Identifier: MIT

import * as shellSanitizer from '../src/shell-arg-sanitizer';

describe('sanitize', () => {
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
        (arg) => {
            /* test */
            expect(() => shellSanitizer.sanitize(arg)).toThrow('Command injection detected in shell argument: ' + arg);
        }
    );

    test.each([
        ['/path/to/sechub-cli -configfile /path/to/config.json -output /path/to/workspace scan'],
        ['/path/to/sechub-cli -configfile /path/to/config.json -output /path/to/workspace -addScmHistory scan'],
        ['/path/to/sechub-cli -jobUUID job-uuid -project project-name --reportformat json getReport']
    ])(
        'does not throw CommandInjectionError for safe shell argument: %s',
        (arg) => {
            /* test */
            expect(() => shellSanitizer.sanitize(arg)).not.toThrow();
    });

    it('removes duplicate whitespaces', function () {
        /* prepare */
        const arg = '  /path/to/sechub-cli   ';

        /* execute */
        const sanitized = shellSanitizer.sanitize(arg);

        /* test */
        expect(sanitized).toEqual('/path/to/sechub-cli');
    });
});