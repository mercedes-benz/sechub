// SPDX-License-Identifier: MIT

import { AxiosProxyConfig } from "axios";
import { ScanType } from 'sechub-openapi-ts-client'
import * as core from '@actions/core';

const COMMA = ',';

/**
 * Splits an input string by comma and sanitizes the result by removing leading and trailing whitespaces.
 * Result will contain non-empty strings only.
 *
 * @returns array of comma separated strings
 */
export function split(input: string): string[] {
    if (!input) return [];

    return input
        .split(COMMA)
        .map(item => item.trim())
        .filter(item => item.length > 0);
}

export function safeAcceptedScanTypes(data: string[]): string[] {
    const accepted: string[] = [];
    if (data){
        for (const entry of data) {
            if (equalIgnoreCase(entry, ScanType.CodeScan)) {
                accepted.push(ScanType.CodeScan);
            } else if (equalIgnoreCase(entry, ScanType.LicenseScan)) {
                accepted.push(ScanType.LicenseScan);
            } else if (equalIgnoreCase(entry, ScanType.SecretScan)) {
                accepted.push(ScanType.SecretScan);
            } else if (equalIgnoreCase(entry, ScanType.IacScan)){
                accepted.push(ScanType.IacScan);
            }
        }
    }
    // the default and fallback is codeScan
    if (accepted.length == 0) {
        accepted.push(ScanType.CodeScan);
    }
    return accepted;
}

/**
 * This method checks the default environment variables 'http_proxy' and 'https_proxy' for http/https proxy specification.
 * The variables are expected to be specified in this format: http_proxy='http://user:password@proxy.example.org:1234'.
 * This results in an URL object like this:
 * URL {
      href: 'http://user:password@proxy.example.org:1234/',
      origin: 'http://proxy.example.org:1234',
      protocol: 'http:',
      username: 'user',
      password: 'password',
      host: 'proxy.example.org:1234',
      hostname: 'proxy.example.org',
      port: '1234',
      pathname: '/',
      search: '',
      searchParams: URLSearchParams {},
      hash: ''
    }
 * @returns configured AxiosProxyConfig or undefined if no proxy was
 * @throws error if the proxy URL inside the ENV variable is an invalid URL
 */
export function resolveProxyConfig(): URL | undefined {
    const httpsProxy = process.env.https_proxy;
    const httpProxy = process.env.http_proxy;

    const proxy = httpsProxy || httpProxy;
    
    if (!proxy) {
        return undefined;
    }
    try {
        return new URL(proxy);
    } catch(error: any) {
        throw new Error(`Trying to setup proxy configuration received the error: "${error.message}". Make sure to use the following syntax: http://user:password@proxy.example.org:1234 or without credentials: http://proxy.example.org:1234`);
    }
}

function equalIgnoreCase(string1: string, string2: string): boolean {
    return (string1 ?? '').toLowerCase() === (string2 ?? '').toLowerCase();
}
