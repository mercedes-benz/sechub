// SPDX-License-Identifier: MIT
import { ProjectData } from '@/generated-sources/openapi'
import { defineStore } from 'pinia'

const STORE_NAME = 'projectStore'

const getProjects = () => {
  const projects = localStorage.getItem(STORE_NAME)
  return projects ? JSON.parse(projects) : []
}

export const useProjectStore = defineStore(STORE_NAME, {
  state: () => ({
    projects: getProjects() as ProjectData[],
  }),
  actions: {
    storeProjects (projects: ProjectData[]) {
      this.projects = projects
      localStorage.setItem(STORE_NAME, JSON.stringify(this.projects))
    },
  },
  getters: {
    getProjectById: state => {
      return (id: string) => state.projects.find(project => project.projectId === id) || undefined
    },
  },
})
