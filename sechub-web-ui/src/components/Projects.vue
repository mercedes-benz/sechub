<!-- SPDX-License-Identifier: MIT -->
<template>
  <v-card
    class="mr-auto"
    color="background_paper"
    width="70%"
  >
    <v-toolbar
      color="background_paper"
      width="70%"
    >
      <v-toolbar-title>{{ $t('PROJECTS') }}</v-toolbar-title>
    </v-toolbar>

    <v-list
      bg-color="background_paper"
      lines="two"
    >
      <v-list-item
        v-for="(item, i) in items"
        :key="i"
        background="red"
        class="ma-5"
        rounded="lg"
        :value="item"
      >
        <template #prepend>
          <v-icon :class="getIconClass(item)" :icon="item.icon" />
        </template>
        <template #title>
          <span>{{ item.text }}</span>
        </template>
        <template #subtitle>
          <span v-if="item.owned">({{ $t('OWNED') }})</span>
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
      getIconClass (item) {
        return item.owned ? 'project-owned' : ''
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
