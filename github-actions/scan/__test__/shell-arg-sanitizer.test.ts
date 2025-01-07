/* eslint-disable indent */
// SPDX-License-Identifier: MIT

import * as shellArgSanitizer from '../src/shell-arg-sanitizer';
import * as core from '@actions/core';

jest.mock('@actions/core');

const mockError = core.error as jest.MockedFunction<typeof core.error>;

const debugEnabled = false;

beforeEach(() => {
    mockError.mockImplementation((message: string | Error) => {
        if (debugEnabled) {
            console.log(`Error: ${message}`);
        }
    });
    mockError.mockClear();
});


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
        ['$(rm -rf /; echo hacked)'],
        ['kill'],
        ['sleep'],
        ['shutdown'],
        ['reboot'],
        ['halt'],
        ['ps'],
        ['top'],
        ['killall'],
        ['pkill'],
        ['pgrep'],
        ['chown'],
        ['chmod'],
        ['chgrp'],
        ['passwd'],
        ['su'],
        ['sudo'],
        ['chsh'],
        ['chfn'],
        ['chroot']
    ])(
        '%s throws CommandInjectionError',
        (arg) => {
            /* test */
            expect(() => shellArgSanitizer.sanitize(arg)).toThrow(/Command injection detected in shell argument:/);
        }
    );

    test.each([
        ['/path/to/sechub-cli'],
        ['-configfile'],
        ['/path/to/config.json'],
        ['-output'],
        ['/path/to/workspace'],
        ['-addScmHistory'],
        ['scan'],
        ['-jobUUID'],
        ['-project'],
        ['--reportformat'],
        ['json'],
        ['getReport']
    ])(
        'does not throw CommandInjectionError for safe shell argument: %s',
        (arg) => {
            /* test */
            expect(() => shellArgSanitizer.sanitize(arg)).not.toThrow();
        });

    it('removes whitespaces', function () {
        /* prepare */
        const arg = '  /path/to/sechub-cli   ';

        /* execute */
        const sanitized = shellArgSanitizer.sanitize(arg);

        /* test */
        expect(sanitized).toEqual('/path/to/sechub-cli');
    });
});