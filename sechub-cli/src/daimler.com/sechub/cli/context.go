// SPDX-License-Identifier: MIT
package cli

import (
	"crypto/tls"
	"net/http"
	"time"
)

// Context - Structure containing all relevant data for SecHub client functionality
type Context struct {
	config                *Config
	unfilledByteValue     []byte
	byteValue             []byte
	HttpClient            *http.Client
	sechubConfig          *SecHubConfig
	sourceZipFileChecksum string
	sourceZipFileName     string
}

func (context *Context) isUploadingSourceZip() bool {
	return context.sourceZipFileName != ""
}

// NewContext - Creates a new CLI context by given config
func NewContext(config *Config) *Context {
	context := new(Context)
	context.config = config

	/* setup HTTP client */
	tr := &http.Transport{
		TLSClientConfig: &tls.Config{InsecureSkipVerify: config.trustAll},
	}
	context.HttpClient = &http.Client{Timeout: time.Duration(context.config.timeOutNanoseconds), Transport: tr}

	return context
}
