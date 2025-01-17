<!-- SPDX-License-Identifier: MIT -->
<template>
  <v-card>
    Hallo
  </v-card>
</template>

<script lang="ts">
  import defaultClient from '@/services/defaultClient'
  import { useRouter } from 'vue-router'
  import { ref } from 'vue'

  export default {
    name: 'UserDetailInformation',

    setup () {
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
