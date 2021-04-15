// SPDX-License-Identifier: MIT

package cli

import (
	"bytes"
	"fmt"
	"io"
	"mime/multipart"
	"net/http"
	"os"
	"path/filepath"

	sechubUtil "daimler.com/sechub/util"
)

/**
 * Send http request with standard header, means accepted and content-type are set to json
 */
func sendWithDefaultHeader(method string, url string, context *Context) *http.Response {
	header := make(map[string]string)
	header["Content-Type"] = "application/json"
	header["Accept"] = "application/json"

	return sendWithHeader(method, url, context, header)
}

func sendWithHeader(method string, url string, context *Context, header map[string]string) *http.Response {
	/* we use inputForContentProcessing - means origin content, unfilled, prevents password leak in logs */
	sechubUtil.LogDebug(context.config.debug, fmt.Sprintf("Sending %s:%s\n Headers: %s\n Origin-Content: %q", method, url, header, context.inputForContentProcessing))

	/* prepare */
	req, err1 := http.NewRequest(method, url, bytes.NewBuffer(context.contentToSend)) // we use "contentToSend" and not "inputForContentProcessing" !
	sechubUtil.HandleHTTPError(err1, ExitCodeHTTPError)
	req.SetBasicAuth(context.config.user, context.config.apiToken)

	for key := range header {
		req.Header.Set(key, header[key])
	}

	/* send */
	response, err2 := context.HTTPClient.Do(req) //http.Post(createJobURL, "application/json", bytes.NewBuffer(context.contentToSend))
	HandleHTTPErrorAndResponse(response, err2, context)
	return response
}

// Creates a new file upload http request with optional extra params
func newfileUploadRequest(uploadToURL string, params map[string]string, paramName, path string) (*http.Request, error) {
	file, err := os.Open(path)
	if err != nil {
		return nil, err
	}
	defer file.Close()

	body := &bytes.Buffer{}
	writer := multipart.NewWriter(body)
	part, err := writer.CreateFormFile(paramName, filepath.Base(path))
	if err != nil {
		return nil, err
	}
	_, err = io.Copy(part, file)

	if err != nil {
		return nil, err
	}

	for key, val := range params {
		_ = writer.WriteField(key, val)
	}
	err = writer.Close()
	if err != nil {
		return nil, err
	}

	req, err := http.NewRequest("POST", uploadToURL, body)
	req.Header.Set("Content-Type", writer.FormDataContentType())
	return req, err
}
