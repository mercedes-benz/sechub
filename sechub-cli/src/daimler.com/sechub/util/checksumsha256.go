// SPDX-License-Identifier: MIT
package util

import (
	"crypto/sha256"
	"encoding/hex"
	"io"
	"log"
	"os"
)

func CreateChecksum(filename string) string {
	hasher := sha256.New()

	f, err := os.Open(filename)
	if err != nil {
		log.Fatal(err)
		os.Exit(1)
	}
	defer f.Close()
	if _, err := io.Copy(hasher, f); err != nil {
		log.Fatal(err)
		os.Exit(1)
	}

	return hex.EncodeToString(hasher.Sum(nil))
}
