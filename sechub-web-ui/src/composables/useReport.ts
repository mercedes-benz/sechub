/* SPDX-License-Identifier: MIT */
import defaultClient from '@/services/defaultClient'
import { SecHubReport } from 'sechub-openapi-ts-client'
import { useReportStore } from '@/stores/reportStore'
import { handleApiError } from '@/services/apiErrorHandler'

export async function useFetchReport (projectId: string, jobUUID:string) {
  const store = useReportStore()
  const report = ref<SecHubReport>({})
  const error = ref<string | undefined>(undefined)
  const loading = ref(true)

  try {
    report.value = await defaultClient.withExecutionApi.userDownloadJobReport({
      projectId,
      jobUUID,
    })
    store.storeReport(report.value)
  } catch (err) {
    handleApiError(err)
    const errMsg = 'JOB_ERROR_REPORT_JSON_DONLOAD_FAILED'
    console.error(errMsg, err)
    error.value = errMsg
  } finally {
    loading.value = false
  }

  return { report, error, loading }
}
