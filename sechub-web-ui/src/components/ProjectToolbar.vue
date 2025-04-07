<!-- SPDX-License-Identifier: MIT -->
<template>
  <v-toolbar color="background_paper">
    <v-toolbar-title>{{ projectId }}</v-toolbar-title>
    <template #prepend>
      <v-btn
        icon="mdi-arrow-left"
        @click="router.go(-1)"
      />
    </template>
  </v-toolbar>
</template>
<script lang="ts">
  import { defineComponent } from 'vue'
  import { useRouter } from 'vue-router'

  export default defineComponent({
    props: {
      projectId: {
        type: String,
        required: true,
      },
    },

    setup () {
      const router = useRouter()

      return {
        router,
      }
    },
  })
</script>
<style scoped>
.v-toolbar-title {
    font-weight: bold;
}
</style>
