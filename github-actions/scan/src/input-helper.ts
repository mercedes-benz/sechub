// SPDX-License-Identifier: MIT

import { AxiosProxyConfig } from "axios";
import { handleError } from "./action-helper";

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
export function resolveProxyConfig(): AxiosProxyConfig | undefined {
    const httpsProxy = process.env.https_proxy;
    const httpProxy = process.env.http_proxy;

    const proxy = httpsProxy || httpProxy;
    
    if (!proxy) {
        return undefined;
    }
    try {
        const proxyUrl = new URL(proxy);
        const proxyConfig = {
            protocol: proxyUrl.protocol.replace(':', ''), // Remove the trailing colon
            host: proxyUrl.hostname,
            port: proxyUrl.port ? parseInt(proxyUrl.port, 10) : getProtocolDefaultPort(proxyUrl.protocol),
            ...(proxyUrl.username || proxyUrl.password ? {
                auth: {
                    username: proxyUrl.username,
                    password: proxyUrl.password
                }
            } : undefined)
        };
        return proxyConfig;
    } catch(error: any) {
        throw new Error(`Trying to setup proxy configuration received the error: "${error.message}". Make sure to use the following syntax: http://user:password@proxy.example.org:1234 or without credentials: http://proxy.example.org:1234`);
    }
}

function getProtocolDefaultPort(protocol: string) {
    if (!protocol){
        throw new Error('No protocol defined!');
    }
    if (protocol.startsWith('https')) {
        return 443;
    } else if (protocol.startsWith('http')) {
        return 80;
    } else {
        throw new Error('Accepted protocols are "http" and "https"');
    }
}
