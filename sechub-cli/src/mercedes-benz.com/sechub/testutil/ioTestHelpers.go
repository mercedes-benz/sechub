// SPDX-License-Identifier: MIT

package util

import (
	"io"
)

// CountBytesInStream - Return number of bytes in input. Reads until EOF.
func CountBytesInStream(input io.ReadCloser) int64 {
	buf, _ := io.ReadAll(input)
	return int64(len(buf))
}
