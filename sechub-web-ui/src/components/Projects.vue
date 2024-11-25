<!-- SPDX-License-Identifier: MIT -->
<template>
    <v-list
    lines="two" 
    max-width="90%"
    class="ma-auto"
    >
      <v-list-item
        v-for="(item, i) in items"
        :key="i"
        :value="item"
        rounded="xl"
        class="ma-1 pa-5"
      >

            <template v-slot:prepend>
                <v-icon :icon="item.icon" :class="getIconClass(item)"></v-icon>
            </template>
            <template v-slot:title>
                <span>{{ item.text }}</span>
            </template>

      </v-list-item>
    </v-list>
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
      return item.owned ? 'text-primary' : 'text-secondary';
    },
  },
  }
</script>

<style scoped>
    .text-secondary {
    color: "secondary" !important;
    }
    .text-primary {
    color: "primary" !important;
    }
</style>