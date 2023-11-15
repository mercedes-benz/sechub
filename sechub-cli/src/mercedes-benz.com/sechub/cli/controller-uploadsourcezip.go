// SPDX-License-Identifier: MIT

package cli

import (
	"os"

	sechubUtil "mercedes-benz.com/sechub/util"
)

func uploadSourceZipFile(context *Context) {
	if !context.config.keepTempFiles {
		// remove zip file after upload
		defer os.Remove(context.sourceZipFileName)
	}

	extraParams := map[string]string{
		"checkSum": context.sourceZipFileChecksum,
	}

	request, err := newFileUploadRequestViaPipe(buildUploadSourceCodeAPICall(context), extraParams, "file", context.sourceZipFileName)
	sechubUtil.HandleError(err, ExitCodeIOError)

	handleHTTPRequestAndResponse(context, request, false) // HTTP call for upload
}

func uploadBinariesTarFile(context *Context) {
	if !context.config.keepTempFiles {
		// remove tar file after upload
		defer os.Remove(context.binariesTarFileName)
	}

	extraParams := map[string]string{
		"checkSum": context.binariesTarFileChecksum,
	}

	request, err := newFileUploadRequestViaPipe(buildUploadBinariesAPICall(context), extraParams, "file", context.binariesTarFileName)
	sechubUtil.HandleError(err, ExitCodeIOError)

	handleHTTPRequestAndResponse(context, request, false) // HTTP call for upload
}
