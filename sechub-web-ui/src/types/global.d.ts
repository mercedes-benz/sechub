// SPDX-License-Identifier: MIT
declare module 'insane';

interface ImportMeta {
  env: {
    VITE_API_USERNAME: string | undefined,
    VITE_API_PASSWORD: string | undefined,
    VITE_API_BASIC_AUTH_DEV: string | undefined,
    VITE_SECHUB_USER_SUPPORT_EMAIL: string | undefined,
    VITE_SECHUB_USER_SUPPORT_WEBSITE: string | undefined,
    VITE_SECHUB_FAQ_LINK: string | undefined,
    VITE_SECHUB_UPLOAD_SOURCES_MAXIMUM_BYTES: number | undefined,
    VITE_SECHUB_UPLOAD_BINARIES_MAXIMUM_BYTES: number | undefined,
  }
}
