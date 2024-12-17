// SPDX-License-Identifier: MIT

import { getClientVersion } from '../src/client-version-helper';
import axios from 'axios';
import MockAdapter from 'axios-mock-adapter';
import * as core from '@actions/core';

jest.mock('@actions/core');

const mockDebug = core.debug as jest.MockedFunction<typeof core.debug>;

const debugEnabled = false;

beforeEach(() => {
    mockDebug.mockImplementation((message: string | Error) => {
        if (debugEnabled) {
            console.log(`Debug: ${message}`);
        }
    });
    mockDebug.mockClear();
});

describe('getClientVersion', function () {

    it.each([
        undefined,
        null,
        ''
    ])('client version undefined - throws: No SecHub client version defined!', async (param) => {
        /* prepare */
        const clientVersion: any = param;

        /* execute */
        const result = getClientVersion(clientVersion);

        /* test */
        await expect(result).rejects.toThrow('No SecHub client version defined!');
    });

    it.each([
        'this-is-not-a-valid-version',
        '1',
        '1.0',
        '1.0.a',
        '1.0.0a'
    ])('client version invalid - throws: Invalid SecHub client version', async  (param) => {
        /* prepare */
        const clientVersion = param;

        /* execute  */
        const result = getClientVersion(clientVersion);

        /* test */
        await expect(result).rejects.toThrow(`Invalid SecHub client version: ${clientVersion}`);
    });

    it('client version 1.0.0 - returns 1.0.0', async () => {
        /* prepare */
        const clientVersion = '1.0.0';
        const axiosMock = new MockAdapter(axios);

        /* execute  */
        const result = await getClientVersion(clientVersion);

        /* test */
        expect(result).toEqual('1.0.0');
        expect(axiosMock.history.get.length).toBe(0);
    });

    it('client version latest - with latest being 1.5.0 - returns 1.5.0', async () => {
        /* prepare */
        const clientVersion = 'latest';
        const axiosMock = new MockAdapter(axios);
        const axiosMockResponse = `<!DOCTYPE html>
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
                <meta http-equiv="refresh" content="0; URL=https://github.com/mercedes-benz/sechub/releases/download/v1.5.0-client/sechub-cli-1.5.0.zip">
                <title>Main Page</title>
            </head>
            <body></body>
            </html>`;
        axiosMock.onGet('https://mercedes-benz.github.io/sechub/latest/client-download.html').reply(200, axiosMockResponse);

        /* execute  */
        const result = await getClientVersion(clientVersion);

        /* test */
        expect(result).toEqual('1.5.0');
        expect(axiosMock.history.get.length).toBe(1);
    });
});
