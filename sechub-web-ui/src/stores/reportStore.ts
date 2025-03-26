// SPDX-License-Identifier: MIT
import { SecHubReport } from '@/generated-sources/openapi'
import { defineStore } from 'pinia'

const STORE_NAME = 'reportStore'

const getReports = () => {
  const reports = localStorage.getItem(STORE_NAME)
  return reports ? JSON.parse(reports) : new Map<string, SecHubReport>()
}

export const useReportStore = defineStore(STORE_NAME, {
  state: () => ({
    reports: getReports() as Map<string, SecHubReport>
  }),

  actions: {
    storeReport(newReport: SecHubReport) {
        const uuid = newReport.jobUUID;
        if (!uuid){
            return
        }
        console.log(uuid)
        const existingReport = this.reports.get(uuid);
        console.log(existingReport)

        if (!existingReport || (JSON.stringify(existingReport) !== JSON.stringify(newReport))) {
            this.reports.set(uuid, newReport);
            console.log("stored")
        }
    },
  },

  getters: {
    getReportByUUID: state => {
        return (uuid: string) => state.reports.get(uuid) || undefined
    }
  },
})
