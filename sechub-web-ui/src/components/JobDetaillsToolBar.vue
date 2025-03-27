<!-- SPDX-License-Identifier: MIT -->
<template>
    <v-card class="mr-auto" color="background_paper">
        <v-toolbar color="background_paper">
            <v-toolbar-title>
                {{ jobUUID }}
                <v-icon 
                icon="mdi-circle"
                class="ma-2"
                :class="getTrafficLightClass(trafficLight)">
                </v-icon>
            </v-toolbar-title>
            <v-btn icon="mdi-reply" @click="routerGoBack()" />
        </v-toolbar>
    </v-card>
</template>
<script lang="ts">
  import { defineComponent, toRefs } from 'vue'
  import { getTrafficLightClass } from '@/utils/projectUtils'
  import { useRouter } from 'vue-router'

interface Props {
  jobUUID: string
  trafficLight: string
}

export default defineComponent({
  props: {
    jobUUID: {
      type: String,
      required: true,
    },
    trafficLight: {
        type: String,
        required: true,
    }
  },

  setup (props: Props, {}) {
    const { jobUUID, trafficLight } = toRefs(props)
    const router = useRouter()

    function routerGoBack () {
        router.go(-1)
      }

    return {
        getTrafficLightClass,
        routerGoBack,
        jobUUID,
        trafficLight
    }
  },
})
</script>