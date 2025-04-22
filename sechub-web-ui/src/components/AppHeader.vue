<!-- SPDX-License-Identifier: MIT -->
<template>
  <v-app-bar
    color="background_paper"
  >
    <router-link to="/projects">
      <img
        alt="Logo"
        class="logo ma-2 pa-2"
        src="@/assets/sechub-logo-shield.png"
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
      <v-btn icon="mdi-account" @click="goToUserPage()" />

      <v-btn icon="mdi-logout-variant" @click="logout()" />

      <v-btn :href="faqLink" icon="mdi-forum-outline" target="_blank" />
    </template>
  </v-app-bar>
</template>

<script lang="ts">
  import defaultClient from '@/services/defaultClient'
  import { useRouter } from 'vue-router'
  import { useConfig } from '@/config'

  export default {
    name: 'ProjectComponent',

    setup () {
      const router = useRouter()
      const username = 'SecHub User'
      const config = useConfig()

      const faqLink = ref(config.value.SECHUB_FAQ_LINK)

      function goToUserPage () {
        router.push('/user')
      }

      async function logout () {
        try {
          await defaultClient.withOtherApi.userLogout()
          // redirect to root after logout
          router.push('/login')
        } catch (err) {
          console.error(err)
        }
      }

      return {
        username,
        faqLink,
        logout,
        goToUserPage,
      }
    },
  }
</script>

<style scoped>
.logo {
  height: 90%;
  max-height: 64px;
  width: auto;
}
</style>
