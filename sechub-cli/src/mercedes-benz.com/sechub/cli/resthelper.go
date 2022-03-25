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
	/* we use inputForContentProcessing - means origin content, unfilled, prevents password leak in logs */
	sechubUtil.LogDebug(context.config.debug, fmt.Sprintf("Sending %s:%s\n Headers: %s\n Origin-Content: %q", method, url, header, context.inputForContentProcessing))

	/* prepare */
	request, err1 := http.NewRequest(method, url, bytes.NewBuffer(context.contentToSend)) // we use "contentToSend" and not "inputForContentProcessing" !
	sechubUtil.HandleHTTPError(err1, ExitCodeHTTPError)
	request.SetBasicAuth(context.config.user, context.config.apiToken)

	for key := range header {
		request.Header.Set(key, header[key])
	}

	/* send */
	return handleHTTPRequestAndResponse(context, request)
}

// handleHTTPRequestAndResponse - run http request and handle the response in a resilient way
func handleHTTPRequestAndResponse(context *Context, request *http.Request) *http.Response {
	// HTTP call
	response, err := context.HTTPClient.Do(request)    // e.g. http.Post(createJobURL, "application/json", bytes.NewBuffer(context.contentToSend))
	sechubUtil.HandleHTTPError(err, ExitCodeHTTPError) // Handle networking errors etc. (exit)

	// Resilience handling
	if response.StatusCode > 403 { // StatusCode is in 4xx(400-403 are unrecoverable) or 5xx
		sechubUtil.LogWarning(
			fmt.Sprintf("Received unexpected Status Code %d (%s) from server. Retrying in %d seconds...",
				response.StatusCode, response.Status, context.config.waitSeconds))

		for retry := 1; retry <= HTTPMaxRetries; retry++ {
			time.Sleep(time.Duration(context.config.waitSeconds) * time.Second)

			sechubUtil.Log(fmt.Sprintf("          retry %d/%d", retry, HTTPMaxRetries), false)
			response, err = context.HTTPClient.Do(request) // retry HTTP call
			sechubUtil.HandleHTTPError(err, ExitCodeHTTPError)

			if response.StatusCode < 400 {
				break // exit loop on success (1xx, 2xx or 3xx StatusCode)
			}
		}
	}

	sechubUtil.LogDebug(context.config.debug, fmt.Sprintf("HTTP response: %+v", response))
	sechubUtil.HandleHTTPResponse(response, ExitCodeHTTPError) // Will exit if we still got a 4xx or 5xx StatusCode

	return response
}

// Creates a new file upload http request with optional extra params (low memory footprint regardless the file size)
func newFileUploadRequestViaPipe(uploadToURL string, params map[string]string, paramName, zipfilename string) (*http.Request, error) {
	r, w := io.Pipe()
	m := multipart.NewWriter(w)

	go func() {
		defer w.Close()

		for key, val := range params {
			_ = m.WriteField(key, val)
		}

		part, err := m.CreateFormFile(paramName, filepath.Base(zipfilename))
		if err != nil {
			return
		}

		file, err := os.Open(zipfilename)
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
	// request.Header.Set("Content-Length", "1641")
	// fmt.Println(request.Header)

	// Set HTTP protocol 1.1 and ContentLength to -1 will create a `transfer-encoding: chunked`.
	// This avoids warnings about missing content length on server side.
	// request.ContentLength = -1

	request.ContentLength = computeContentLengthOfFileUpload(params, paramName, zipfilename)
	fmt.Printf("Computed Content-Length = %d\n", request.ContentLength)

	buf := make([]byte, 1)
	summe := 0

	for err == nil {
		_, err = io.ReadAtLeast(r, buf, 1)
		if err == io.EOF {
			break
		}
		// fmt.Print(numberOfBytes)
		// fmt.Print(" gelesen. Zeichen =")
		// fmt.Println(string(buf))
		summe++
	}
	fmt.Printf("    Real Content-Length = %d\n", summe)
	// TODO: Write a test to check if computed size equals real stream size

	return request, err
}

func computeContentLengthOfFileUpload(params map[string]string, paramName, zipfilename string) (contentLength int64) {
	/* Real world example of multipart content sent to SecHub server when uploading:
	--f76dd0c1a814e0af2f4d197827fd9caa1e9636276e064454356141ae1347
	Content-Disposition: form-data; name="title"

	Sourcecode zipped
	--f76dd0c1a814e0af2f4d197827fd9caa1e9636276e064454356141ae1347
	Content-Disposition: form-data; name="author"

	Sechub client 0.0.0-285d1b6-dirty-20220324161639
	--f76dd0c1a814e0af2f4d197827fd9caa1e9636276e064454356141ae1347
	Content-Disposition: form-data; name="checkSum"

	ccdcf7c07a8461f8aeb44f6bbd2166d184c79f7acfd86cb3415dcb452f274a63
	--f76dd0c1a814e0af2f4d197827fd9caa1e9636276e064454356141ae1347
	Content-Disposition: form-data; name="file"; filename="sourcecode-testproject.zip"
	Content-Type: application/octet-stream

	<binary content of zip file here>
	--f76dd0c1a814e0af2f4d197827fd9caa1e9636276e064454356141ae1347--
	*/
	const ContentLengthMultipartBoundary = 63                                             // multipart boundary line including \n
	const ContentLengthMultipartBoundaryTrailingLine = ContentLengthMultipartBoundary + 2 // multipart boundary line plus `--`

	const ContentLengthFormData = 42 // Content-Disposition: form-data; name="..."  (including \n)

	const ContentLengthFormDataZipFile = 92
	// Content-Disposition: form-data; name="file"; filename="sourcecode.zip"
	// Content-Type: application/octet-stream
	// (including multiple \n)

	contentLength = 0
	for key, val := range params {
		contentLength += ContentLengthMultipartBoundary
		contentLength += ContentLengthFormData
		contentLength += int64(len(key))
		contentLength += int64(len(val))
	}

	// uploaded .zip file part
	contentLength += ContentLengthMultipartBoundary
	contentLength += ContentLengthFormDataZipFile
	contentLength += int64(len(paramName))
	contentLength += int64(len(filepath.Base(zipfilename)))

	// TODO: read size of zip file from disk (util/filehelpers?)

	// multipart trailing line
	contentLength += ContentLengthMultipartBoundaryTrailingLine

	return contentLength
}
