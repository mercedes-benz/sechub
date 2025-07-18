// SPDX-License-Identifier: MIT

import { ContentType } from '../src/content-type';
import { resolveProxyConfig, split, ensureAcceptedScanType } from '../src/input-helper';
import { ScanType } from 'sechub-openapi-ts-client';

describe('split', function () {
    it('input undefined - returns empty array', function () {
        /* prepare */
        const input: any = undefined;

        /* execute */
        const result = split(input);

        /* test */
        expect(result).toEqual([]);
    });

    it('input null - returns empty array', function () {
        /* prepare */
        const input: any = null;

        /* execute */
        const result = split(input);

        /* test */
        expect(result).toEqual([]);
    });

    it('input empty - returns empty array', function () {
        /* prepare */
        const input: any = '';

        /* execute */
        const result = split(input);

        /* test */
        expect(result).toEqual([]);
    });

    it('input single whitespace - returns empty array', function () {
        /* prepare */
        const input: any = ' ';

        /* execute */
        const result = split(input);

        /* test */
        expect(result).toEqual([]);
    });

    it('input single comma - returns empty array', function () {
        /* prepare */
        const input: any = ',';

        /* execute */
        const result = split(input);

        /* test */
        expect(result).toEqual([]);
    });

    it('input single value - returns array with single value', function () {
        /* prepare */
        const input: any = 'a';

        /* execute */
        const result = split(input);

        /* test */
        expect(result).toEqual(['a']);
    });

    it('input multiple comma separated values - returns array with multiple values', function () {
        /* prepare */
        const input: any = 'a, b, c, d, e, f, g';

        /* execute */
        const result = split(input);

        /* test */
        expect(result).toEqual(['a', 'b', 'c', 'd', 'e', 'f', 'g']);
    });
});

describe('resolveProxyConfig', function () {
    it('proxy undefined - returns undefined for axios proxy config', function () {
        /* prepare */
        delete process.env.http_proxy;
        delete process.env.https_proxy;

        /* execute */
        const result = resolveProxyConfig();

        /* test */
        expect(result).toBeUndefined();
    });

    it('only http_proxy defined - returns valid axios', function () {
        /* prepare */
        process.env.http_proxy = 'http://user:password@proxy.example.org:1234';
        delete process.env.https_proxy;

        /* execute */
        const result = resolveProxyConfig();

        /* test */
        expect(result?.protocol).toEqual('http');
        expect(result?.host).toEqual('proxy.example.org');
        expect(result?.port).toEqual(1234);
        expect(result?.auth?.username).toEqual('user');
        expect(result?.auth?.password).toEqual('password');
    });

    it('only https_proxy defined - returns valid axios', function () {
        /* prepare */
        delete process.env.http_proxy;
        process.env.https_proxy = 'https://user:password@proxy.example.org:1234';

        /* execute */
        const result = resolveProxyConfig();

        /* test */
        expect(result?.protocol).toEqual('https');
        expect(result?.host).toEqual('proxy.example.org');
        expect(result?.port).toEqual(1234);
        expect(result?.auth?.username).toEqual('user');
        expect(result?.auth?.password).toEqual('password');
    });

    it('http_proxy and https_proxy defined - uses values of https_proxy', function () {
        /* prepare */
        process.env.http_proxy = 'http://user:password@proxy.example.org:1234';
        process.env.https_proxy = 'https://other:pass@proxy.other.org:5678';

        /* execute */
        const result = resolveProxyConfig();

        /* test */
        expect(result?.protocol).toEqual('https');
        expect(result?.host).toEqual('proxy.other.org');
        expect(result?.port).toEqual(5678);
        expect(result?.auth?.username).toEqual('other');
        expect(result?.auth?.password).toEqual('pass');
    });

    it('http_proxy and https_proxy defined - uses values of https_proxy no authentication section', function () {
        /* prepare */
        process.env.http_proxy = 'http://proxy.example.org:1234';
        process.env.https_proxy = 'https://proxy.other.org:5678';

        /* execute */
        const result = resolveProxyConfig();

        /* test */
        expect(result?.protocol).toEqual('https');
        expect(result?.host).toEqual('proxy.other.org');
        expect(result?.port).toEqual(5678);
        expect(result?.auth).toBeUndefined();
    });

    it('https_proxy defined - uses values of https_proxy no authentication section', function () {
        /* prepare */
        delete process.env.http_proxy;
        process.env.https_proxy = 'https://proxy.other.org:5678';

        /* execute */
        const result = resolveProxyConfig();

        /* test */
        expect(result?.protocol).toEqual('https');
        expect(result?.host).toEqual('proxy.other.org');
        expect(result?.port).toEqual(5678);
        expect(result?.auth).toBeUndefined();
    });

    it('invalid http_proxy defined - takes defined https_proxy', function () {
        /* prepare */
        process.env.http_proxy = 'invalid-proxy-url';
        process.env.https_proxy = 'https://proxy.other.org:5678';

        /* execute */
        const result = resolveProxyConfig();

        /* test */
        expect(result?.protocol).toEqual('https');
        expect(result?.host).toEqual('proxy.other.org');
        expect(result?.port).toEqual(5678);
        expect(result?.auth).toBeUndefined();
    });

    it('invalid https_proxy defined - throws error', function () {
        /* prepare */
        delete process.env.http_proxy;
        process.env.https_proxy = 'invalid-proxy-url';
        
        /* execute + test */
        expect(() => resolveProxyConfig()).toThrowError(/Invalid URL/);
    });

    it('https_proxy defined without port - uses default port 443', function () {
        /* prepare */
        process.env.http_proxy = 'http://proxy.example.org';
        process.env.https_proxy = 'https://proxy.other.org';

        /* execute */
        const result = resolveProxyConfig();

        /* test */
        expect(result?.protocol).toEqual('https');
        expect(result?.host).toEqual('proxy.other.org');
        expect(result?.port).toEqual(443);
        expect(result?.auth).toBeUndefined();
    });

    it('http_proxy defined without port - uses default port 80', function () {
        /* prepare */
        process.env.http_proxy = 'http://proxy.example.org';
        delete process.env.https_proxy;

        /* execute */
        const result = resolveProxyConfig();

        /* test */
        expect(result?.protocol).toEqual('http');
        expect(result?.host).toEqual('proxy.example.org');
        expect(result?.port).toEqual(80);
        expect(result?.auth).toBeUndefined();
    });

    it('http_proxy defined with invalid protocol - throws error', function () {
        /* prepare */
        process.env.http_proxy = 'ftp://proxy.example.org';
        delete process.env.https_proxy;

        /* execute + test */
        expect(() => resolveProxyConfig()).toThrowError(/Accepted protocols are "http" and "https"/);
    });

    it('https_proxy defined with invalid protocol - throws error', function () {
        /* prepare */
        delete process.env.http_proxy;
        process.env.https_proxy = 'sftp://proxy.other.org';

        /* execute + test */
        expect(() => resolveProxyConfig()).toThrowError(/Accepted protocols are "http" and "https"/);
    });
});

describe('ensureAcceptedScanType', function() {

    test('ensureAcceptedScanType - no params results in default', () => {

        /* execute */
        const result = ensureAcceptedScanType([]);

        /* test */
        expect(result).toEqual([ScanType.CodeScan]);

    });

    test('ensureAcceptedScanType - wrong params results in default', () => {

        /* execute */
        const result = ensureAcceptedScanType(['x','y']);

        /* test */
        expect(result).toEqual([ScanType.CodeScan]);

    });

    test('ensureAcceptedScanType - partly wrong params results in correct one only', () => {

        /* execute */
        const result = ensureAcceptedScanType(['licensescan','y']);

        /* test */
        expect(result).toEqual([ScanType.LicenseScan]);

    });

    test('ensureAcceptedScanType - wellknown params accepted case insensitive but converted', () => {

        /* execute */
        const result = ensureAcceptedScanType(['licensescan','SECRETscan','CodeScan', 'IAcScan']);

        /* test */
        expect(result).toContain(ScanType.LicenseScan);
        expect(result).toContain(ScanType.CodeScan);
        expect(result).toContain(ScanType.SecretScan);
        expect(result).toContain(ScanType.IacScan);

        expect(result.length).toBe(4);

    });

});