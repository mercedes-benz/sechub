<!-- SPDX-License-Identifier: MIT -->
<template>
  <v-card
    class="mr-auto"
    color="background_paper"
    width="70%"
  >
    <v-toolbar
      color="background_paper"
      width="70%"
    >
      <v-toolbar-title>{{ $t('PROJECTS') }}</v-toolbar-title>
    </v-toolbar>

    <div v-if="alive"><p>ALIVE</p></div>
    <div v-else><p>NOT ALIVE</p></div>

    <!-- case the user is not assigned to any project -->
    <div v-if="projects.length === 0">
      <v-list
        bg-color="background_paper"
        lines="two"
      >
        <v-list-item class="ma-5" rounded="lg">{{ $t('NO_PROJECTS_ASSIGNED') }}</v-list-item>
      </v-list>
    </div>
    
    <v-list
      v-else
      bg-color="background_paper"
      lines="two"
    >
      <v-list-item
        v-for="project in projects"
        :key="project.projectId"
        class="ma-5"
        rounded="lg"
        :value="project"
      >
        <template #prepend>
          <v-icon :icon="mdi-cube" />
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
      const alive =ref(false)
      const projects = ref([])
      const loading = ref(true)
      const error = ref(null)

      onMounted(async () => {
        try {
          alive.value = defaultClient.withSystemApi.anonymousCheckAliveHead()
          // projects.value = defaultClient.withProjectApi.getAssignedProjectDataList()
          const response = defaultClient.withProjectApi.getAssignedProjectDataListRaw()
          console.log("HELP:", (await response).value)
        } catch (err) {
          error.value = 'ProjectAPI error fetching assigned projects.'
          console.error('ProjectAPI error fetching assigned projects:', err)
        } finally {
          loading.value = false
        }
      })

      return {
        alive,
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
