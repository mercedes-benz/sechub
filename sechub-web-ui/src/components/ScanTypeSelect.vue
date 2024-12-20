<!-- SPDX-License-Identifier: MIT -->
<template>
    <v-chip-group
        multiple
        class="ma-2"
        >
        <v-chip
        v-for="(item, i) in scanOptions"
        :key="i"
        outlined
        filter
        :value="item"
        @click="toggleSelection(item)"
        :color="selectedScanOptions.includes(item) ? 'primary' : 'default'"
        >
        {{ item }}
        </v-chip>
    </v-chip-group>
</template>
<script lang="ts">
  import { defineComponent, toRefs } from 'vue'

  interface Props {
    scanOptions: Array<String>
    selectedScanOptions: Array<String>
  }

  export default defineComponent({
  props: {
    scanOptions: {
        type: Array<string>,
        required: true
    },
    selectedScanOptions:{
        type: Array<string>,
        reuired: true
    },
  },
  emits:['onToggleSelection'],
  setup (props: Props, { emit }){
    const {scanOptions, selectedScanOptions} = toRefs(props)

    function toggleSelection(item: String) {
        emit('onToggleSelection', item)
    }
    return {
        toggleSelection
    }
  }
})
</script>