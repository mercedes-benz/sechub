// SPDX-License-Identifier: MIT

package cli

import (
	"crypto/tls"
	"net/http"
	"time"
)

// Context - represents global context of this sechub client call. Structure contains all relevant data for SecHub client functionality (configuration, states, client and more
type Context struct {
	config                    *Config
	inputForContentProcessing []byte // used for log output and as base for templating, no data replaced (so apitoken env variable is still there as variable and suitable for logging)
	contentToSend             []byte // template output used for communication to server - contains replaced parts from env variables (e.g. password instead of variable)
	HTTPClient                *http.Client
	sechubConfig              *SecHubConfig
	binariesTarFileChecksum   string
	binariesTarFileName       string
	binariesTarUploadNeeded   bool
	sourceZipFileChecksum     string
	sourceZipFileName         string
	sourceZipUploadNeeded     bool
	jobStatus                 *jobStatusResult
	jobList                   *jobListResult
}

// NewContext - creates a new CLI context by given config
func NewContext(config *Config) *Context {
	context := new(Context)
	context.config = config
	context.jobStatus = new(jobStatusResult)
	context.jobList = new(jobListResult)

	/* setup HTTP client */
	tr := &http.Transport{
		TLSClientConfig: &tls.Config{InsecureSkipVerify: config.trustAll},
		Proxy:           http.ProxyFromEnvironment,
	}
	context.HTTPClient = &http.Client{Timeout: time.Duration(context.config.timeOutNanoseconds), Transport: tr}

	return context
}
