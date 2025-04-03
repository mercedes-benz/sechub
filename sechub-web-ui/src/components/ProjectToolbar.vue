<!-- SPDX-License-Identifier: MIT -->
<template>
      <v-toolbar color="background_paper">
      <v-toolbar-title>{{ projectId }}</v-toolbar-title>
      <template v-slot:prepend>
          <v-btn
          icon="mdi-arrow-left" 
          @click="router.go(-1)" />
      </template>
      </v-toolbar>
</template>
<script lang="ts">
  import { defineComponent, toRefs } from 'vue'
  import { useRouter } from 'vue-router'

interface Props {
    projectId: string
}

export default defineComponent({
  props: {
    projectId: {
      type: String,
      required: true,
    }
  },

  setup (props: Props) {
    const { projectId } = toRefs(props)
    const router = useRouter()

    return {
        router,
        projectId,
    }
  },
})
</script>
<style scoped>
.v-toolbar-title {
    font-weight: bold;
}
</style>