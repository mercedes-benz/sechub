// SPDX-License-Identifier: MIT
import { App } from 'vue';
import { Configuration, ProjectAdministrationApi} from '@/generated-sources/openapi'; 

const apiClientPlugin = {
  install(app: App) {
    const config = new Configuration({ basePath: "http://my.example.com" });
    const projectAdminApi = new ProjectAdministrationApi(config);

    app.config.globalProperties.$projectAdminApi = projectAdminApi;

    app.provide('projectAdminClient', projectAdminApi)
  }
};

export default apiClientPlugin;