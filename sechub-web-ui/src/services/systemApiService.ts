// SPDX-License-Identifier: MIT
import { SystemApi } from "@/generated-sources/openapi";
import apiConfig from './configuration';

const systemApi = new SystemApi(apiConfig)

export default systemApi;

