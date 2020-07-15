// SPDX-License-Identifier: MIT

package cli

import (
	"crypto/tls"
	"net/http"
	"time"
)

// Context represents global context of this sechub client call. Contains configuration, states, client and more
type Context struct {
	config                *Config
	unfilledByteValue     []byte
	byteValue             []byte
	HTTPClient            *http.Client
	sechubConfig          *SecHubConfig
	sourceZipFileChecksum string
	sourceZipFileName     string
}

func (context *Context) isUploadingSourceZip() bool {
	return context.sourceZipFileName != ""
}

// NewContext - creates a new CLI context by given config
func NewContext(config *Config) *Context {
	context := new(Context)
	context.config = config

	/* setup HTTP client */
	tr := &http.Transport{
		TLSClientConfig: &tls.Config{InsecureSkipVerify: config.trustAll},
	}
	context.HTTPClient = &http.Client{Timeout: time.Duration(context.config.timeOutNanoseconds), Transport: tr}

	return context
}
