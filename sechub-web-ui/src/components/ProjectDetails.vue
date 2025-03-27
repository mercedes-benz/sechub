<!-- SPDX-License-Identifier: MIT -->
<template>
  <v-card>
    <v-card-item>
      <v-card-title>{{ $t('PROJECT_DETAILS_TITLE') }} {{ project.projectId }}</v-card-title>
    </v-card-item>
    <v-card-item>
      <v-card-title>{{ $t('PROJECT_DETAILS_OWNER') }}</v-card-title>
    </v-card-item>
    <v-card-text>
      <v-btn v-tooltip="project.owner.emailAddress" variant="text">{{ project.owner.userId }}</v-btn>
    </v-card-text>
    <div v-if="project.assignedUsers">
      <v-card-item>
        <v-card-title>{{ $t('PROJECT_DETAILS_MEMBERS') }}</v-card-title>
      </v-card-item>
      <v-card-text>
        <div v-for="(member, i) in project.assignedUsers" :key="i">
          <v-btn v-tooltip="member.emailAddress" variant="text">{{ member.userId }}</v-btn>
        </div>
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
  import { ProjectData, ProjectUserData } from '@/generated-sources/openapi'

  interface Props {
    projectData: ProjectData,
    selectedUserData: ProjectUserData|undefined,
  }

  export default defineComponent({
    props: {
      projectData: {
        type: Object,
        required: true,
      },

      selectedUserData: {
        type: Object,
        required: false,
      },
    },

    setup (props: Props) {
      const { projectData } = toRefs(props)
      const { selectedUserData } = toRefs(props)

      const project = projectData
      const selectedUser = selectedUserData

      return {
        project, selectedUser,
      }
    },
  })
</script>
