<!-- SPDX-License-Identifier: MIT -->
<template>
  <di />
</template>
<script lang="ts">
  import { useRouter } from 'vue-router'

  export default {
    setup () {
      const router = useRouter()
      router.push({
        name: `/projects/`,
      })
    },
  }
</script>
