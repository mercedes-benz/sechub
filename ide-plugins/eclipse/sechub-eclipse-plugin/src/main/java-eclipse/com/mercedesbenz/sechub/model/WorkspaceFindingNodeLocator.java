// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.model;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.ITextEditor;

import com.mercedesbenz.sechub.EclipseUtil;
import com.mercedesbenz.sechub.Logging;
import com.mercedesbenz.sechub.SecHubActivator;
import com.mercedesbenz.sechub.util.SimpleStringUtil;

public class WorkspaceFindingNodeLocator {

	IWorkbench workbench;
	private String META_DATA_CACHE_ID_ECLISE_LOCATION = "metadata.eclipse.location";

	public WorkspaceFindingNodeLocator(IWorkbench workbench) {
		this.workbench = workbench;
	}

	public void searchInProjectsForFindingAndShowInEditor(FindingNode finding) {
		FindingNodeLocatorJob job = new FindingNodeLocatorJob(finding);
		job.setUser(true);
		job.schedule();
	}

	private IStatus searchInProjectsForFindingAndShowInEditorInternal(FindingNode finding, IProgressMonitor monitor) {
		Object eclipseLocation = finding.getCachedMetaData(META_DATA_CACHE_ID_ECLISE_LOCATION);
		String fileLocation = finding.getLocation();

		IFile file = null;
		if (eclipseLocation instanceof IFile) {
			file = (IFile) eclipseLocation;
		}
		int worked = 1;
		boolean foundFile = false;
		if (file == null) {

			if (fileLocation == null) {
				return Status.CANCEL_STATUS;
			}
			FileLocationExplorer explorer = new FileLocationExplorer();

			monitor.beginTask("resolve", 2);
			IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
			IProject[] projects = workspaceRoot.getProjects();
			for (IProject project : projects) {
				explorer.getSearchFolders().add(project.getLocation().toFile().toPath());
			}
			List<Path> result = null;
			try {
				result = explorer.searchFor(fileLocation);
			} catch (IOException e) {
				return new Status(IStatus.ERROR, SecHubActivator.PLUGIN_ID, "Search failed!", e);
			}
			if (result.isEmpty()) {
				MessageDialog.openWarning(getShell(), "File not found", "Was not able to locate " + fileLocation);
				return Status.CANCEL_STATUS;
			}
			monitor.worked(worked++);
			Path path = null;
			if (result.size() > 1) {
				/* multiple results found */

				ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new LabelProvider());
				dialog.setElements(result.toArray());
				dialog.setTitle("Which is the correct target?");

				int resultCode = dialog.open();
				if (resultCode != Window.OK) {
					return Status.CANCEL_STATUS;
				}
				Object[] files = dialog.getResult();
				if (files.length == 0) {
					return Status.CANCEL_STATUS;
				}
				Object f = files[0];
				if (!(f instanceof Path)) {
					return Status.CANCEL_STATUS;
				}
				path = (Path) f;

			} else {
				path = result.get(0);
			}
			file = EclipseUtil.toIFileOrNull(path);
			if (file == null) {
				return new Status(IStatus.ERROR, SecHubActivator.PLUGIN_ID,
						"Was not able to resolve eclipse resource by path:" + path.toString());
			}
			finding.setCachedMetaData(META_DATA_CACHE_ID_ECLISE_LOCATION, file);
		}
		FindingMatchType findingInFile = searchForFindingInFile(file, finding);
		monitor.worked(worked++);

		if (findingInFile != null) {
			foundFile = true;
		}

		if (!foundFile) {
			String message = "The file '" + fileLocation + "' could not be found!";
			MessageDialog.openError(getShell(), "File not found!", message);
			return new Status(IStatus.ERROR, SecHubActivator.PLUGIN_ID, message);
		}
		return Status.OK_STATUS;
	}

	private FindingMatchType searchForFindingInFile(IFile file, FindingNode finding) {

		try {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			IWorkbenchPage page = window.getActivePage();

			// Open file in editor or focus if already open
			IEditorPart editor = IDE.openEditor(page, file);

			if (!(editor instanceof ITextEditor)) {
				return null;
			}
			ITextEditor textEditor = (ITextEditor) editor;
			IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
			if (document == null) {
				return null;
			}
			IRegion lineInfo = null;
			int lineNumber = finding.getLine();

			try {
				lineInfo = document.getLineInformation(lineNumber - 1);
			} catch (BadLocationException e) {
				try {
					String message = "Unable to find line " + lineNumber
							+ ". Please look for the line and code manually.";
					Logging.logError(message);

					MessageDialog.openError(getShell(), "Line not found!", message);

					IRegion startLine = document.getLineInformation(0);
					textEditor.selectAndReveal(startLine.getOffset(), startLine.getLength());
				} catch (BadLocationException blex) {
					Logging.logError("Unexpected: Line not found!", blex);
				}
				return null;
			}
			if (lineInfo == null) {
				// unable to find line
				return FindingMatchType.LINE_DID_NOT_MATCH;
			}

			return selectFindingInEditor(finding, textEditor, document, lineInfo, lineNumber);

		} catch (PartInitException ex) {
			Logging.logError(ex.getClass().getSimpleName(), ex);
			return null;
		}

	}

	private FindingMatchType selectFindingInEditor(FindingNode finding, ITextEditor textEditor, IDocument document,
			IRegion lineInfo, int lineNumber) {
		FindingMatchType findingInFile = null;

		ITextSelection selectedTextSnippet = new TextSelection(document, lineInfo.getOffset(), lineInfo.getLength());
		String textSnippet = SimpleStringUtil.removeAllSpaces(selectedTextSnippet.getText());
		String findingSnippet = SimpleStringUtil.removeAllSpaces(finding.getSource());

		// Check if the text snippet in the document is equal to the finding source code
		if (textSnippet.compareTo(findingSnippet) == 0) {
			String message = "The code in line " + lineNumber + " matches.";
			findingInFile = FindingMatchType.EXACT_MATCH;
			Logging.logInfo(message);
		} else {
			String message = "Found line " + lineNumber + ". Code did not match.";
			findingInFile = FindingMatchType.LINE_CONTENT_DIFFERENT;
			Logging.logWarning(message);
			MessageDialog.openWarning(getShell(), "Code not matching.", message);
		}

		// Jump to finding location
		textEditor.selectAndReveal(lineInfo.getOffset(), lineInfo.getLength());
		return findingInFile;
	}

	private Shell getShell() {
		IShellProvider shellProvider = workbench.getModalDialogShellProvider();
		Shell shell = shellProvider.getShell();

		return shell;
	}

	private enum FindingMatchType {
		EXACT_MATCH,

		LINE_CONTENT_DIFFERENT,

		LINE_DID_NOT_MATCH
	}

	/**
	 * We have much UI changes here, so we use a UIJob where we have access to UI directly withyout display
	 * async etc.
	 *
	 */
	private class FindingNodeLocatorJob extends UIJob {
	
		private FindingNode finding;
	
		public FindingNodeLocatorJob(FindingNode finding) {
			super("Start locating finding " + finding.getId());
			this.finding = finding;
		}
	
		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			return searchInProjectsForFindingAndShowInEditorInternal(finding, monitor);
		}
	
	}
}
