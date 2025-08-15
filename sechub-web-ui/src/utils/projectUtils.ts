// SPDX-License-Identifier: MIT
export function formatDate (dateString: string) {
  if (dateString === '') {
    return
  }
  const date = new Date(dateString)
  const day = String(date.getDate()).padStart(2, '0')
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const year = date.getFullYear()
  const time = date.toTimeString().split(' ')[0]
  return `${day}.${month}.${year} ${time}`
}

export function getTrafficLightClass (value: string) {
  switch (value) {
    case 'OFF':
      return 'traffic-light-off'
    case 'RED':
      return 'traffic-light-red'
    case 'GREEN':
      return 'traffic-light-green'
    case 'YELLOW':
      return 'traffic-light-yellow'
    default:
      return 'traffic-light-none'
  }
}

export const getIconFromScanStatus = (text: string): string => {
  switch (text) {
    case 'ERROR':
    case 'FAILED':
      return 'mdi-alert-circle-outline'
    case 'WARNING':
      return 'mdi-alert-circle-outline'
    case 'INFO':
      return 'mdi-information-outline'
    case 'SUCCESS':
      return 'mdi-check-circle-outline'
    default:
      return ''
  }
}

export const getIconColorFromScanStatus = (text: string): string => {
  switch (text) {
    case 'ERROR':
    case 'FAILED':
      return 'error'
    case 'WARNING':
      return 'warning'
    case 'INFO':
      return 'primary'
    case 'SUCCESS':
      return 'success'
    default:
      return 'grey'
  }
}

export function calculateIcon (severity: string) {
  const iconMap: Record<string, string> = {
    CRITICAL: 'mdi-alert-circle-outline',
    HIGH: 'mdi-alert-circle-outline',
    MEDIUM: 'mdi-alert-circle-outline',
    LOW: 'mdi-information-outline',
    INFO: 'mdi-information-outline',
  }
  return iconMap[severity] || ''
}

export function calculateColor (severity: string) {
  const colorMap: Record<string, string> = {
    CRITICAL: 'error',
    HIGH: 'error',
    MEDIUM: 'warning',
    LOW: 'success',
    INFO: 'primary',
  }
  return colorMap[severity] || 'layer_01'
}
