// SPDX-License-Identifier: MIT
declare module 'insane';

interface ImportMeta {
  env: {
    VITE_API_HOST: string | undefined,
    VITE_API_USER: string | undefined,
    VITE_API_PASSWORD: string | undefined,
    VITE_API_LOCAL_DEV: boolean | undefined,
  }
}
