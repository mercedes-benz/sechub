// SPDX-License-Identifier: MIT
export function formatDate (dateString: string) {
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
