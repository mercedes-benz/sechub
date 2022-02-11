// SPDX-License-Identifier: MIT

package util

import "time"

// SecHub default date format (including time zone)
const dateFormat = "2006-01-02 15:04:05 (Z07:00)"

// Timestamp - return current time as string in SecHub default format
func Timestamp() string {
	return (time.Now().Format(dateFormat))
}
