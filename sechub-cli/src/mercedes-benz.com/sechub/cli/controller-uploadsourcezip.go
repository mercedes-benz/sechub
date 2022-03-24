// SPDX-License-Identifier: MIT

package cli

import (
	"os"

	sechubUtil "mercedes-benz.com/sechub/util"
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

	request, err := newFileUploadRequestViaPipe(buildUploadSourceCodeAPICall(context), extraParams, "file", context.sourceZipFileName)
	sechubUtil.HandleError(err, ExitCodeIOError)

	request.SetBasicAuth(context.config.user, context.config.apiToken)

	handleHTTPRequestAndResponse(context, request) // HTTP call for upload
}
