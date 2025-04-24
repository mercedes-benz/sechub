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
 * The variables are expected to be specified in this format: http_proxy='http://user:password@proxy.example.org:3128'.
 * This results in an URL object like this:
 * URL {
      href: 'http://user:password@proxy.example.org:3128/',
      origin: 'http://proxy.example.org:3128',
      protocol: 'http:',
      username: 'user',
      password: 'password',
      host: 'proxy.example.org:3128',
      hostname: 'proxy.example.org',
      port: '3128',
      pathname: '/',
      search: '',
      searchParams: URLSearchParams {},
      hash: ''
    }
 * @returns configured AxiosProxyConfig or undefined if no proxy was
 * @throws error if the proxy URL inside the ENV variable is an invalid URL
 */
export function resolveProxyConfig(): AxiosProxyConfig | undefined {
    const httpProxy = process.env.http_proxy || undefined;
    const httpsProxy = process.env.https_proxy || undefined;

    const proxy = httpProxy || httpsProxy;
    let proxyConfig = undefined;

    if (!proxy) {
        return proxyConfig;
    }
    try {
        const proxyUrl = new URL(proxy);
        proxyConfig = {
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
        throw new Error(`Trying to setup proxy configuration received the error: "${error.message}". Make sure to use the following syntax: http://user:password@proxy.example.org:3128 or without credentials: http://proxy.example.org:3128`);
    }
}

function getProtocolDefaultPort(protocol: string) {
    if (protocol === 'http:') {
        return 80;
    } else if (protocol === 'https:') {
        return 443;
    } else {
        throw new Error('Accepted protocols are "http" and "https"');
    }
}
