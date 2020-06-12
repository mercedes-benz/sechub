// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action;

import java.awt.event.ActionEvent;
import java.util.Optional;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.developertools.JSONDeveloperHelper;
import com.daimler.sechub.developertools.admin.ErrorHandler;
import com.daimler.sechub.developertools.admin.ui.ConfigurationSetup;
import com.daimler.sechub.developertools.admin.ui.OutputUI;
import com.daimler.sechub.developertools.admin.ui.ThreeButtonDialogResult;
import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.cache.InputCache;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public abstract class AbstractUIAction extends AbstractAction {

    private JSONDeveloperHelper jsonHelper = JSONDeveloperHelper.INSTANCE;

    private static final long serialVersionUID = 1L;
    private static InputCache inputCache = new InputCache();
    static {
        initializeDefaults();
    }

    private static final Logger LOG = LoggerFactory.getLogger(AbstractUIAction.class);

    private transient UIContext context;

    public AbstractUIAction(String text, UIContext context) {
        this.context = context;
        this.putValue(Action.NAME, text);
    }

    private static void initializeDefaults() {
        /* @formatter:off */
        inputCache.set(InputCacheIdentifier.EMAILADRESS, "sechub@example.org");
        inputCache.set(InputCacheIdentifier.PROJECT_MOCK_CONFIG_JSON,
                "{ \n" + "  \"apiVersion\" : \"1.0\",\n" + "\n" + "   \"codeScan\" : {\n" + "         \"result\" : \"yellow\"   \n" + "   },\n"
                        + "   \"webScan\" : {\n" + "         \"result\" : \"green\"   \n" + "   },\n" + "   \"infraScan\" : {\n"
                        + "         \"result\" : \"red\"   \n" + "   }\n" + " \n" + "}");
        inputCache.set(InputCacheIdentifier.MARK_PROJECT_FALSE_POSITIVE, "{\n" + 
                "    \"apiVersion\": \"1.0\", \n" + 
                "    \"type\" : \"falsePositiveJobDataList\", \n" + 
                "    \n" + 
                "    \"jobData\": [\n" + 
                "            {\n" + 
                "                \"jobUUID\": \"$JobUUID\",\n" + 
                "                \"findingId\": 42, \n" + 
                "                \"comment\" : \"Can be ignored, because:\" \n" + 
                "            },\n" + 
                "            {\n" + 
                "                \"jobUUID\": \"$JobUUID\",\n" + 
                "                \"findingId\": 4711\n" + 
                "            }\n" + 
                "      ]\n" + 
                "                \n" + 
                "}");
        /* @formatter:on */
    }

    protected UIContext getContext() {
        return context;
    }
    
    /**
     * SecHub uses always lower cased identifier - this method is a helper.
     * @param id
     * @return lower cased trimmed id - or empty string when given id is null
     */
    protected String asSecHubId(String id) {
        if (id==null || id.isEmpty()) {
            return ""; 
        }
        return id.toLowerCase().trim();
    }

    @Override
    public final void actionPerformed(ActionEvent event) {
        String actionName = getClass().getSimpleName() + "-" + System.currentTimeMillis();
        String threadName = "action-" + actionName;
        Thread t = new Thread(() -> {
            LOG.info("start action {}", actionName);
            context.getCommandUI().startActionProgress(actionName);

            getErrorHandler().resetErrors();

            output("");
            output("[EXECUTE] " + actionName);

            safeExecute(event, actionName);

            context.getCommandUI().stopActionProgress(actionName);

            if (getErrorHandler().hasErrors()) {
                output("[FAILED]");
            }else {
                output("[SUCCESS]");
            }

            LOG.info("action {} terminated", actionName);

        }, threadName);

        t.start();
    }

    ErrorHandler getErrorHandler() {
        return getContext().getErrorHandler();
    }

    public String getName() {
        return (String) this.getValue(Action.NAME);
    }

    protected void outputAsBeautifiedJSONOnSuccess(String text) {
        outputAsTextOnSuccess(jsonHelper.beatuifyJSON(text));
    }

    protected void outputAsBeautifiedJSONEven(String text) {
        output(jsonHelper.beatuifyJSON(text));
    }

    /**
     * Output given text - but only when no errors occurred on action call
     * 
     * @param text
     */
    protected void outputAsTextOnSuccess(String text) {
        if (getErrorHandler().hasErrors()) {
            return;
        }
        output(text);
    }

    /**
     * Output given text - no matter of an error has happend or not
     * 
     * @param text
     */
    void output(String text) {

        OutputUI outputUI = getContext().getOutputUI();
        outputUI.output(text);
    }

    private void safeExecute(ActionEvent event, String executionDescription) {
        try {
            execute(event);
        } catch (Exception e) {
            context.getOutputUI().error("Execution of " + executionDescription + " failed", e);
        }
    }

    /**
     * Shows an input dialog for user (one liner). Last entered values for given
     * identifier will be shown
     * 
     * @param message
     * @param identifier
     * @return
     */
    protected Optional<String> getUserInput(String message, InputCacheIdentifier identifier) {

        Optional<String> x = getContext().getDialogUI().getUserInput(message, inputCache.get(identifier));
        if (x.isPresent() && identifier != null) {
            inputCache.set(identifier, x.get());
        }
        return x;
    }

    /**
     * Shows an input dialog for user (multi line). Last entered values for given
     * identifier will be shown
     * 
     * @param message
     * @param identifier
     * @return
     */
    protected Optional<String> getUserInputFromTextArea(String message, InputCacheIdentifier identifier) {

        Optional<String> x = getContext().getDialogUI().getUserInputFromTextArea(message, inputCache.get(identifier));
        if (x.isPresent() && identifier != null) {
            inputCache.set(identifier, x.get());
        }
        return x;
    }
    
    protected ThreeButtonDialogResult<String> getUserInputFromField(String inputLabelText) {
    	return getContext().getDialogUI().getUserInputFromField(inputLabelText);
    }

    /**
     * Maybe we have some actions where override shall not be possible - if so override the method
     * and return false
     * @return <code>true</code> (default) when this action confirmations can be disabled by system property
     */
    protected boolean canConfirmationBeOverridenBySetup() {
        return true;
    }
    
    protected boolean confirm(String message) {
        if (canConfirmationBeOverridenBySetup() && ConfigurationSetup.isConfirmationDisabled()) {
            return true;
        }
        return getContext().getDialogUI().confirm(message);
    }

    protected void warn(String message) {
        getContext().getDialogUI().warn(message);
    }

    /**
     * Executes a SecHub action and results are shown in developer admin UI
     * 
     * @param e
     * @throws Exception
     */
    protected abstract void execute(ActionEvent e) throws Exception;
}
