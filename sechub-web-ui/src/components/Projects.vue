<!-- SPDX-License-Identifier: MIT -->
<template>
    <v-card 
  class="mr-auto"
  color="background_default"
  width="70%"
    >
  <v-toolbar 
  color="background_default" 
  width="70%" >
    <v-toolbar-title>{{ $t('PROJECTS') }}</v-toolbar-title>
  </v-toolbar>


    <v-list
    lines="two"
    bg-color="background_default"
    >
      <v-list-item
        v-for="(item, i) in items"
        :key="i"
        :value="item"
        rounded="xl"
        class="my-3"
        >

        <template v-slot:prepend>
            <v-icon :icon="item.icon" :class="getIconClass(item)"></v-icon>
        </template>
        <template v-slot:title>
            <span>{{ item.text }}</span>
        </template>

      </v-list-item>
    </v-list>
  </v-card>
</template>

<script>
  export default {
    data: () => ({
      items: [
        { text: 'Project 1', icon: 'mdi-cube', owned: true },
        { text: 'Project 2', icon: 'mdi-cube', owned: false  },
        { text: 'Project 3', icon: 'mdi-cube', owned: true  },
      ],
    }),
    methods: {
    getIconClass(item) {
      return item.owned ? 'text-primary' : '';
    },
  },
  }
</script>

<style scoped>
  .text-primary {
    color: "primary" !important;
  }
</style>