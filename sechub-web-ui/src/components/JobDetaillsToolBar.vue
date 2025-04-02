<!-- SPDX-License-Identifier: MIT -->
<template>
    <v-card class="mr-auto" color="background_paper">
      <ProjectToolbar 
        :project-id="projectId"
        />
        <v-toolbar color="background_paper">
            <v-toolbar-title>
                {{ scanType }}
                {{ jobUUID }}
                <v-icon 
                icon="mdi-circle"
                class="ma-2"
                :class="getTrafficLightClass(trafficLight)">
                </v-icon>
            </v-toolbar-title>
        </v-toolbar>
    </v-card>
</template>
<script lang="ts">
  import { defineComponent, toRefs } from 'vue'
  import { getTrafficLightClass } from '@/utils/projectUtils'

interface Props {
  scanType: string
  jobUUID: string
  trafficLight: string
  projectId: string
}

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
    }
  },

  setup (props: Props) {
    const { jobUUID, trafficLight } = toRefs(props)

    return {
        getTrafficLightClass,
        jobUUID,
        trafficLight
    }
  },
})
</script>