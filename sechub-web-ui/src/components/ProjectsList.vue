<!-- SPDX-License-Identifier: MIT -->
<template>
  <v-container fluid>
    <v-row>
      <v-col cols="12" md="8">

        <v-alert
          v-model="alert"
          closable
          color="error"
          density="compact"
          :title="$t('API_ERROR_TITLE')"
          type="warning"
          variant="tonal"
        >
          {{ error }}
        </v-alert>

        <v-card class="mr-auto" color="background_paper">
          <v-toolbar color="background_paper">
            <v-toolbar-title>{{ $t('PROJECTS') }}</v-toolbar-title>
            <template #prepend>
              <v-btn icon="mdi-refresh" @click="fetchData()" />
            </template>
          </v-toolbar>

          <!-- Case when the user is not assigned to any project -->
          <div v-if="projects.length === 0 && !loading">
            <v-list bg-color="background_paper" lines="two">
              <v-list-item v-if="error" class="ma-5" rounded="lg">{{ $t('ERROR_FETCHING_DATA') }}</v-list-item>
              <v-list-item v-else class="ma-5" rounded="lg">{{ $t('NO_PROJECTS_ASSIGNED') }}</v-list-item>
            </v-list>
          </div>

          <!-- Iterate over the projects array -->
          <v-list v-else bg-color="background_paper" lines="two">
            <v-list-item
              v-for="(project, i) in projects"
              :key="i"
              class="ma-5"
              rounded="lg"
              :value="project"
              @click="openProjectPage(project)"
            >
              <template #prepend>
                <v-icon
                  v-if="project.isOwned"
                  :class="ownedClass"
                  icon="mdi-cube"
                />
                <v-icon v-else class="ma-2" icon="mdi-cube" />
              </template>
              <template #title>
                <span>{{ project.projectId }}</span>
              </template>
              <template #subtitle>
                <span v-if="project.isOwned">({{ $t('OWNED') }})</span>
                <span v-else>({{ $t('MEMBER') }})</span>
              </template>
            </v-list-item>
          </v-list>
        </v-card>
      </v-col>
      <v-col cols="12" md="4">
        <!-- Possible Aside content -->
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
  import { useFetchProjects } from '@/composables/useProjects'
  import { ProjectData } from '@/generated-sources/openapi'
  import { useRouter } from 'vue-router'
  import { useI18n } from 'vue-i18n'

  export default {
    name: 'ProjectListComponent',

    setup () {
      const { t } = useI18n()
      const router = useRouter()
      const projects = ref<ProjectData[]>([])
      const error = ref<string | undefined>(undefined)
      const loading = ref(true)
      const alert = ref(false)

      fetchData()

      const openProjectPage = (project: ProjectData) => {
        router.push({
          name: `/projects/[id]/`,
          params: {
            id: project.projectId,
          },
        })
      }

      async function fetchData () {
        loading.value = true
        error.value = undefined
        alert.value = false

        const { projects: reloadProjects, error: reloadError, loading: reloadLoading } = await useFetchProjects()

        projects.value = reloadProjects.value
        loading.value = reloadLoading.value

        if (reloadError.value) {
          error.value = t(reloadError.value)
          alert.value = true
        }
      }

      return {
        ownedClass: {
          'project-owned': true,
        },
        projects,
        loading,
        error,
        alert,
        fetchData,
        openProjectPage,
      }
    },
  }
</script>

<style scoped>
  .project-owned {
    color: rgb(var(--v-theme-primary)) !important;
    margin-right: 8px;
  }
  .v-list-item{
    background-color: rgb(var(--v-theme-layer_01)) !important;
  }
</style>
