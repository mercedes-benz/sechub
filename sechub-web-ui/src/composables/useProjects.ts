/* SPDX-License-Identifier: MIT */
import { ProjectData } from '@/generated-sources/openapi'
import defaultClient from '@/services/defaultClient'
import { useProjectStore } from '@/stores/projectStore'

export function useFetchProjects () {
  const store = useProjectStore()
  const projects = ref<ProjectData[]>([])
  const error = ref<string | undefined>(undefined)
  const loading = ref(true)

  const fetchProjects = async () => {
    try {
      projects.value = await defaultClient.withProjectApi.getAssignedProjectDataList()
      store.storeProjects(projects.value)
    } catch (err) {
      error.value = 'ProjectAPI error fetching assigned projects.'
      console.error('ProjectAPI error fetching assigned projects:', err)
    } finally {
      loading.value = false
    }
  }

  fetchProjects()

  return { projects, error, loading }
}
