<!-- SPDX-License-Identifier: MIT -->
<template>
  <v-card>
    <v-card-item>
      <v-card-title>{{ $t('PROJECT_DETAILS_TITLE') }} {{ projectData.projectId }}</v-card-title>
    </v-card-item>
    <v-card-item>
      <v-card-title>{{ $t('PROJECT_DETAILS_OWNER') }}</v-card-title>
    </v-card-item>
    <v-card-text>
      {{ projectData.owner }}
    </v-card-text>
    <div v-if="projectData.assignedUsers">
      <v-card-item>
        <v-card-title>{{ $t('PROJECT_DETAILS_MEMBERS') }}</v-card-title>
      </v-card-item>
      <v-card-text>
        <div
          v-for="(member,i) in projectData.assignedUsers"
          :key="i"
        >{{ member }}</div>
      </v-card-text>
    </div>
    <div v-else>
      <v-card-text>
        {{ $t('PROJECT_DETAILS_NOT_OWNED') }}
      </v-card-text>
    </div>

  </v-card>
</template>
<script lang="ts">
  import { defineComponent, toRefs } from 'vue'
  import { ProjectData } from '@/generated-sources/openapi'

  interface Props {
    projectData: ProjectData
  }

  export default defineComponent({
    props: {
      projectData: {
        type: Object,
        required: true,
      },
    },

    setup (props: Props) {
      const { projectData } = toRefs(props)

      return {
        projectData,
      }
    },
  })
</script>
