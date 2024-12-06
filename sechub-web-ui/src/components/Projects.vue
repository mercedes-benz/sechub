<!-- SPDX-License-Identifier: MIT -->
<template>
  <v-card class="mr-auto" color="background_paper" width="70%">
    <v-toolbar color="background_paper" width="70%">
      <v-toolbar-title>{{ $t('PROJECTS') }}</v-toolbar-title>
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
      >
        <template #prepend>
          <v-icon v-if="project.isOwned" :class="ownedClass" icon="mdi-cube" />
          <v-icon v-else icon="mdi-cube" />
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
</template>

<script>
  import defaultClient from '@/services/defaultClient'

  export default {
    name: 'ProjectsComponent',

    setup () {
      const projects = ref([])
      const loading = ref(true)
      const error = ref(null)

      onMounted(async () => {
        try {
          projects.value = await defaultClient.withProjectApi.getAssignedProjectDataList()
        } catch (err) {
          error.value = 'ProjectAPI error fetching assigned projects.'
          console.error('ProjectAPI error fetching assigned projects:', err)
        } finally {
          loading.value = false
        }
      })

      return {
        ownedClass: {
          'project-owned': true,
        },
        projects,
        loading,
        error,
      }
    },
  }
</script>

<style scoped>
  .project-owned {
    color: rgb(var(--v-theme-primary)) !important;
  }
  .v-list-item{
    background-color: rgb(var(--v-theme-layer_01)) !important;
  }
</style>
