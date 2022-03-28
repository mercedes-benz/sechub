// SPDX-License-Identifier: MIT

package cli

import (
	"fmt"
	"os"
	"testing"

	sechubTestUtil "mercedes-benz.com/sechub/testutil"
)

func Test_computeContentLengthOfFileUpload(t *testing.T) {
	// PREPRARE
	context := new(Context)
	config := new(Config)
	context.config = config

	extraParams := map[string]string{
		"title":    "Sourcecode zipped",
		"author":   "Sechub client " + Version(),
		"checkSum": context.sourceZipFileChecksum,
	}

	dir := sechubTestUtil.InitializeTestTempDir(t)
	testfile := dir + "/testfile.zip"
	defer os.RemoveAll(dir)
	content := []byte("This is a fake zip file.\n")
	sechubTestUtil.CreateTestFile(testfile, 0644, content, t)

	// EXECUTE
	request, _ := newFileUploadRequestViaPipe(buildUploadSourceCodeAPICall(context), extraParams, "file", testfile)
	realContentLength := sechubTestUtil.CountBytesInStream(request.Body)
	// TEST
	fmt.Printf("Real content length: %d bytes\n", realContentLength)
	fmt.Printf("Computed content length: %d bytes\n", request.ContentLength)
	sechubTestUtil.AssertEquals(realContentLength, request.ContentLength, t)
}
