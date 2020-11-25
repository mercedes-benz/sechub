// SPDX-License-Identifier: MIT

package cli

import (
	"fmt"
	"net/http"

	sechubUtil "daimler.com/sechub/util"
)

// HandleHTTPErrorAndResponse does just handle error and repsonse
func HandleHTTPErrorAndResponse(res *http.Response, err error, context *Context) {
	sechubUtil.LogDebug(context.config.debug, fmt.Sprintf("HTTP response: %+v", res))
	sechubUtil.HandleHTTPError(err, ExitCodeHTTPError)
	sechubUtil.HandleHTTPResponse(res, ExitCodeHTTPError)
}
