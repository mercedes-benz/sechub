// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action;

import java.awt.event.ActionEvent;
import java.util.Optional;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.developertools.JSONDeveloperHelper;
import com.daimler.sechub.developertools.admin.ui.OutputUI;
import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.cache.InputCache;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public abstract class AbstractUIAction extends AbstractAction{

	private JSONDeveloperHelper jsonHelper = JSONDeveloperHelper.INSTANCE;

	private static final long serialVersionUID = 1L;
	private static InputCache inputCache = new InputCache();
	static {
		inputCache.set(InputCacheIdentifier.EMAILADRESS, "sechub@example.org");
	}


	private static final Logger LOG = LoggerFactory.getLogger(AbstractUIAction.class);


	private transient UIContext context;

	public AbstractUIAction(String text, UIContext context) {
		this.context=context;
		this.putValue(Action.NAME, text);
	}

	protected UIContext getContext() {
		return context;
	}

	@Override
	public final void actionPerformed(ActionEvent event) {
		String actionName = getClass().getSimpleName()+"-"+System.currentTimeMillis();
		String threadName = "action-"+actionName;
		Thread t = new Thread(()-> {
			LOG.info("start action {}",actionName);
			context.getCommandUI().startActionProgress(actionName);


			outputAsText("");
			outputAsText("[EXECUTE] "+actionName);

			safeExecute(event, actionName);

			context.getCommandUI().stopActionProgress(actionName);
			outputAsText("[DONE]");
			LOG.info("done action {}",actionName);

		},threadName);

		t.start();
	}

	public String getName() {
		return (String) this.getValue(Action.NAME);
	}

	protected void outputAsBeautifiedJSON(String text) {
		outputAsText(jsonHelper.beatuifyJSON(text));
	}

	protected void outputAsText(String text) {

		OutputUI outputUI = getContext().getOutputUI();
		outputUI.output(text);
	}

	private void safeExecute(ActionEvent event, String executionDescription) {
		try {
			execute(event);
		}catch(Exception e) {
			context.getOutputUI().error("Execution of "+executionDescription+" failed",e);
		}
	}

	/**
	 * Shows an input dialog for user (one liner). Last entered values for given idenifier will be shown
	 * @param message
	 * @param identifier
	 * @return
	 */
	protected Optional<String> getUserInput(String message, InputCacheIdentifier identifier) {

		Optional<String> x = getContext().getDialogUI().getUserInput(message,inputCache.get(identifier));
		if (x.isPresent() && identifier!=null) {
			inputCache.set(identifier, x.get());
		}
		return x;
	}
	
	/**
	 * Shows an input dialog for user (multi line). Last entered values for given idenifier will be shown
	 * @param message
	 * @param identifier
	 * @return
	 */
	protected Optional<String> getUserInputFromTextArea(String message, InputCacheIdentifier identifier) {

		Optional<String> x = getContext().getDialogUI().getUserInputFromTextArea(message,inputCache.get(identifier));
		if (x.isPresent() && identifier!=null) {
			inputCache.set(identifier, x.get());
		}
		return x;
	}

	protected boolean confirm(String message) {
		return getContext().getDialogUI().confirm(message);
	}



	protected void warn(String message) {
		getContext().getDialogUI().warn(message);
	}

	protected abstract void execute(ActionEvent e) throws Exception;
}
