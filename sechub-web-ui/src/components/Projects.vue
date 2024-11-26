<!-- SPDX-License-Identifier: MIT -->
<template>
    <v-card 
  class="mr-auto"
  color="background_paper"
  width="70%"
    >
  <v-toolbar 
  color="background_paper" 
  width="70%" >
    <v-toolbar-title>{{ $t('PROJECTS') }}</v-toolbar-title>
  </v-toolbar>


    <v-list
    lines="two"
    bg-color="background_paper"
    >
      <v-list-item
        v-for="(item, i) in items"
        :key="i"
        :value="item"
        rounded="lg"
        class="ma-5"
        background="red"
        >
          <template v-slot:prepend>
              <v-icon :icon="item.icon" :class="getIconClass(item)"></v-icon>
          </template>
          <template v-slot:title>
              <span>{{ item.text }}</span>
          </template>
          <template v-slot:subtitle>
            <span  v-if="item.owned">({{ $t('OWNED') }})</span>
            <span v-else>({{ $t('MEMBER') }})</span>
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
        { text: 'Project 2', icon: 'mdi-cube', owned: false },
        { text: 'Project 3', icon: 'mdi-cube', owned: true },
      ],
    }),
    methods: {
    getIconClass(item) {
      return item.owned ? 'project-owned' : '';
    },
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