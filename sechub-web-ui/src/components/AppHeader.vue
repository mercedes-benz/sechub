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
          <div>{{ welcomeText }}</div>
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

    <template v-if="isLoggedIn" #append>
      <v-btn icon="mdi-account" @click="goToUserPage()" />

      <v-btn icon="mdi-logout-variant" />

      <v-btn icon="mdi-forum-outline" />
    </template>
  </v-app-bar>
</template>

<script lang="ts">
  import { useRouter } from 'vue-router'
  import { useFetchUserDetail } from '@/composables/useUserDetail'
  import { useI18n } from 'vue-i18n'

  export default {
    name: 'AppHeader',

    setup () {
      const { t } = useI18n()
      const router = useRouter()
      const welcomeText = ref('')
      const username = ref('')
      const isLoggedIn = ref(false)

      // initially fetch and store user data
      userFetchUserDetailInformation()

      function goToUserPage () {
        router.push('/user')
      }

      async function userFetchUserDetailInformation(){
        const { userDetailInformation, error } = await useFetchUserDetail()
        if(userDetailInformation){
          isLoggedIn.value = true
          welcomeText.value = t('GREETING')
          username.value = userDetailInformation.value.userId || 'SecHub User'
        }else{
          console.log(error.value)
        }
      }

      return {
        username,
        welcomeText,
        isLoggedIn,
        goToUserPage,
        t,
      }
    }
  }
</script>

<style scoped>
.logo {
  height: 90%;
  max-height: 64px;
  width: auto;
}
</style>
