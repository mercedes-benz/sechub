// SPDX-License-Identifier: MIT
import { ProjectData } from '@/generated-sources/openapi'
import { defineStore } from 'pinia'

export const useProjectStore = defineStore('projectStore', {
  state: () => ({
    projects: [] as ProjectData[],
  }),
  actions: {
    storeProjects (projects: ProjectData[]) {
      this.projects = projects
    },
  },
  getters: {
    getProjectById: state => {
      return (id: string) => state.projects.find(project => project.projectId === id) || {}
    },
  },
})
