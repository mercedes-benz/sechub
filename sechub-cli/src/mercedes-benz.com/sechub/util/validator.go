// SPDX-License-Identifier: MIT

package util

// ArrayContains - check if value is contained in an array of elements
func StringArrayContains(array []string, value string) bool {
	for _, element := range array {
		if element == value {
			return true
		}
	}
	return false
}
