// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.ui.action;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.developertools.JSONDeveloperHelper;
import com.mercedesbenz.sechub.developertools.admin.DeveloperAdministration;
import com.mercedesbenz.sechub.developertools.admin.ErrorHandler;
import com.mercedesbenz.sechub.developertools.admin.ui.ConfigurationSetup;
import com.mercedesbenz.sechub.developertools.admin.ui.OutputUI;
import com.mercedesbenz.sechub.developertools.admin.ui.ThreeButtonDialogResult;
import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.developertools.admin.ui.cache.InputCache;
import com.mercedesbenz.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public abstract class AbstractUIAction extends AbstractAction {

    private JSONDeveloperHelper jsonHelper = JSONDeveloperHelper.INSTANCE;

    private static final long serialVersionUID = 1L;
    private static InputCache inputCache = InputCache.DEFAULT;

    private static final Logger LOG = LoggerFactory.getLogger(AbstractUIAction.class);

    private transient UIContext context;

    public AbstractUIAction(String text, UIContext context) {
        this.context = context;
        this.putValue(Action.NAME, text);
    }

    public AbstractUIAction tooltip(String text) {
        this.putValue(Action.SHORT_DESCRIPTION, text);// tooltip text
        return this;
    }

    public AbstractUIAction tooltipUseText() {
        return tooltip((String) this.getValue(Action.NAME));
    }

    protected void setIcon(URL url) {
        Icon icon = new ImageIcon(url);
        putValue(Action.LARGE_ICON_KEY, icon);
        putValue(Action.SMALL_ICON, icon);
    }

    protected UIContext getContext() {
        return context;
    }

    protected DeveloperAdministration getAdministration() {
        return getContext().getAdministration();
    }

    /**
     * SecHub uses always lower cased identifier - this method is a helper.
     *
     * @param id
     * @return lower cased trimmed id - or empty string when given id is null
     */
    protected String asSecHubId(String id) {
        if (id == null || id.isEmpty()) {
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
            } else {
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
     * Output given text - no matter of an error has happened or not
     *
     * @param text
     */
    protected void output(String text) {

        OutputUI outputUI = getContext().getOutputUI();
        outputUI.output(text);
    }

    /**
     * Output given text - no matter of an error has happened or not
     *
     * @param text
     */
    protected void error(String text) {

        OutputUI outputUI = getContext().getOutputUI();
        outputUI.error(text);
    }

    private void safeExecute(ActionEvent event, String executionDescription) {
        try {
            execute(event);
        } catch (Throwable t) {
            context.getOutputUI().error("Execution of " + executionDescription + " failed", t);
        }
    }

    /**
     * Shows an input dialog for user (one liner). Default values for given
     * identifier will be shown - and always be reused. NO caching!
     *
     * @param message
     * @param identifier
     * @return
     */
    protected Optional<String> getUserInput(String message, String defaultValue) {
        Optional<String> x = getContext().getDialogUI().getUserInput(message, defaultValue);
        return x;
    }

    /**
     * Shows an input dialog for user (one liner).
     *
     * @param message
     * @return
     */
    protected Optional<String> getUserInput(String message) {
        return getUserInput(message, (InputCacheIdentifier) null);
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
     * Shows an password dialog for user (one liner). Last entered values for given
     * identifier will be used when nothing entered
     *
     * @param message
     * @param identifier
     * @return
     */
    protected Optional<String> getUserPassword(String message, InputCacheIdentifier identifier) {

        Optional<String> x = getContext().getDialogUI().getUserPassword(message, inputCache.get(identifier));
        if (x.isPresent() && identifier != null) {
            inputCache.set(identifier, x.get());
        }
        return x;
    }

    /**
     * Shows an input dialog for selecting one of given types
     *
     * @param title
     * @param text
     * @return
     */
    protected <T> Optional<T> getUserInputFromCombobox(String title, T initialValue, String message, @SuppressWarnings({ "unchecked" }) T... identifier) {
        return getContext().getDialogUI().getUserInputFromCombobox(message, title, Arrays.asList(identifier), initialValue);
    }

    /**
     * Shows an input dialog for user (multi line) and sets given text as content
     *
     * @param title
     * @param text
     * @return
     */
    protected Optional<String> getUserInputFromTextArea(String title, String text) {
        Optional<String> x = getContext().getDialogUI().getUserInputFromTextArea(title, text);
        return x;
    }

    /**
     * Shows an input dialog for user (multi line). Last entered values for given
     * identifier will be shown
     *
     * @param title
     * @param identifier
     * @return
     */
    protected Optional<String> getUserInputFromTextArea(String title, InputCacheIdentifier identifier) {

        Optional<String> x = getContext().getDialogUI().getUserInputFromTextArea(title, inputCache.get(identifier));
        if (x.isPresent() && identifier != null) {
            inputCache.set(identifier, x.get());
        }
        return x;
    }

    protected ThreeButtonDialogResult<String> getUserInputFromField(String inputLabelText) {
        return getContext().getDialogUI().getUserInputFromField(inputLabelText);
    }

    /**
     * Maybe we have some actions where override shall not be possible - if so
     * override the method and return false
     *
     * @return <code>true</code> (default) when this action confirmations can be
     *         disabled by system property
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
