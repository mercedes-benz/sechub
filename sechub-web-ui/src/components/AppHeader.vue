<!-- SPDX-License-Identifier: MIT -->
<template>
  <v-app-bar
    color="background_paper"
  >
    <router-link to="/">
      <img
        alt="Logo"
        class="logo ma-2 pa-1"
        src="@/assets/sechub-logo.svg"
      >
    </router-link>

    <v-container fill-height fluid>
      <v-row
        align:center
        justify="center"
      >
        <v-col class="pa-0">
          <div>Welcome</div>
        </v-col>
      </v-row>
      <v-row
        align:center
        justify="center"
      >
        <v-col class="pa-0">
          <v-app-bar-title> {{ username }}</v-app-bar-title>
        </v-col>
      </v-row>
    </v-container>

    <!-- Search Bar
        <v-spacer></v-spacer>

        <v-responsive
        class="mx-auto">
            <v-text-field hide-details solo single-line class="ml-5"
            rounded
            light variant="outlined"
            label="search"
            prepend-inner-icon="mdi-magnify" />
        </v-responsive>
        -->

    <template #append>
      <v-btn icon="mdi-account" />

      <v-btn icon="mdi-logout-variant" />

      <v-btn icon="mdi-forum-outline" />
    </template>
  </v-app-bar>
</template>

<script setup lang="ts">
  const username = 'Erika Mustermann'
</script>

<style scoped>
.logo {
  height: 90%;
  max-height: 64px;
  width: auto;
}
</style>
