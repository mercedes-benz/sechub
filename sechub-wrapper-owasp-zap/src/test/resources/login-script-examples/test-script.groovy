// SPDX-License-Identifier: MIT

import static com.mercedesbenz.sechub.zapwrapper.scan.login.ZapScriptBindingKeys.*
import com.mercedesbenz.sechub.zapwrapper.util.TOTPGenerator

import org.slf4j.Logger
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.WebDriverWait
import org.openqa.selenium.JavascriptExecutor

import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration


// This is just an example how variables can be used (no compile failure)
// for a dedicated and more "real live" example look into "example-script.groovy"
final Logger logger = binding.getVariable(LOGGER)
final WebDriver webdriver = binding.getVariable(WEBDRIVER)
final WebDriverWait webdriverWait = binding.getVariable(WEBDRIVER_WAIT)
final JavascriptExecutor javaScriptExecutor = binding.getVariable(JAVASCRIPT_EXECUTOR)
final SecHubWebScanConfiguration sechubWebScanConfig = binding.getVariable(SECHUB_WEBSCAN_CONFIG)
final TOTPGenerator totpGenerator = binding.getVariable(TOTP_GENERATOR)

final String user = binding.getVariable("custom-username")
final String password = binding.getVariable("custom-password")
final String loginUrl = binding.getVariable(LOGIN_URL)
final String targetUrl = binding.getVariable(TARGET_URL)

// Implementation here does only log info - but enough for unit tests...
logger.info("- test works with user:{} to login at:{}", user, loginUrl)