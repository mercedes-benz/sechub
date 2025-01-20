// SPDX-License-Identifier: MIT

import com.mercedesbenz.sechub.zapwrapper.scan.login.UserInfoScriptException

throw new UserInfoScriptException("The authentication requires TOTP but no TOTP configuration was found inside the SecHub configuration!");
