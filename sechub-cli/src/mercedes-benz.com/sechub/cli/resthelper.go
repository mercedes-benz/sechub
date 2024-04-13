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
	"strconv"
	"time"

	sechubUtil "mercedes-benz.com/sechub/util"
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
	/* prepare */
	request, err1 := http.NewRequest(method, url, bytes.NewBuffer(context.contentToSend)) // we use "contentToSend" and not "inputForContentProcessing" !
	sechubUtil.HandleHTTPError(err1, ExitCodeHTTPError)

	for key := range header {
		request.Header.Set(key, header[key])
	}

	/* send */
	return handleHTTPRequestAndResponse(context, request, true)
}

// handleHTTPRequestAndResponse - run http request and handle the response in a resilient way
func handleHTTPRequestAndResponse(context *Context, request *http.Request, printContentOfRequest bool) *http.Response {
	var response *http.Response
	var err error

	/* we use inputForContentProcessing - means origin content, unfilled, prevents password leak in logs */
	if context.config.debugHTTP {
		sechubUtil.LogDebug(true, fmt.Sprintf("HTTP %s %s\n  HTTP Headers: %s\n  Content length of HTTP request: %d bytes",
			request.Method, request.URL, request.Header, request.ContentLength))
		if printContentOfRequest {
			sechubUtil.LogDebug(true, fmt.Sprintf("Content of HTTP request: %q", request.Body))
		}
	} else {
		sechubUtil.LogDebug(context.config.debug, fmt.Sprintf("HTTP %s %s", request.Method, request.URL))
	}

	// Set authorization data after logging - so no secrets are logged
	request.SetBasicAuth(context.config.user, context.config.apiToken)

	for retry := 0; retry <= HTTPMaxRetries; retry++ {
		response, err = context.HTTPClient.Do(request)     // Execute HTTP call
		sechubUtil.HandleHTTPError(err, ExitCodeHTTPError) // Handle networking errors etc. (exit)

		if response.StatusCode != 503 || request.Method == "POST" {
			// exit loop on
			// - any status code beside 503
			// - HTTP POST, then the message body can only be read once (io.reader) so a subsequent call will fail.
			break
		}

		// Resilience handling
		if retry == 0 {
			sechubUtil.LogWarning(
				fmt.Sprintf("Received Status Code '%d' from SecHub server. Server may be busy. Retrying in %d seconds...",
					response.StatusCode, context.config.waitSeconds))
		}
		time.Sleep(time.Duration(context.config.waitSeconds) * time.Second)
		if retry < HTTPMaxRetries {
			sechubUtil.Log(fmt.Sprintf("          retry %d/%d", retry+1, HTTPMaxRetries), false)
		}
	}

	if context.config.debugHTTP {
		sechubUtil.LogDebug(true, fmt.Sprintf("HTTP response:\n%+v", response))
	} else {
		sechubUtil.LogDebug(context.config.debug, fmt.Sprintf("HTTP response code: %d", response.StatusCode))
	}
	sechubUtil.HandleHTTPResponse(response, ExitCodeHTTPError)

	return response
}

// Creates a new file upload http request with optional extra params (low memory footprint regardless the file size)
func newFileUploadRequestViaPipe(uploadToURL string, params map[string]string, paramName, filename string) (*http.Request, error) {
	r, w := io.Pipe()
	m := multipart.NewWriter(w)

	go func() {
		defer w.Close()

		for key, val := range params {
			_ = m.WriteField(key, val)
		}

		part, err := m.CreateFormFile(paramName, filepath.Base(filename))
		if err != nil {
			return
		}

		file, err := os.Open(filename)
		if err != nil {
			return
		}
		defer file.Close()

		if _, err = io.Copy(part, file); err != nil {
			return
		}

		m.Close() // Write trailing boundary end line
	}()

	request, err := http.NewRequest("POST", uploadToURL, r)
	request.Header.Set("Content-Type", m.FormDataContentType())

	filesize := sechubUtil.GetFileSize(filename)
	request.Header.Set("x-file-size", strconv.FormatInt(filesize, 10))
	request.ContentLength = computeContentLengthOfFileUpload(params, paramName, filename, filesize)

	return request, err
}

func computeContentLengthOfFileUpload(params map[string]string, paramName, filename string, filesize int64) (contentLength int64) {
	/* Real world example of multipart content sent to SecHub server when uploading:
	--f76dd0c1a814e0af2f4d197827fd9caa1e9636276e064454356141ae1347
	Content-Disposition: form-data; name="checkSum"

	ccdcf7c07a8461f8aeb44f6bbd2166d184c79f7acfd86cb3415dcb452f274a63
	--f76dd0c1a814e0af2f4d197827fd9caa1e9636276e064454356141ae1347
	Content-Disposition: form-data; name="file"; filename="sourcecode-testproject.zip"
	Content-Type: application/octet-stream

	<binary content of zip file here>
	--f76dd0c1a814e0af2f4d197827fd9caa1e9636276e064454356141ae1347--
	*/
	const ContentLengthMultipartBoundary = 63                                             // multipart boundary line including newlines
	const ContentLengthMultipartBoundaryTrailingLine = ContentLengthMultipartBoundary + 2 // multipart boundary line plus `--`

	const ContentLengthFormData = 46 // Content-Disposition: form-data; name="..."  (including newlines)

	const ContentLengthFormDataFile = 100
	// Content-Disposition: form-data; name="file"; filename="sourcecode.zip"
	// Content-Type: application/octet-stream
	// (including newlines)

	contentLength = 0
	// multipart form-data parameter list
	for key, val := range params {
		contentLength += ContentLengthMultipartBoundary
		contentLength += ContentLengthFormData
		contentLength += int64(len(key))
		contentLength += int64(len(val))
	}

	// multipart .zip file part
	contentLength += ContentLengthMultipartBoundary
	contentLength += ContentLengthFormDataFile
	contentLength += int64(len(paramName))
	contentLength += int64(len(filepath.Base(filename)))
	contentLength += filesize

	// multipart trailing line
	contentLength += ContentLengthMultipartBoundaryTrailingLine

	return contentLength
}
