// SPDX-License-Identifier: MIT

import axios from 'axios';
import { load } from 'cheerio';
import * as core from '@actions/core';

export async function getClientVersion(clientVersion: string): Promise<string> {
    if (!clientVersion || clientVersion.length === 0) {
        throw new Error('No SecHub client version defined!');
    }

    if (!isValidVersion(clientVersion)) {
        throw new Error(`Invalid SecHub client version: ${clientVersion}`);
    }

    if (clientVersion === 'latest') {
        // This will return a html page containing a refresh tag like
        // <meta http-equiv="refresh" content="0; url=https://github.com/mercedes-benz/sechub/releases/download/v1.5.0-client/sechub-cli-1.5.0.zip
        const latestClientDownloadPage = 'https://mercedes-benz.github.io/sechub/latest/client-download.html';
        // parse the client version from the redirect URL
        const redirectUrl = await getRedirectUrl(latestClientDownloadPage);
        clientVersion = redirectUrl
            .substring(redirectUrl.lastIndexOf('/') + 1)
            .replace('sechub-cli-', '')
            .replace('.zip', '');
    }

    return clientVersion;
}

function isValidVersion(version: string): boolean {
    if (version === 'build') {
        return true; // build is always okay
    }
    const regex = /^\d+\.\d+\.\d+$|^latest$/;
    return regex.test(version);
}

async function getRedirectUrl(url: string): Promise<string> {
    const response = await axios.get(url);
    const $ = load(response.data);
    const metaRefreshTag = $('meta[http-equiv="refresh"]');

    if (metaRefreshTag.length > 0) {
        const content = metaRefreshTag.attr('content');
        if (content==null){
            throw new Error('No redirect content found');
        }
        const redirectUrl = content!.split(';')[1].split('=')[1];
        core.debug(`Redirect URL found: ${redirectUrl}`);
        return redirectUrl;
    } else {
        throw new Error(`No redirect URL found at: ${url}`);
    }
}