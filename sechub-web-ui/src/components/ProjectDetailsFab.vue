<!-- SPDX-License-Identifier: MIT -->
<template>
  <v-fab
    absolute
    :class="fabClass"
    color="primary"
    height="60px"
    location="top end"
    offset
    rounded="circle"
    width="60px"
    @click="onToggleProjectDetails"
  >
    <v-icon>{{ showProjectsDetails ? 'mdi-chevron-right' : 'mdi-chevron-left' }}</v-icon>
    <v-tooltip v-if="showProjectsDetails" activator="parent" location="top">{{ $t('PROJECT_DETAILS_TOOLTIP_CLOSE') }}</v-tooltip>
    <v-tooltip v-else activator="parent" location="top">{{ $t('PROJECT_DETAILS_TOOLTIP_OPEN') }}</v-tooltip>
  </v-fab>
</template>
<script lang="ts">
  import { defineComponent, toRefs } from 'vue'

  interface Props {
    showProjectsDetails: boolean
  }

  export default defineComponent({
    props: {
      showProjectsDetails: {
        type: Boolean,
        required: true,
      },
    },
    emits: ['onToggleDetails'],
    setup (props: Props, { emit }) {
      const { showProjectsDetails } = toRefs(props)

      function onToggleProjectDetails () {
        emit('onToggleDetails')
      }

      const fabClass = computed(() => {
        return {
          'custom-fab-position-open': showProjectsDetails.value,
          'custom-fab-position-closed': !showProjectsDetails.value,
        }
      })

      return {
        onToggleProjectDetails,
        fabClass,
      }
    },
  })
</script>
<style scoped>
.custom-fab-position-open {
  position: absolute;
  top: 160px;
  right: 65px;
}
.custom-fab-position-closed {
  position: absolute;
  top: 160px;
  right: -30px;
}
</style>
