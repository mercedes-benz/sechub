
import static com.mercedesbenz.sechub.zapwrapper.scan.login.ZapScriptBindingKeys.*

import com.mercedesbenz.sechub.zapwrapper.util.TOTPGenerator

import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.WebDriverWait
import org.openqa.selenium.JavascriptExecutor

import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration


final FirefoxDriver firefox = binding.getVariable(FIREFOX_WEBDRIVER_KEY)
final WebDriverWait webdriverWait = binding.getVariable(FIREFOX_WEBDRIVER_WAIT_KEY)
final JavascriptExecutor javaScriptExecutor = binding.getVariable(JAVASCRIPTEXECUTOR_KEY)
final SecHubWebScanConfiguration sechubWebScanConfig = binding.getVariable(SECHUB_WEBSCAN_CONFIG_KEY)
final TOTPGenerator totpGenerator = binding.getVariable(TOTP_GENERATOR_KEY)

final String user = binding.getVariable(USER_KEY)
final String password = binding.getVariable(PASSWORD_KEY)
final String loginUrl = binding.getVariable(LOGIN_URL_KEY)
final String targetUrl = binding.getVariable(TARGET_URL_KEY)


invalid-code
