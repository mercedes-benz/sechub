/* SPDX-License-Identifier: MIT */
import defaultClient from '@/services/defaultClient'
import { SecHubReport } from '@/generated-sources/openapi'
import { useReportStore } from '@/stores/reportStore'
import { useI18n } from 'vue-i18n'

export async function useFetchReport (projectId: string, jobUUID:string) {
  const { t } = useI18n()
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
    const errMsg = t('JOB_ERROR_REPORT_JSON_DONLOAD_FAILED' + jobUUID)
    console.error(errMsg, err)
    error.value = errMsg
  } finally {
    loading.value = false
  }

  return { report, error, loading }
}
