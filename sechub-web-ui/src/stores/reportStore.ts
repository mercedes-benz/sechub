// SPDX-License-Identifier: MIT
import { SecHubReport } from 'sechub-openapi-ts-client'
import { defineStore } from 'pinia'

const STORE_NAME = 'reportStore'
const MAXIMAL_CACHE_SIZE = 10

const getReports = (): SecHubReport[] => {
  const reports = localStorage.getItem(STORE_NAME)
  return reports ? JSON.parse(reports) : []
}

export const useReportStore = defineStore(STORE_NAME, {
  state: () => ({
    reports: getReports() as SecHubReport[],
  }),

  actions: {
    storeReport (newReport: SecHubReport) {
      const uuid = newReport.jobUUID

      if (!uuid) {
        return
      }

      const existingReportIndex = this.reports.findIndex(report => report.jobUUID === uuid)

      if (existingReportIndex === -1) {
        // report not in store
        this.reports.push(newReport)

        if (this.reports.length > MAXIMAL_CACHE_SIZE) {
          // we store MAXIMAL_CACHE_SIZE reports in localstorage
          this.reports.shift()
        }
      } else if (JSON.stringify(this.reports[existingReportIndex]) !== JSON.stringify(newReport)) {
        // report already in store but has canged
        this.reports[existingReportIndex] = newReport
      }

      localStorage.setItem(STORE_NAME, JSON.stringify(this.reports))
    },
  },

  getters: {
    getReportByUUID: state => {
      return (uuid: string) => state.reports.find(report => report.jobUUID === uuid) || undefined
    },
  },
})
