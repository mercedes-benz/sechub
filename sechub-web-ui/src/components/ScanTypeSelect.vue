<!-- SPDX-License-Identifier: MIT -->
<template>
  <v-chip-group
    class="ma-2"
    multiple
  >
    <v-chip
      v-for="(item, i) in scanOptions"
      :key="i"
      :color="selectedScanOptions.includes(item) ? 'primary' : 'default'"
      filter
      outlined
      :value="item"
      @click="toggleSelection(item)"
    >
      {{ item }}
    </v-chip>
  </v-chip-group>
</template>
<script lang="ts">
  import { defineComponent } from 'vue'

  interface Props {
    scanOptions: Array<String>
    selectedScanOptions: Array<String>
  }

  export default defineComponent({
    props: {
      scanOptions: {
        type: Array<string>,
        required: true,
      },
      selectedScanOptions: {
        type: Array<string>,
        reuired: true,
      },
    },
    emits: ['onToggleSelection'],
    setup (props: Props, { emit }) {
      function toggleSelection (item: String) {
        emit('onToggleSelection', item)
      }
      return {
        toggleSelection,
      }
    },
  })
</script>
