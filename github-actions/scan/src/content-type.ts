// SPDX-License-Identifier: MIT

import { SecHubConfigurationModelBuilderData } from './configuration-builder'

export class ContentType {

    static SOURCE = 'source';
    static BINARIES = 'binaries';

    public static isSource(data: string | undefined): boolean {
        if (!data) {
            return false;
        }
        return data.toLowerCase() === this.SOURCE;
    }

    public static isBinary(data: string | undefined): boolean {
        if (!data) {
            return false;
        }
        return data.toLowerCase() === this.BINARIES;
    }

    static safeAcceptedContentType(contentType: string): string {
        if (ContentType.isSource(contentType)) {
            return ContentType.SOURCE;
        }
        if (ContentType.isBinary(contentType)) {
            return ContentType.BINARIES;
        }
        return ContentType.SOURCE;
    }

}