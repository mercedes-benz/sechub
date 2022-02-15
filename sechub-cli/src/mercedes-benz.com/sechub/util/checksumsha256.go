// SPDX-License-Identifier: MIT

package util

import (
	"crypto/sha256"
	"encoding/hex"
	"io"
	"os"
)

// CreateChecksum creates a sha256 checksum string
func CreateChecksum(filename string) string {
	hasher := sha256.New()

	f, err := os.Open(filename)
	HandleError(err, 1)
	defer f.Close()

	_, err = io.Copy(hasher, f)
	HandleError(err, 1)

	return hex.EncodeToString(hasher.Sum(nil))
}
