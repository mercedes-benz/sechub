// SPDX-License-Identifier: MIT

package cli

import (
	"fmt"
)

// https://localhost:8443/api/project/testproject/job/
func buildCreateNewSecHubJobAPICall(context *Context) string {

	apiPart := fmt.Sprintf("project/%s/job", context.config.projectID)
	return buildAPIUrl(&context.config.server, &apiPart)
}

// https://localhost:8443/api/project/testproject/job/e21b13fc-591e-4abd-b119-755d473c5625/approve
func buildApproveSecHubJobAPICall(context *Context) string {
	context.contentToSend = nil // Do not send content
	context.inputForContentProcessing = nil
	apiPart := fmt.Sprintf("project/%s/job/%s/approve", context.config.projectID, context.config.secHubJobUUID)
	return buildAPIUrl(&context.config.server, &apiPart)
}

// https://localhost:8443/api/project/testproject/job/e21b13fc-591e-4abd-b119-755d473c5625/binaries
func buildUploadBinariesAPICall(context *Context) string {
	apiPart := fmt.Sprintf("project/%s/job/%s/binaries", context.config.projectID, context.config.secHubJobUUID)
	return buildAPIUrl(&context.config.server, &apiPart)
}

// https://localhost:8443/api/project/testproject/job/e21b13fc-591e-4abd-b119-755d473c5625/sourcecode
func buildUploadSourceCodeAPICall(context *Context) string {
	apiPart := fmt.Sprintf("project/%s/job/%s/sourcecode", context.config.projectID, context.config.secHubJobUUID)
	return buildAPIUrl(&context.config.server, &apiPart)
}

// https://localhost:8443/api/project/testproject/job/e21b13fc-591e-4abd-b119-755d473c5625
func buildGetSecHubJobStatusAPICall(context *Context) string {
	context.contentToSend = nil // Do not send content
	context.inputForContentProcessing = nil
	apiPart := fmt.Sprintf("project/%s/job/%s", context.config.projectID, context.config.secHubJobUUID)
	return buildAPIUrl(&context.config.server, &apiPart)
}

// https://localhost:8443/api/project/testproject/report/e21b13fc-591e-4abd-b119-755d473c5625
func buildGetSecHubJobReportAPICall(context *Context) string {
	context.contentToSend = nil // Do not send content
	context.inputForContentProcessing = nil

	apiPart := fmt.Sprintf("project/%s/report/%s", context.config.projectID, context.config.secHubJobUUID)

	if context.config.reportFormat == ReportFormatSPDXJSON {
		apiPart = fmt.Sprintf("project/%s/report/spdx/%s", context.config.projectID, context.config.secHubJobUUID)
	}

	return buildAPIUrl(&context.config.server, &apiPart)
}

// https://sechub.example.com/api/project/project1/jobs?size=10&page=0
func buildGetSecHubJobListAPICall(context *Context, size int) string {
	context.contentToSend = nil // Do not send content
	context.inputForContentProcessing = nil
	apiPart := fmt.Sprintf("project/%s/jobs?size=%d&page=0", context.config.projectID, size)
	return buildAPIUrl(&context.config.server, &apiPart)
}

// https://localhost:8081/api/project/testproject/false-positives
func buildFalsePositivesAPICall(context *Context) string {
	apiPart := fmt.Sprintf("project/%s/false-positives", context.config.projectID)
	return buildAPIUrl(&context.config.server, &apiPart)
}

// https://localhost:8081/api/project/testproject/false-positive
func buildFalsePositiveAPICall(context *Context) string {
	apiPart := fmt.Sprintf("project/%s/false-positive", context.config.projectID)
	return buildAPIUrl(&context.config.server, &apiPart)
}

func buildAPIUrl(server *string, apiPart *string) string {
	return fmt.Sprintf("%s/api/%s", *server, *apiPart)
}
