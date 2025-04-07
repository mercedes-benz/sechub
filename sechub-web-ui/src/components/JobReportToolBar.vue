<!-- SPDX-License-Identifier: MIT -->
<template>
  <v-card class="mr-auto" color="background_paper">
    <ProjectToolbar
      :project-id="projectId"
    />
    <v-toolbar color="background_paper">
      <v-toolbar-title>
        {{ jobUUID }}
        {{ scanType }}
      </v-toolbar-title>
      <v-icon
        class="ma-2"
        :class="getTrafficLightClass(trafficLight)"
        icon="mdi-circle"
        size="x-large"
      />
    </v-toolbar>
  </v-card>
</template>
<script lang="ts">
  import { defineComponent } from 'vue'
  import { getTrafficLightClass } from '@/utils/projectUtils'

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
