// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.action.ActionSupport;
import com.daimler.sechub.developertools.admin.ui.action.integrationtestserver.FetchMockMailsAction;
import com.daimler.sechub.developertools.admin.ui.action.integrationtestserver.testdata.CreateScenario2TestDataAction;
import com.daimler.sechub.developertools.admin.ui.action.integrationtestserver.testdata.CreateScenario3TestDataAction;
import com.daimler.sechub.developertools.admin.ui.action.integrationtestserver.testdata.TriggerNewCodeScanJobScenario3User1Action;
import com.daimler.sechub.developertools.admin.ui.action.integrationtestserver.testdata.TriggerNewWebScanJobScenario3User1Action;
import com.daimler.sechub.developertools.admin.ui.action.job.CancelJobAction;
import com.daimler.sechub.developertools.admin.ui.action.job.DownloadFullscanDataForJobAction;
import com.daimler.sechub.developertools.admin.ui.action.job.DownloadHTMLReportForJobAction;
import com.daimler.sechub.developertools.admin.ui.action.job.DownloadJSONReportForJobAction;
import com.daimler.sechub.developertools.admin.ui.action.job.GetJobStatusAction;
import com.daimler.sechub.developertools.admin.ui.action.job.ShowRunningBatchJobsListAction;
import com.daimler.sechub.developertools.admin.ui.action.other.CheckAliveAction;
import com.daimler.sechub.developertools.admin.ui.action.other.CheckVersionAction;
import com.daimler.sechub.developertools.admin.ui.action.project.AssignUserToProjectAction;
import com.daimler.sechub.developertools.admin.ui.action.project.CreateOverviewCSVExportAction;
import com.daimler.sechub.developertools.admin.ui.action.project.CreateProjectAction;
import com.daimler.sechub.developertools.admin.ui.action.project.CreateProjectMassCSVImportAction;
import com.daimler.sechub.developertools.admin.ui.action.project.DeleteProjectAction;
import com.daimler.sechub.developertools.admin.ui.action.project.DeleteProjectMassCSVImportAction;
import com.daimler.sechub.developertools.admin.ui.action.project.ShowProjectDetailAction;
import com.daimler.sechub.developertools.admin.ui.action.project.ShowProjectListAction;
import com.daimler.sechub.developertools.admin.ui.action.project.ShowProjectsScanLogsAction;
import com.daimler.sechub.developertools.admin.ui.action.project.UnassignUserFromProjectAction;
import com.daimler.sechub.developertools.admin.ui.action.project.UpdateProjectWhitelistAction;
import com.daimler.sechub.developertools.admin.ui.action.scheduler.DisableSchedulerJobProcessingAction;
import com.daimler.sechub.developertools.admin.ui.action.scheduler.EnableSchedulerJobProcessingAction;
import com.daimler.sechub.developertools.admin.ui.action.scheduler.RefreshSchedulerStatusAction;
import com.daimler.sechub.developertools.admin.ui.action.status.ListStatusEntriesAction;
import com.daimler.sechub.developertools.admin.ui.action.user.AcceptUserSignupAction;
import com.daimler.sechub.developertools.admin.ui.action.user.AnonymousRequestNewAPITokenUserAction;
import com.daimler.sechub.developertools.admin.ui.action.user.AnonymousSigninNewUserAction;
import com.daimler.sechub.developertools.admin.ui.action.user.DeleteUserAction;
import com.daimler.sechub.developertools.admin.ui.action.user.ListSignupsAction;
import com.daimler.sechub.developertools.admin.ui.action.user.ShowAdminListAction;
import com.daimler.sechub.developertools.admin.ui.action.user.ShowUserDetailAction;
import com.daimler.sechub.developertools.admin.ui.action.user.ShowUserListAction;
import com.daimler.sechub.developertools.admin.ui.action.user.priviledges.GrantAdminRightsToUserAction;
import com.daimler.sechub.developertools.admin.ui.action.user.priviledges.RevokeAdminRightsFromAdminAction;
import com.daimler.sechub.integrationtest.api.IntegrationTestMockMode;

public class CommandUI {
	private JPanel panel;
	private JMenuBar menuBar;

	private JProgressBar progressBar;
	private Queue<String> queue = new ConcurrentLinkedQueue<>();
	UIContext context;

	public JPanel getPanel() {
		return panel;
	}

	public JMenuBar getMenuBar() {
		return menuBar;
	}

	public CommandUI(UIContext context) {
		this.context = context;

		progressBar = new JProgressBar();
		progressBar.setIndeterminate(false);
		progressBar.setPreferredSize(new Dimension(400,30));

		panel = new JPanel(new BorderLayout());
//		panel.setBorder(BorderFactory.createLineBorder(Color.RED));

		panel.add(progressBar, BorderLayout.EAST);

		menuBar = new JMenuBar();
		createGlobalMenu();
		createJobMenu();
		createProjectMenu();
		createUserMenu();

		createEditMenu();
		createIntegrationTestServerMenu();
		createMassOperationsMenu();

	}

	public void createEditMenu() {
		JMenu mainMenu = new JMenu("Edit");
		ActionSupport support = new ActionSupport();
		support.apply(mainMenu, support.createDefaultCutCopyAndPastActions());
		menuBar.add(mainMenu);
	}

	private void createUserMenu() {
		JMenu menu = new JMenu("User");
		menuBar.add(menu);

		JMenu signupMenu = new JMenu("Signup");
		menu.add(signupMenu);
		add(signupMenu, new ListSignupsAction(context));
		add(signupMenu, new AcceptUserSignupAction(context));

		menu.addSeparator();

		add(menu, new ShowUserListAction(context));
		add(menu, new ShowUserDetailAction(context));
		add(menu, new ShowAdminListAction(context));
		menu.addSeparator();

		add(menu, new AssignUserToProjectAction(context));
		add(menu, new UnassignUserFromProjectAction(context));
		menu.addSeparator();

		add(menu, new DeleteUserAction(context));
		menu.addSeparator();

		JMenu anonymous = new JMenu("Anonymous");
		menu.add(anonymous);
		add(anonymous,new AnonymousRequestNewAPITokenUserAction(context));
		add(anonymous,new AnonymousSigninNewUserAction(context));

		JMenu grantMenu = new JMenu("Grant");
		add(grantMenu,new GrantAdminRightsToUserAction(context));
		menu.add(grantMenu);

		JMenu revokeMenu = new JMenu("Revoke");
		add(revokeMenu,new RevokeAdminRightsFromAdminAction(context));
		menu.add(revokeMenu);


	}

	private void createProjectMenu() {
		JMenu menu = new JMenu("Project");
		menuBar.add(menu);

		add(menu, new CreateProjectAction(context));
		add(menu, new DeleteProjectAction(context));
		menu.addSeparator();
		add(menu, new ShowProjectListAction(context));
		add(menu, new ShowProjectDetailAction(context));
		add(menu, new ShowProjectsScanLogsAction(context));
		add(menu, new UpdateProjectWhitelistAction(context));
		menu.addSeparator();
		add(menu, new AssignUserToProjectAction(context));
		add(menu, new UnassignUserFromProjectAction(context));

	}

	private void createGlobalMenu() {
		JMenu menu = new JMenu("Global");
		menuBar.add(menu);

		JMenu schedulerMenu = new JMenu("Scheduler");
		add(schedulerMenu, new DisableSchedulerJobProcessingAction(context));
		add(schedulerMenu, new EnableSchedulerJobProcessingAction(context));
		add(schedulerMenu, new RefreshSchedulerStatusAction(context));
		menu.add(schedulerMenu);

		JMenu statusMenu = new JMenu("Status");
		menu.add(statusMenu);

		add(statusMenu, new ShowRunningBatchJobsListAction(context));
		statusMenu.addSeparator();
		add(statusMenu, new CheckAliveAction(context));
		add(statusMenu, new CheckVersionAction(context));
		statusMenu.addSeparator();
		add(statusMenu, new ListStatusEntriesAction(context));
		add(statusMenu, new CreateOverviewCSVExportAction(context));
		add(statusMenu, new ShowAdminListAction(context));

	}
	private void createJobMenu() {
		JMenu menu = new JMenu("Job");
		menuBar.add(menu);
		add(menu, new GetJobStatusAction(context));
		add(menu, new ShowRunningBatchJobsListAction(context));
		add(menu, new CancelJobAction(context));
		menu.addSeparator();
		add(menu, new DownloadJSONReportForJobAction(context));
		add(menu, new DownloadHTMLReportForJobAction(context));
		menu.addSeparator();
		add(menu, new DownloadFullscanDataForJobAction(context));
		add(menu, new ShowRunningBatchJobsListAction(context));


	}

	private void createIntegrationTestServerMenu() {
		JMenu menu = new JMenu("Integration TestServer");
		menuBar.add(menu);
		if (! ConfigurationSetup.isIntegrationTestServerMenuEnabled()) {
			menu.setEnabled(false);
			menu.setToolTipText("Not enabled, use \"-D"+ConfigurationSetup.SECHUB_ENABLE_INTEGRATION_TESTSERVER_MENU.getSystemPropertyid()+"=true\" to enable it and run an integration test server!");
		}
		add(menu, new FetchMockMailsAction(context));
		menu.addSeparator();

		JMenu testDataMenu = new JMenu("Testdata");
		menu.add(testDataMenu);
		add(testDataMenu, new CreateScenario2TestDataAction(context));
		add(testDataMenu, new CreateScenario3TestDataAction(context));
		testDataMenu.addSeparator();
		add(testDataMenu, new TriggerNewWebScanJobScenario3User1Action(context,IntegrationTestMockMode.WEBSCAN__NETSPARKER_RESULT_GREEN__FAST));
		add(testDataMenu, new TriggerNewWebScanJobScenario3User1Action(context,IntegrationTestMockMode.WEBSCAN__NETSPARKER_RESULT_GREEN__LONG_RUNNING));
		add(testDataMenu, new TriggerNewWebScanJobScenario3User1Action(context,IntegrationTestMockMode.WEBSCAN__NETSPARKER_RESULT_ONE_FINDING__FAST));
		add(testDataMenu, new TriggerNewWebScanJobScenario3User1Action(context,IntegrationTestMockMode.WEBSCAN__NETSPARKER_MANY_RESULTS__FAST));
		testDataMenu.addSeparator();
		add(testDataMenu, new TriggerNewCodeScanJobScenario3User1Action(context,IntegrationTestMockMode.CODE_SCAN__CHECKMARX__YELLOW__FAST));
		add(testDataMenu, new TriggerNewCodeScanJobScenario3User1Action(context,IntegrationTestMockMode.CODE_SCAN__CHECKMARX__GREEN__FAST));


	}

	private void createMassOperationsMenu() {
		JMenu massOperationsMenu = new JMenu("Mass operations");
		menuBar.add(massOperationsMenu);
		add(massOperationsMenu, new CreateProjectMassCSVImportAction(context));
		massOperationsMenu.addSeparator();
		add(massOperationsMenu, new DeleteProjectMassCSVImportAction(context));
	}


	private void add(JMenu menu, AbstractUIAction action) {
		JMenuItem menuItem = new JMenuItem(action);
		menu.add(menuItem);

	}

	public void startActionProgress(String threadName) {
		queue.add(threadName);
		handleQueueState();
	}

	public void stopActionProgress(String threadName) {
		queue.remove(threadName);
		handleQueueState();
	}

	private void handleQueueState() {
		boolean showProgress = !queue.isEmpty();
		progressBar.setIndeterminate(showProgress);
		context.getGlassPaneUI().block(showProgress);
	}

}
