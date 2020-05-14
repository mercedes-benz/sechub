// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.daimler.sechub.developertools.admin.ui.action.ActionSupport;
import com.daimler.sechub.developertools.admin.ui.action.adapter.ShowAdapterDialogAction;
import com.daimler.sechub.developertools.admin.ui.action.integrationtestserver.FetchMockMailsAction;
import com.daimler.sechub.developertools.admin.ui.action.integrationtestserver.testdata.CreateScenario2TestDataAction;
import com.daimler.sechub.developertools.admin.ui.action.integrationtestserver.testdata.CreateScenario3TestDataAction;
import com.daimler.sechub.developertools.admin.ui.action.integrationtestserver.testdata.TriggerNewCodeScanJobScenario3User1Action;
import com.daimler.sechub.developertools.admin.ui.action.integrationtestserver.testdata.TriggerNewInfraScanJobScenario3User1Action;
import com.daimler.sechub.developertools.admin.ui.action.integrationtestserver.testdata.TriggerNewWebScanJobScenario3User1Action;
import com.daimler.sechub.developertools.admin.ui.action.job.CancelJobAction;
import com.daimler.sechub.developertools.admin.ui.action.job.DownloadFullscanDataForJobAction;
import com.daimler.sechub.developertools.admin.ui.action.job.DownloadHTMLReportForJobAction;
import com.daimler.sechub.developertools.admin.ui.action.job.DownloadJSONReportForJobAction;
import com.daimler.sechub.developertools.admin.ui.action.job.GetJobStatusAction;
import com.daimler.sechub.developertools.admin.ui.action.job.RestartJobAction;
import com.daimler.sechub.developertools.admin.ui.action.job.RestartJobHardAction;
import com.daimler.sechub.developertools.admin.ui.action.job.ShowRunningBatchJobsListAction;
import com.daimler.sechub.developertools.admin.ui.action.other.CheckAliveAction;
import com.daimler.sechub.developertools.admin.ui.action.other.CheckVersionAction;
import com.daimler.sechub.developertools.admin.ui.action.project.AssignUserToProjectAction;
import com.daimler.sechub.developertools.admin.ui.action.project.AssignUserToProjectMassCSVImportAction;
import com.daimler.sechub.developertools.admin.ui.action.project.CreateOverviewCSVExportAction;
import com.daimler.sechub.developertools.admin.ui.action.project.CreateProjectAction;
import com.daimler.sechub.developertools.admin.ui.action.project.CreateProjectMassCSVImportAction;
import com.daimler.sechub.developertools.admin.ui.action.project.DeleteProjectAction;
import com.daimler.sechub.developertools.admin.ui.action.project.DeleteProjectMassCSVImportAction;
import com.daimler.sechub.developertools.admin.ui.action.project.GetProjectMockConfigurationAction;
import com.daimler.sechub.developertools.admin.ui.action.project.SetProjectMockDataConfigurationAction;
import com.daimler.sechub.developertools.admin.ui.action.project.ShowProjectDetailAction;
import com.daimler.sechub.developertools.admin.ui.action.project.ShowProjectListAction;
import com.daimler.sechub.developertools.admin.ui.action.project.ShowProjectsScanLogsAction;
import com.daimler.sechub.developertools.admin.ui.action.project.UnassignUserFromProjectAction;
import com.daimler.sechub.developertools.admin.ui.action.project.UnassignUserFromProjectMassCSVImportAction;
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
import com.daimler.sechub.sharedkernel.mapping.MappingIdentifier;

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
		createStatusMenu();
		createJobMenu();
		createProjectMenu();
		createUserMenu();
		createEditMenu();
		createSchedulerMenu();
		createIntegrationTestServerMenu();
		createMassOperationsMenu();
		
		createAdapterMenu();

	}

	public void createEditMenu() {
		JMenu mainMenu = new JMenu("Edit");
		ActionSupport support = new ActionSupport();
		support.apply(mainMenu, support.createDefaultCutCopyAndPastActions());
		menuBar.add(mainMenu);
	}
	
	public void createAdapterMenu() {
        JMenu menu = new JMenu("Adapter");
        menuBar.add(menu);
        
        add(menu, new ShowAdapterDialogAction(context,"Checkmarx",MappingIdentifier.CHECKMARX_NEWPROJECT_PRESET_ID.getId(),MappingIdentifier.CHECKMARX_NEWPROJECT_TEAM_ID.getId()));
    }

	private void createUserMenu() {
		JMenu menu = new JMenu("User");
		menuBar.add(menu);

		add(menu,new AnonymousSigninNewUserAction(context));
		add(menu, new AcceptUserSignupAction(context));
		add(menu, new DeleteUserAction(context));
		menu.addSeparator();
		add(menu, new ShowUserDetailAction(context));
		add(menu,new AnonymousRequestNewAPITokenUserAction(context));
		menu.addSeparator();
		add(menu, new ListSignupsAction(context));
		add(menu, new ShowUserListAction(context));
		menu.addSeparator();
		add(menu, new AssignUserToProjectAction(context));
		add(menu, new UnassignUserFromProjectAction(context));
		menu.addSeparator();

		JMenu adminMenu = new JMenu("Admin rights");
		add(adminMenu,new GrantAdminRightsToUserAction(context));
		add(adminMenu,new RevokeAdminRightsFromAdminAction(context));
		adminMenu.addSeparator();
		add(adminMenu, new ShowAdminListAction(context));
		menu.add(adminMenu);
	}

	private void createProjectMenu() {
		JMenu menu = new JMenu("Project");
		menuBar.add(menu);

		add(menu, new CreateProjectAction(context));
		add(menu, new DeleteProjectAction(context));
		add(menu, new ShowProjectDetailAction(context));
		add(menu, new ShowProjectsScanLogsAction(context));
		menu.addSeparator();
		add(menu, new AssignUserToProjectAction(context));
		add(menu, new UnassignUserFromProjectAction(context));
		menu.addSeparator();
		add(menu, new ShowProjectListAction(context));
		menu.addSeparator();
		add(menu, new UpdateProjectWhitelistAction(context));
		
		JMenu projectMockData = new JMenu("Mockdata");
		menu.add(projectMockData);
		
		add(projectMockData, new SetProjectMockDataConfigurationAction(context));
		add(projectMockData, new GetProjectMockConfigurationAction(context));
	}

	private void createStatusMenu() {
		JMenu menu = new JMenu("Status");
		menuBar.add(menu);

		add(menu, new ShowRunningBatchJobsListAction(context));
		menu.addSeparator();
		add(menu, new CheckAliveAction(context));
		add(menu, new CheckVersionAction(context));
		add(menu, new ListStatusEntriesAction(context));
		menu.addSeparator();
		add(menu, new ShowAdminListAction(context));
		add(menu, new CreateOverviewCSVExportAction(context));
	}

	private void createSchedulerMenu() {
		JMenu menu = new JMenu("Scheduler");
		menuBar.add(menu);

		add(menu, new DisableSchedulerJobProcessingAction(context));
		add(menu, new EnableSchedulerJobProcessingAction(context));
		add(menu, new RefreshSchedulerStatusAction(context));
	}

	private void createJobMenu() {
		JMenu menu = new JMenu("Job");
		menuBar.add(menu);
		add(menu, new GetJobStatusAction(context));
		add(menu, new CancelJobAction(context));
		add(menu, new RestartJobAction(context));
		add(menu, new RestartJobHardAction(context));
		menu.addSeparator();
		add(menu, new ShowRunningBatchJobsListAction(context));
		menu.addSeparator();
		add(menu, new DownloadJSONReportForJobAction(context));
		add(menu, new DownloadHTMLReportForJobAction(context));
		menu.addSeparator();
		add(menu, new DownloadFullscanDataForJobAction(context));
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
		add(menu, new SetProjectMockDataConfigurationAction(context));
		add(menu, new GetProjectMockConfigurationAction(context));
		menu.addSeparator();

		JMenu testDataMenu = new JMenu("Testdata");
		menu.add(testDataMenu);
		add(testDataMenu, new CreateScenario2TestDataAction(context));
		add(testDataMenu, new CreateScenario3TestDataAction(context));
		testDataMenu.addSeparator();
		add(testDataMenu, new TriggerNewInfraScanJobScenario3User1Action(context));
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
		add(massOperationsMenu, new AssignUserToProjectMassCSVImportAction(context));
		massOperationsMenu.addSeparator();
		add(massOperationsMenu, new DeleteProjectMassCSVImportAction(context));
		add(massOperationsMenu, new UnassignUserFromProjectMassCSVImportAction(context));
	}


	private void add(JMenu menu, AbstractAction action) {
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
