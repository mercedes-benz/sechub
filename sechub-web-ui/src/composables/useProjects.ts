/* SPDX-License-Identifier: MIT */
import { ProjectData } from 'sechub-openapi-ts-client'
import defaultClient from '@/services/defaultClient'
import { useProjectStore } from '@/stores/projectStore'
import { handleApiError } from '@/services/apiErrorHandler'

export async function useFetchProjects () {
  const store = useProjectStore()
  const projects = ref<ProjectData[]>([])
  const error = ref<string | undefined>(undefined)

  try {
    projects.value = await defaultClient.withProjectApi.getAssignedProjectDataList()
    store.storeProjects(projects.value)
  } catch (err) {
    handleApiError(err)
    const errMsg = 'ERROR_MESSAGE_FETCHING_PROJECTS'
    error.value = errMsg
    console.error(errMsg, err)
  }

  return { projects, error }
}
