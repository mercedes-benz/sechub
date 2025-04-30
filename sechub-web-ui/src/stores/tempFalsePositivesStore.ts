// SPDX-License-Identifier: MIT
import { defineStore } from 'pinia'

const STORE_NAME = 'tmpFalsePositives'
const MAXIMAL_CACHE_SIZE = 10

const getFalsePositives = () => {
  const falsePositives = localStorage.getItem(STORE_NAME)
  return falsePositives ? JSON.parse(falsePositives) : []
}

export interface TmpFalsePositives {
  jobUUID: string,
  findingIds: Array<number>
}

// this storage holds only the current userDetailInformation
export const useTmpFalsePositivesStore = defineStore(STORE_NAME, {
  state: () => ({
    falsePositives: getFalsePositives() as TmpFalsePositives [],
  }),
  actions: {
    storeFalsePositives (newFalsePositive: TmpFalsePositives) {
      const uuid = newFalsePositive.jobUUID

      if (!uuid) {
        return
      }

      const existingReportIndex = this.falsePositives.findIndex(fp => fp.jobUUID === uuid)

      if (existingReportIndex === -1) {
        // falsePositives not in store
        this.falsePositives.push(newFalsePositive)

        if (this.falsePositives.length > MAXIMAL_CACHE_SIZE) {
          // we store MAXIMAL_CACHE_SIZE falsePositives in localstorage
          this.falsePositives.shift()
        }
      } else if (JSON.stringify(this.falsePositives[existingReportIndex]) !== JSON.stringify(newFalsePositive)) {
        // falsePositives already in store but has canged
        this.falsePositives[existingReportIndex] = newFalsePositive
      }

      localStorage.setItem(STORE_NAME, JSON.stringify(this.falsePositives))
    },
  },
  getters: {
    getFalsePositivesByUUID: state => {
      return (uuid: string) => state.falsePositives.find(falsePositive => falsePositive.jobUUID === uuid) || undefined
    },
  },
})
