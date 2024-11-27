// SPDX-License-Identifier: MIT
import { CONFIG } from 'src/config'
import { Configuration } from "@/generated-sources/openapi";

const apiConfig = new Configuration({
    basePath: CONFIG.API_HOST,
    // todo: check if cookie set by server is sent back (for auth reasons)
})

export default apiConfig;