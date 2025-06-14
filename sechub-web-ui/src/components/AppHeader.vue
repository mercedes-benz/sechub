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
  import { useRouter } from 'vue-router'
  import { useI18n } from 'vue-i18n'
  import { useFetchUserDetail } from '@/composables/useUserDetail'
  import { useConfig } from '@/config'

  import { useProjectStore } from '@/stores/projectStore'
  import { useReportStore } from '@/stores/reportStore'
  import { useUserDetailInformationStore } from '@/stores/userDetailInformationStore'
  import { useTmpFalsePositivesStore } from '@/stores/tempFalsePositivesStore'

  export default {
    name: 'AppHeader',

    setup () {
      const { t } = useI18n()
      const router = useRouter()
      const config = useConfig()

      const projectStore = useProjectStore()
      const reportStore = useReportStore()
      const userDetailInformationStore = useUserDetailInformationStore()
      const tempFalsePositivesStore = useTmpFalsePositivesStore()

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
        // Reset all storages
        projectStore.$reset()
        reportStore.$reset()
        tempFalsePositivesStore.$reset()
        userDetailInformationStore.$reset()

        // Perform a full browser navigation to /logout so the nginx redirect is followed
        window.location.href = '/logout'
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
