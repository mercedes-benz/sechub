import { ProjectAdministrationApi } from "@/generated-sources/openapi";
import apiConfig from "./configuration";

const projectApi = new ProjectAdministrationApi(apiConfig);

export default {
    async getProjects(){
        const response = projectApi.getAssignedProjectDataList();
        return response;
    }
}