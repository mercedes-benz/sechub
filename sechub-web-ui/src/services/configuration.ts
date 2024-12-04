// SPDX-License-Identifier: MIT
import { CONFIG } from '@/config'
import { Configuration } from '@/generated-sources/openapi'

const apiConfig = new Configuration({
  basePath: CONFIG.HOST,
  username: "admin",
  password: "myTop$ecret",
  apiKey: "myTop$ecret"
  

  // todo: check if cookie set by server is sent back (for auth reasons)
})

export default apiConfig
console.log("CONFIG:", apiConfig.basePath)