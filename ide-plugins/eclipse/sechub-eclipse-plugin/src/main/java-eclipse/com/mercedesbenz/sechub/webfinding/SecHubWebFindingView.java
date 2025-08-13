// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webfinding;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.part.ViewPart;

import com.mercedesbenz.sechub.api.internal.gen.model.SecHubFinding;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.model.FindingModel;
import com.mercedesbenz.sechub.model.FindingNode;
import com.mercedesbenz.sechub.util.BrowserUtil;
import com.mercedesbenz.sechub.util.CweLinkTextCreator;

public class SecHubWebFindingView extends ViewPart {

	public static final String ID = "com.mercedesbenz.sechub.views.SecHubWebFindingView";

	private Link linkDescriptionWithLinks;
	private StyledText findingAsJson;
	private StyledText description;
	private StyledText solution;

	private Composite mainComposite;

	@Override
	public void setFocus() {
		findingAsJson.setFocus();
	}

	@Override
	public void createPartControl(Composite parent) {
		Layout layout = parent.getLayout();
		if (layout instanceof FillLayout) {
			FillLayout gl = (FillLayout) layout;
			gl.marginWidth = -5;
		}

		GridLayout mainCompositeLayout = new GridLayout();
		mainCompositeLayout.marginLeft = 0;
		mainCompositeLayout.marginRight = 0;

		GridData mainCompositelayoutData1 = GridDataFactory.fillDefaults().grab(true, true).create();

		mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayoutData(mainCompositelayoutData1);
		mainComposite.setLayout(mainCompositeLayout);

		GridData headlineCompositeLayoutData = GridDataFactory.fillDefaults().grab(true, false).create();
		FillLayout headlineCompositeLayout = new FillLayout();

		Composite headlineComposite = new Composite(mainComposite, SWT.NONE);
		headlineComposite.setLayout(headlineCompositeLayout);
		headlineComposite.setLayoutData(headlineCompositeLayoutData);

		linkDescriptionWithLinks = new Link(headlineComposite, SWT.NONE);
		linkDescriptionWithLinks.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String selectedText = e.text;
				if (selectedText == null) {
					return;
				}
				BrowserUtil.openInExternalBrowser(selectedText);
			}
		});
		linkDescriptionWithLinks.setText("");

		GridData descriptionLabelLayoutData = GridDataFactory.fillDefaults().grab(true, false).create();
		GridData descriptionTextLayoutData = GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 75)
				.create();

		Label descriptionLabel = new Label(mainComposite, SWT.NONE);
		descriptionLabel.setText("Description:");
		descriptionLabel.setLayoutData(descriptionLabelLayoutData);

		description = new StyledText(mainComposite, SWT.WRAP | SWT.V_SCROLL| SWT.H_SCROLL | SWT.READ_ONLY);
		description.setLayoutData(descriptionTextLayoutData);
		description.setAlwaysShowScrollBars(false);

		GridData solutionLabelLayoutData = GridDataFactory.fillDefaults().grab(true, false).create();
		GridData solutionTextLayoutData = GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 75)
				.create();

		Label solutionLabel = new Label(mainComposite, SWT.NONE);
		solutionLabel.setText("Solution:");
		solutionLabel.setLayoutData(solutionLabelLayoutData);

		solution = new StyledText(mainComposite, SWT.WRAP | SWT.V_SCROLL| SWT.H_SCROLL | SWT.READ_ONLY);
		solution.setLayoutData(solutionTextLayoutData);
		solution.setAlwaysShowScrollBars(false);

		GridData jsonLabelLayoutData = GridDataFactory.fillDefaults().grab(true, false).create();
		Label findingAsJsonLabel = new Label(mainComposite, SWT.NONE);
		findingAsJsonLabel.setText("Full json:");
		findingAsJsonLabel.setLayoutData(jsonLabelLayoutData);

		GridData jsonTextLayoutData = GridDataFactory.fillDefaults().grab(true, true).create();
		findingAsJson = new StyledText(mainComposite, SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
		findingAsJson.setLayoutData(jsonTextLayoutData);
		findingAsJson.setAlwaysShowScrollBars(false);

		makeActions();
		contributeToActionBars();

		GridLayoutFactory.fillDefaults().generateLayout(parent);
	}

	private void makeActions() {
	}

	private void contributeToActionBars() {
	}

	public void update(FindingModel model) {
		if (model == null) {
			clearDetails();
			return;
		}

		FindingNode node = model.getFirstFinding();
		if (node == null || node.getFinding() == null) {
			clearDetails();
			return;
		}

		SecHubFinding sechubFinding = node.getFinding();
		String headDescription = CweLinkTextCreator.createCweLinkTextWithInfos(node);
		linkDescriptionWithLinks.setText(headDescription);

		description.setText(sechubFinding.getDescription());
		solution.setText(sechubFinding.getSolution());

		String asJson = JSONConverter.get().toJSON(sechubFinding, true);
		findingAsJson.setText(asJson);

		mainComposite.layout();
	}

	private void clearDetails() {
		findingAsJson.setText("");
		description.setText("");
		solution.setText("");
		linkDescriptionWithLinks.setText("");
	}

	public void importReport() {
	}
}