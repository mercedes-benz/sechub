// SPDX-License-Identifier: MIT

package util

import (
	"encoding/json"
)

// IsValidJSON checks if given string byte array is valid json or not
func IsValidJSON(b []byte) bool {

	var js map[string]interface{}
	return json.Unmarshal(b, &js) == nil
}
