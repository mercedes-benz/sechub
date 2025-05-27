/* SPDX-License-Identifier: MIT */
import defaultClient from '@/services/defaultClient'
import { UserDetailInformation } from '@/generated-sources/openapi'
import { useUserDetailInformationStore } from '@/stores/userDetailInformationStore'
import { handleApiError } from '@/services/apiErrorHandler'

export async function useFetchUserDetail () {
  const store = useUserDetailInformationStore()
  const error = ref()
  const userDetailInformation = ref<UserDetailInformation>({})

  try {
    userDetailInformation.value = await defaultClient.withUserSelfServiceApi.userFetchUserDetailInformation()
    store.storeUserDetailInformation(userDetailInformation.value)
  } catch (error) {
    console.error(error)
    store.clearUserDetailInformation()
    handleApiError(error)
  }

  return { userDetailInformation, error }
}
