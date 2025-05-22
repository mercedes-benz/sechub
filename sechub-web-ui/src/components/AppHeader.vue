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

      <v-btn icon="mdi-logout-variant" @click="logout()" />

      <v-btn :href="faqLink" icon="mdi-forum-outline" target="_blank" />
    </template>
  </v-app-bar>
</template>

<script lang="ts">
  import defaultClient from '@/services/defaultClient'
  import { useRouter } from 'vue-router'
  import { useI18n } from 'vue-i18n'
  import { useFetchUserDetail } from '@/composables/useUserDetail'
  import { useConfig } from '@/config'
  import { handleApiError } from '@/utils/apiErrorHandler'

  export default {
    name: 'AppHeader',

    setup () {
      const { t } = useI18n()
      const router = useRouter()
      const config = useConfig()

      const faqLink = ref(config.value.SECHUB_FAQ_LINK)

      const welcomeText = ref('')
      const username = ref('')
      const isLoggedIn = ref(false)

      // initially fetch and store user data
      userFetchUserDetailInformation()

      async function userFetchUserDetailInformation () {
        const { userDetailInformation, error } = await useFetchUserDetail()

        if (userDetailInformation.value.userId) {
          isLoggedIn.value = true
          welcomeText.value = t('GREETING')
          username.value = userDetailInformation.value.userId
        } else {
          isLoggedIn.value = false
          username.value = 'SecHub'
          console.error(error.value)
        }
      }

      function goToUserPage () {
        router.push('/user')
      }

      async function logout () {
        try {
          await defaultClient.withOtherApi.userLogout()
          // redirect to root after logout
          router.push('/login')
        } catch (err) {
		      handleApiError(err)
          console.error(err)
        }
      }

      return {
        username,
        faqLink,
        welcomeText,
        isLoggedIn,
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
