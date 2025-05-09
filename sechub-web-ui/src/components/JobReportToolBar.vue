<!-- SPDX-License-Identifier: MIT -->
<template>
  <v-card class="mr-auto" color="background_paper">
    <ProjectToolbar
      :project-id="projectId"
    />
    <v-toolbar color="background_paper">
      <v-toolbar-title>
        <span>{{ jobUUID }}</span>
        <span
          class="ml-6 sechub-primary-color"
        >{{ scanType }}</span>

      </v-toolbar-title>
      <template #prepend>
        <v-icon
          class="ma-2"
          :class="getTrafficLightClass(trafficLight)"
          icon="mdi-circle"
          size="x-large"
        />
      </template>

    </v-toolbar>
  </v-card>
</template>
<script lang="ts">
  import { defineComponent } from 'vue'
  import { getTrafficLightClass } from '@/utils/projectUtils'
  import '@/styles/sechub.scss'

  export default defineComponent({
    props: {
      scanType: {
        type: String,
        required: true,
      },
      jobUUID: {
        type: String,
        required: true,
      },
      trafficLight: {
        type: String,
        required: true,
      },
      projectId: {
        type: String,
        required: true,
      },
    },

    setup () {
      return {
        getTrafficLightClass,
      }
    },
  })
</script>
