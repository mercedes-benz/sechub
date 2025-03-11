import static com.mercedesbenz.sechub.zapwrapper.scan.login.ZapScriptBindingKeys.*

import org.slf4j.Logger
import com.mercedesbenz.sechub.zapwrapper.util.TOTPGenerator
import com.mercedesbenz.sechub.zapwrapper.scan.login.UserInfoScriptException

import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.WebDriverWait
import org.openqa.selenium.JavascriptExecutor

import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration

// all available bindings
final Logger logger = binding.getVariable(LOGGER)
final WebDriver webdriver = binding.getVariable(WEBDRIVER)
final WebDriverWait webdriverWait = binding.getVariable(WEBDRIVER_WAIT)
final JavascriptExecutor javaScriptExecutor = binding.getVariable(JAVASCRIPT_EXECUTOR)
final SecHubWebScanConfiguration sechubWebScanConfig = binding.getVariable(SECHUB_WEBSCAN_CONFIG)
final TOTPGenerator totpGenerator = binding.getVariable(TOTP_GENERATOR)

final String user = binding.getVariable("username");
final String password = binding.getVariable("password")
final String loginUrl = binding.getVariable(LOGIN_URL)
final String targetUrl = binding.getVariable(TARGET_URL)

logger.info("Start")
// This is an example on how to use custom exceptions inside authentication scripts
// Exceptions of the type UserInfoScriptException are a channel back to the caller of the script,
// since they are caught by the ZAP wrapper executing this script.
// The message of the exception is sent in a SecHubMessage back to the client that pulls the results of the SecHub job.
if (totpGenerator == null) {
    throw new UserInfoScriptException("The authentication requires TOTP but no TOTP generator is available in bindings. Normally this means that no TOTP configuration is defined inside the SecHub configuration");
}

logger.info("- webdriver loads: {}",loginUrl)

// example authentication script steps
webdriver.get(loginUrl)

logger.info("- start condition checks")

webdriverWait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".close-dialog"))).click()
webdriverWait.until(ExpectedConditions.presenceOfElementLocated(By.id("email"))).sendKeys(user)
webdriverWait.until(ExpectedConditions.presenceOfElementLocated(By.id("password"))).sendKeys(password)
webdriverWait.until(ExpectedConditions.elementToBeClickable(By.id("loginButton"))).click()
webdriverWait.until(ExpectedConditions.presenceOfElementLocated(By.id("otp"))).sendKeys(totpGenerator.now())
webdriverWait.until(ExpectedConditions.elementToBeClickable(By.id("submitOtp"))).click()

logger.info("- done")