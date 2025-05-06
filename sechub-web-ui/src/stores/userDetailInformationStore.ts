// SPDX-License-Identifier: MIT
import { UserDetailInformation } from '@/generated-sources/openapi'
import { defineStore } from 'pinia'

const STORE_NAME = 'userDetailStore'

const getDetail = () => {
  const userDetailInformation = localStorage.getItem(STORE_NAME)
  return userDetailInformation ? JSON.parse(userDetailInformation) : {}
}

// this storage holds only the current userDetailInformation
export const useUserDetailInformationStore = defineStore(STORE_NAME, {
  state: () => ({
    userDetailInformation: getDetail() as UserDetailInformation,
  }),
  actions: {
    storeUserDetailInformation (userDetailInformation: UserDetailInformation) {
      this.userDetailInformation = userDetailInformation
      localStorage.setItem(STORE_NAME, JSON.stringify(this.userDetailInformation))
    },

    clearUserDetailInformation () {
      this.userDetailInformation = {}
      localStorage.setItem(STORE_NAME, JSON.stringify(this.userDetailInformation))
    },
  },
  getters: {
    getUserDetailInformation: state => {
      return () => state.userDetailInformation
    },
  },
})
