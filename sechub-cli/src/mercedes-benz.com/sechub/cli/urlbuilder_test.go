// SPDX-License-Identifier: MIT

package cli

import (
	"fmt"
	"testing"

	sechubTestUtil "mercedes-benz.com/sechub/testutil"
)

// https://localhost:8443/api/project/testproject/job/
func TestBuildCreateNewSecHubJobAPICall(t *testing.T) {
	/* prepare */
	context := new(Context)
	config := new(Config)

	context.config = config
	config.projectID = "testproject"
	config.server = "https://localhost:8443"

	/* execute */
	result := buildCreateNewSecHubJobAPICall(context)

	/* test*/
	sechubTestUtil.AssertEquals("https://localhost:8443/api/project/testproject/job", result, t)
}

// https://localhost:8443/api/project/testproject/job/e21b13fc-591e-4abd-b119-755d473c5625/approve
func Example_buildApproveSecHubJobAPICall() {
	// PREPRARE
	context := new(Context)
	config := new(Config)

	context.config = config
	config.projectID = "testproject"
	config.server = "https://localhost:8443"
	config.secHubJobUUID = "e21b13fc-591e-4abd-b119-755d473c5625"

	// EXECUTE
	result := buildApproveSecHubJobAPICall(context)

	// TEST
	fmt.Println(result)
	// Output:
	// https://localhost:8443/api/project/testproject/job/e21b13fc-591e-4abd-b119-755d473c5625/approve
}

// https://localhost:8443/api/project/testproject/job/e21b13fc-591e-4abd-b119-755d473c5625
func TestBuildGetSecHubJobStatusAPICall(t *testing.T) {
	/* prepare */
	context := new(Context)
	config := new(Config)

	context.config = config
	config.projectID = "testproject"
	config.server = "https://localhost:8443"

	config.secHubJobUUID = "e21b13fc-591e-4abd-b119-755d473c5625"

	/* execute */
	result := buildGetSecHubJobStatusAPICall(context)

	/* test*/
	sechubTestUtil.AssertEquals("https://localhost:8443/api/project/testproject/job/e21b13fc-591e-4abd-b119-755d473c5625", result, t)
}

// https://localhost:8443/api/project/testproject/report/e21b13fc-591e-4abd-b119-755d473c5625
func TestBuildGetSecHubJobReportAPICall(t *testing.T) {
	/* prepare */
	context := new(Context)
	config := new(Config)

	context.config = config
	config.projectID = "testproject"
	config.server = "https://localhost:8443"

	config.secHubJobUUID = "e21b13fc-591e-4abd-b119-755d473c5625"

	/* execute */
	result := buildGetSecHubJobReportAPICall(context)

	/* test*/
	sechubTestUtil.AssertEquals("https://localhost:8443/api/project/testproject/report/e21b13fc-591e-4abd-b119-755d473c5625", result, t)
}

// https://localhost:8443/api/project/testproject/job/e21b13fc-591e-4abd-b119-755d473c5625/sourcecode
func TestBuildPostSecHubUploadSourceCodeAPICall(t *testing.T) {
	/* prepare */
	context := new(Context)
	config := new(Config)

	context.config = config
	config.projectID = "testproject"
	config.server = "https://localhost:8443"

	config.secHubJobUUID = "e21b13fc-591e-4abd-b119-755d473c5625"

	/* execute */
	result := buildUploadSourceCodeAPICall(context)

	/* test*/
	sechubTestUtil.AssertEquals("https://localhost:8443/api/project/testproject/job/e21b13fc-591e-4abd-b119-755d473c5625/sourcecode", result, t)
}

// https://localhost:8443/api/project/testproject/job/e21b13fc-591e-4abd-b119-755d473c5625/binaries
func Example_buildUploadBinariesAPICall() {
	/* prepare */
	context := new(Context)
	config := new(Config)

	context.config = config
	config.projectID = "testproject"
	config.server = "https://localhost:8443"

	config.secHubJobUUID = "e21b13fc-591e-4abd-b119-755d473c5625"

	/* execute */
	result := buildUploadBinariesAPICall(context)

	/* test*/
	fmt.Println(result)
	// Output:
	// https://localhost:8443/api/project/testproject/job/e21b13fc-591e-4abd-b119-755d473c5625/binaries
}

// https://localhost:8081/api/project/testproject/false-positive
func Example_buildFalsePositivesAPICalls() {
	// PREPARE
	context := new(Context)
	config := new(Config)

	context.config = config
	config.projectID = "testproject"
	config.server = "https://localhost:8443"

	// TEST
	result1 := buildFalsePositivesAPICall(context)
	result2 := buildFalsePositiveAPICall(context)

	// TEST
	fmt.Println(result1)
	fmt.Println(result2)
	// Output:
	// https://localhost:8443/api/project/testproject/false-positives
	// https://localhost:8443/api/project/testproject/false-positive
}

func Example_buildGetSecHubJobReportAPICall_SPDX_JSON() {
	/* prepare */
	context := new(Context)
	config := new(Config)

	context.config = config
	config.projectID = "testproject"
	config.server = "https://localhost:8443"

	config.secHubJobUUID = "e21b13fc-591e-4abd-b119-755d473c5625"
	config.reportFormat = ReportFormatSPDXJSON

	/* execute */
	result := buildGetSecHubJobReportAPICall(context)

	/* test*/
	fmt.Println(result)
	// Output:
	// https://localhost:8443/api/project/testproject/report/spdx/e21b13fc-591e-4abd-b119-755d473c5625
}

func Example_buildGetSecHubJobListAPICall() {
	/* prepare */
	context := new(Context)
	config := new(Config)

	context.config = config
	config.projectID = "testproject"
	config.server = "https://localhost:8443"

	/* execute */
	result := buildGetSecHubJobListAPICall(context, 10)

	/* test*/
	fmt.Println(result)
	// Output:
	// https://localhost:8443/api/project/testproject/jobs?size=10&page=0
}

func TestBuildGetSecHubJobListAPICallWithLabels(t *testing.T) {
	/* prepare */
	context := new(Context)
	config := new(Config)

	context.config = config
	config.projectID = "testproject"
	config.server = "https://localhost:8443"

	labels := map[string]string{
		"key1": "value1",
		"key2": "Non alphnumeric character$ !",
	}
	context.config.labels = labels
	

	/* execute */
	result := buildGetSecHubJobListAPICall(context, 10)

	/* test*/
	sechubTestUtil.AssertStringContains(result, "&withMetaData=true", t)
	sechubTestUtil.AssertStringContains(result, "&metadata.labels.key1=value1", t)
	sechubTestUtil.AssertStringContains(result, "&metadata.labels.key2=Non+alphnumeric+character%24+%21", t)
}
