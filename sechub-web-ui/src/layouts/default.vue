<!-- SPDX-License-Identifier: MIT -->
<template>
  <v-app>
    <AppHeader />
    <v-main class="background-default">
      <div class="pa-10">
        <router-view />
      </div>
    </v-main>
  </v-app>
</template>

<style scoped>
.background-default{
  color: rgb(var(--v-theme-background_default)) !important;
  background-color: rgb(var(--v-theme-background_default)) !important;
}
</style>