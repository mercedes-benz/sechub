// SPDX-License-Identifier: MIT
import { SecHubReport } from '@/generated-sources/openapi'
import { defineStore } from 'pinia'

const STORE_NAME = 'reportStore'

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
        this.reports.push(newReport)
      } else if (JSON.stringify(this.reports[existingReportIndex]) !== JSON.stringify(newReport)) {
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
