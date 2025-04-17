/* SPDX-License-Identifier: MIT */
import { ProjectData } from '@/generated-sources/openapi'
import defaultClient from '@/services/defaultClient'
import { useProjectStore } from '@/stores/projectStore'

export async function useFetchProjects () {
  const store = useProjectStore()
  const projects = ref<ProjectData[]>([])
  const error = ref<string | undefined>(undefined)

  try {
    projects.value = await defaultClient.withProjectApi.getAssignedProjectDataList()
    store.storeProjects(projects.value)
  } catch (err) {
    const errMsg = 'ERROR_MESSAGE_FETCHING_PROJECTS'
    error.value = errMsg
    console.error(errMsg, err)
  } 

  return { projects, error }
}
