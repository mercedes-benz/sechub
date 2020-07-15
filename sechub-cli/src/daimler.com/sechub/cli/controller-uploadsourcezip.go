// SPDX-License-Identifier: MIT

package cli

import (
	"log"
	"os"
)

func uploadSourceZipFile(context *Context) {
	if !context.isUploadingSourceZip() {
		return
	}
	if !context.config.keepTempFiles {
		/* when debug mode enabled we keep the zipped file */
		defer os.Remove(context.sourceZipFileName)
	}
	extraParams := map[string]string{
		"title":    "Sourcecode zipped",
		"author":   "Sechub client " + Version(),
		"checkSum": context.sourceZipFileChecksum,
	}
	request, err := newfileUploadRequest(buildUploadSourceCodeAPICall(context), extraParams, "file", context.sourceZipFileName)

	if err != nil {
		log.Fatal(err)
	}
	request.SetBasicAuth(context.config.user, context.config.apiToken)

	response, err := context.HTTPClient.Do(request)

	HandleHTTPErrorAndResponse(response, err)
}
