// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.idea.window;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.intellij.ide.BrowserUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.JBColor;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.mercedesbenz.sechub.api.internal.gen.model.ProjectData;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubJobInfoForUser;
import com.mercedesbenz.sechub.api.internal.gen.model.SecHubJobInfoForUserListPage;
import com.mercedesbenz.sechub.plugin.idea.SecHubReportRequestListener;
import com.mercedesbenz.sechub.plugin.idea.SecHubSettingsDialogListener;
import org.jetbrains.annotations.NotNull;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.mercedesbenz.sechub.plugin.idea.sechubaccess.SecHubAccess;
import com.mercedesbenz.sechub.plugin.idea.sechubaccess.SecHubAccessFactory;
import com.mercedesbenz.sechub.settings.SechubSettings;

import static java.util.Objects.requireNonNull;

public class SecHubServerPanel implements SecHubPanel {

    private static final Logger LOG = Logger.getInstance(SecHubServerPanel.class);
    private static final String SERVER_URL_NOT_CONFIGURED = "Server URL not configured";
    private static SecHubServerPanel INSTANCE;
    private static final int JOB_PAGE_SIZE = 10;
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SecHubSettingsDialogListener settingsDialogListener;
    private final SecHubReportRequestListener secHubReportRequestListener;
    private JPanel contentPanel;
    private final ObservableSettingsState observableSettingsState = new ObservableSettingsState();
    private final ObservableServerConnection observableServerConnection = new ObservableServerConnection();
    private final ObservableCurrentProjectId observableCurrentProjectId = new ObservableCurrentProjectId();
    private final ObservableProjects observableProjects = new ObservableProjects();
    private final ObservableJobPage observableJobPage = new ObservableJobPage();

    public SecHubServerPanel(SecHubSettingsDialogListener settingsDialogListener, SecHubReportRequestListener secHubReportRequestListener) {
        this.settingsDialogListener = requireNonNull(settingsDialogListener, "Property 'settingsDialogListener' must not be null");
        this.secHubReportRequestListener = requireNonNull(secHubReportRequestListener, "Parameter 'secHubReportLoadListener' must not be null");
        createServerConfigurationChangeListener();
        checkServerConnection();
        createComponents();
    }

    public static void registerInstance(SecHubServerPanel secHubToolWindow) {
        LOG.info("Register tool windows instance:" + secHubToolWindow);
        INSTANCE = secHubToolWindow;
    }

    public static SecHubServerPanel getInstance() {
        return INSTANCE;
    }

    @Override
    public JPanel getContent() {
        return contentPanel;
    }

    public void updateSettingsState(SechubSettings.State state) {
        observableSettingsState.setValue(state);
    }

    /**
     * Creates a listener that listens for changes in the server configuration and
     * rechecks the server connection whenever the configuration changes.
     */
    private void createServerConfigurationChangeListener() {
        observableSettingsState.addPropertyChangeListener(event -> {
            checkServerConnection();
        });
    }

    private boolean checkServerConnection() {
        boolean isServerAlive = secHubAccess().isSecHubServerAlive();
        observableServerConnection.setValue(isServerAlive);
        return isServerAlive;
    }


    private void createComponents() {
        contentPanel = new JBPanel<>();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        /* create action bar */
        JPanel actionBar = createActionBarPanel();
        contentPanel.add(actionBar);

        /* creates panel with server configuration information */
        final JPanel serverConfigurationPanel = createServerConfigurationPanel();
        contentPanel.add(serverConfigurationPanel, BorderLayout.AFTER_LAST_LINE);

        /* creates a separator to visually separate the server configuration from the projects & jobs panels */
        JSeparator contentSeparator = createContentSeparator();
        contentPanel.add(contentSeparator, BorderLayout.AFTER_LAST_LINE);

        JPanel projectsPanel = createProjectsPanel();
        JPanel jobsPanel = createJobsPanel();
        JBSplitter projectsAndJobsSplitter = createProjectsAndJobsSplitter(projectsPanel, jobsPanel);

        contentPanel.add(projectsAndJobsSplitter, BorderLayout.CENTER);
    }

    @NotNull
    private JPanel createActionBarPanel() {
        DefaultActionGroup actionGroup = new DefaultActionGroup();

        /* create action to refresh the server connection */
        AnAction refreshAction = new AnAction("Refresh Server Connection", "Refresh server connection", AllIcons.Actions.Refresh) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                checkServerConnection();
            }
        };
        actionGroup.add(refreshAction);

        /* create action to open the SecHub Web UI */
        AnAction openWebUiAction = new AnAction("Open Web UI", "Open SecHub web ui", AllIcons.General.Web) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                String webUiURL = observableSettingsState.getWebUiURL();
                if (observableSettingsState.useCustomWebUiUrl() && (webUiURL == null || webUiURL.isBlank())) {
                    settingsDialogListener.onShowSettingsDialog();
                    return;
                }

                try {
                    URL url = new URL(webUiURL);
                    BrowserUtil.browse(url);
                } catch (MalformedURLException ex) {
                    LOG.error("Malformed URL for SecHub Web UI: " + webUiURL, ex);
                    settingsDialogListener.onShowSettingsDialog();
                }
            }
        };
        actionGroup.add(openWebUiAction);

        /* create action to open settings dialog */
        AnAction settingsAction = new AnAction("Settings", "Open settings dialog", AllIcons.General.Settings) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                settingsDialogListener.onShowSettingsDialog();
            }
        };
        actionGroup.add(settingsAction);

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, actionGroup, true);
        toolbar.setTargetComponent(null);
        toolbar.setMinimumButtonSize(new Dimension(25, 25));

        JPanel actionBar = new JPanel(new BorderLayout());
        actionBar.add(toolbar.getComponent(), BorderLayout.WEST);
        actionBar.setOpaque(false);
        actionBar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 30));
        actionBar.setMinimumSize(new Dimension(0, 30));
        actionBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        actionBar.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        return actionBar;
    }

    @NotNull
    private JPanel createServerConfigurationPanel() {
        JPanel content = new JPanel(new GridBagLayout());

        JLabel serverUrlLabel = new JBLabel(observableSettingsState.getServerURL());
        serverUrlLabel.setText(observableSettingsState.getServerURL());

        /* listen to changes in server config & update components */
        observableSettingsState.addPropertyChangeListener(event -> {
            String newServerUrl = observableSettingsState.getServerURL();
            serverUrlLabel.setText(newServerUrl);
        });

        JPanel serverActionPanel = new JPanel();
        serverActionPanel.setLayout(new BorderLayout());

        JLabel checkServerConnectionLabel = new JLabel();

        updateServerConnectionLabel(checkServerConnectionLabel, observableServerConnection.getValue());

        /* listen to changes in server connection status and update the connection label */
        observableServerConnection.addPropertyChangeListener(event -> {
            updateServerConnectionLabel(checkServerConnectionLabel, (boolean) event.getNewValue());
        });

        serverActionPanel.add(checkServerConnectionLabel, BorderLayout.WEST);

        GridBagConstraints gbc = new GridBagConstraints();

        /* First column (minimized) */
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        content.add(new JBLabel("Server URL: "), gbc);

        /* Second column (takes max space) */
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        content.add(serverUrlLabel, gbc);

        /* First column (minimized) */
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.insets = JBUI.insets(5, 0, 15, 0);
        content.add(new JBLabel("Server connection: "), gbc);

        /* Second column (takes max space) */
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.insets = JBUI.insets(5, 0, 15, 0);
        content.add(serverActionPanel, gbc);

        content.setMaximumSize(new Dimension(Integer.MAX_VALUE, content.getPreferredSize().height));
        content.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));

        return content;
    }

    private void updateServerConnectionLabel(JLabel checkServerConnectionLabel, boolean isServerAlive) {
        if (isServerAlive) {
            checkServerConnectionLabel.setIcon(AllIcons.General.InspectionsOK);
        } else {
            checkServerConnectionLabel.setIcon(AllIcons.Ide.ErrorPoint);
        }
    }

    @NotNull
    private JSeparator createContentSeparator() {
        JSeparator contentSeparator = new JSeparator(SwingConstants.HORIZONTAL);
        contentSeparator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        contentSeparator.setPreferredSize(new Dimension(Integer.MAX_VALUE, 10));
        return contentSeparator;
    }

    @NotNull
    private JPanel createProjectsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

        JLabel noProjectsLabel = new JBLabel("You are not assigned to any SecHub projects.", AllIcons.General.Warning, SwingConstants.LEFT);
        panel.add(noProjectsLabel);

        /* create dropdown for project IDs */
        JBLabel comboBoxLabel = new JBLabel("Project:");
        panel.add(comboBoxLabel);
        ComboBox<String> comboBox = new ComboBox<>();
        panel.add(comboBox);

        /* add refresh button for projects & jobs */
        JButton refreshButton = new JButton(AllIcons.Actions.Refresh);
        refreshButton.setPreferredSize(new Dimension(30, 30));
        refreshButton.setToolTipText("Refresh Projects & Jobs");
        refreshButton.addActionListener(e -> {
            if (checkServerConnection()) {
                loadProjects();
                loadJobs();
            }
        });
        panel.add(refreshButton);

        /* add action listener to update the current project ID when a project is selected */
        comboBox.addActionListener(e -> {
            String selectedProjectId = (String) comboBox.getSelectedItem();
            observableCurrentProjectId.setValue(selectedProjectId);
        });

        /* reload the dropdown when projects are loaded */
        observableProjects.addPropertyChangeListener(event -> {
            List<ProjectData> projects = (List<ProjectData>) event.getNewValue();
            createProjectsDropdown(projects, comboBox, comboBoxLabel, noProjectsLabel, panel);
        });

        /* listen to changes in server connection status to update projects & jobs components */
        observableServerConnection.addPropertyChangeListener(event -> {
            boolean isServerAlive = observableServerConnection.getValue();
            if (isServerAlive) {
                /* reload projects from server if server is alive */
                loadProjects();
                loadJobs();
            } else {
                /* if server is not alive, clear data */
                observableProjects.setValue(List.of());
                observableJobPage.setValue(null);
            }
        });

        boolean isServerAlive = checkServerConnection();
        if (isServerAlive) {
            loadProjects();
        }

        return panel;
    }

    private void loadProjects() {
        List<ProjectData> secHubProjects = secHubAccess().getSecHubProjects();
        observableProjects.setValue(secHubProjects);
    }

    /* @formatter:off */
    private void createProjectsDropdown(List<ProjectData> projects,
                                        ComboBox<String> comboBox,
                                        JBLabel comboBoxLabel,
                                        JLabel noProjectsLabel,
                                        JPanel panel) {
        /* @formatter:on */

        String currentProjectId = observableCurrentProjectId.getValue();

        /* clear combo box */
        comboBox.removeAllItems();

        /* add fresh project IDs from SecHub server to the combo box */
        projects.forEach(projectData -> comboBox.addItem(projectData.getProjectId()));

        /* check if the current project ID from cache is still existing */
        boolean isDeprecatedCurrentProjectId = projects.stream().noneMatch(projectData -> projectData.getProjectId().equals(currentProjectId));

        if (isDeprecatedCurrentProjectId) {
            LOG.debug("Deprecated current project id found: {}", currentProjectId);
            observableCurrentProjectId.setValue(null);
        }

        if (currentProjectId == null) {
            /* no current project id exists, set the first project of the list as current */
            observableCurrentProjectId.setValue((String) comboBox.getSelectedItem());
        } else {
            /* use the project id from the cache */
            comboBox.setSelectedItem(currentProjectId);
        }

        if (projects.isEmpty()) {
            /* no projects found, hide the combo box and show the no projects label */
            LOG.debug("No SecHub projects found");
            comboBoxLabel.setVisible(false);
            comboBox.setVisible(false);
            noProjectsLabel.setVisible(true);
        } else {
            /* projects found, show the combo box and hide the no projects label */
            comboBoxLabel.setVisible(true);
            comboBox.setVisible(true);
            panel.remove(noProjectsLabel);
            noProjectsLabel.setVisible(false);
        }
    }

    private JPanel createJobsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columnNames = {"Created", "", "UUID", "State", "Result", "Executed By"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                /* Make the table non-editable */
                return false;
            }
        };
        JBTable table = new JBTable(model);
        /* @formatter:off */
        table.getColumnModel().getColumn(0).setPreferredWidth(140); /* created */
        table.getColumnModel().getColumn(0).setMaxWidth(140);       /* created */
        table.getColumnModel().getColumn(1).setWidth(30);           /* traffic light */
        table.getColumnModel().getColumn(1).setMaxWidth(30);        /* traffic light */
        table.getColumnModel().getColumn(2).setPreferredWidth(120); /* job uuid */
        table.getColumnModel().getColumn(3).setPreferredWidth(60);  /* state */
        table.getColumnModel().getColumn(4).setPreferredWidth(60);  /* result */
        table.getColumnModel().getColumn(5).setPreferredWidth(120); /* executed by */
        /* @formatter:on */

        table.getColumnModel().getColumn(1).setCellRenderer(new SecHubServerPanel.TrafficLightRenderer());

        /* add double click handler for report loading */
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                /*  Double-click detected */
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        String currentProjectId = observableCurrentProjectId.getValue();
                        /* Job UUID is in the third column (index 2) */
                        UUID jobUUID = (UUID) table.getValueAt(row, 2);
                        if (currentProjectId != null && jobUUID != null) {
                            secHubReportRequestListener.onReportRequested(currentProjectId, jobUUID);
                        }
                    }
                }
            }
        });

        JScrollPane scrollPane = new JBScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        /* add pagination for jobs */
        JPanel paginationPanel = createJobPaginationPanel();
        panel.add(paginationPanel, BorderLayout.SOUTH);

        /* load the jobs when current project ID changes */
        observableCurrentProjectId.addPropertyChangeListener(event -> {
            /* reset the current job page when selected project id changes */
            observableJobPage.reset();
            String currentProjectId = (String) event.getNewValue();
            if (currentProjectId != null) {
                loadJobs();
            }
        });

        /* load the job table when job page changes */
        observableJobPage.addPropertyChangeListener(event -> {
            SecHubJobInfoForUserListPage page = (SecHubJobInfoForUserListPage) event.getNewValue();
            if (page != null && page.getContent() != null) {
                loadJobsTable(model, page.getContent());
            }
        });

        boolean isServerAlive = observableServerConnection.getValue();
        if (isServerAlive) {
            /* load jobs initially */
            loadJobs();
        }

        return panel;
    }

    private JPanel createJobPaginationPanel() {
        JButton prevButton = new JButton(AllIcons.Actions.Back);
        prevButton.setPreferredSize(new Dimension(30, 30));
        JButton nextButton = new JButton(AllIcons.Actions.Forward);
        nextButton.setPreferredSize(new Dimension(30, 30));
        JLabel pageLabel = new JLabel();

        /* update the displayed page label when pagination values change */
        observableJobPage.addPropertyChangeListener(event -> {
            SecHubJobInfoForUserListPage page = (SecHubJobInfoForUserListPage) event.getNewValue();
            if (page != null) {
                updatePageLabel(pageLabel, page.getPage(), page.getTotalPages());
            }
        });

        /* add action listener for pagination previous button */
        prevButton.addActionListener(e -> {
            boolean hasChanged = observableJobPage.previous();
            if (hasChanged) {
                loadJobs();
            }
        });

        /* add action listener for pagination next button */
        nextButton.addActionListener(e -> {
            boolean hasChanged = observableJobPage.next();
            if (hasChanged) {
                loadJobs();
            }
        });

        JPanel paginationPanel = new JPanel();
        paginationPanel.add(prevButton);
        paginationPanel.add(pageLabel);
        paginationPanel.add(nextButton);

        return paginationPanel;
    }

    private void updatePageLabel(JLabel pageLabel, Integer page, Integer totalPages) {
        pageLabel.setText("%d / %d".formatted(page + 1, totalPages == 0 ? 1 : totalPages));
    }

    /**
     * Resets the job table and populates it with the latest job data.
     *
     * @param model The table model to populate with job data.
     * @param jobs The list of jobs
     */
    private void loadJobsTable(DefaultTableModel model, List<SecHubJobInfoForUser> jobs) {
        /* reset the table data */
        model.setRowCount(0);

        jobs.forEach(job -> {
            /* @formatter:off */
            OffsetDateTime started = job.getStarted();
            String created = started == null ? null : dateTimeFormatter.format(started);
            model.addRow(new Object[]{
                    created,
                    job.getTrafficLight(),
                    job.getJobUUID(),
                    job.getExecutionState(),
                    job.getExecutionResult(),
                    job.getExecutedBy(),
            });
            /* @formatter:on */
        });
    }

    private void loadJobs() {
        String projectId = observableCurrentProjectId.getValue();
        if (projectId != null) {
            int pageIndex = observableJobPage.getPage();
            SecHubJobInfoForUserListPage page = secHubAccess().getSecHubJobPage(projectId, JOB_PAGE_SIZE, pageIndex);
            observableJobPage.setValue(page);
        }
    }

    @NotNull
    private JBSplitter createProjectsAndJobsSplitter(JPanel projectsPanel, JPanel jobsPanel) {
        JBSplitter projectDropdownAndJobsTableSplitter = new OnePixelSplitter(true);
        /* minimize the size of the project dropdown panel (1%) */
        projectDropdownAndJobsTableSplitter.setProportion(0.01f);
        projectDropdownAndJobsTableSplitter.setShowDividerControls(true);
        projectDropdownAndJobsTableSplitter.setShowDividerIcon(true);
        projectDropdownAndJobsTableSplitter.setFirstComponent(projectsPanel);
        projectDropdownAndJobsTableSplitter.setSecondComponent(jobsPanel);
        projectDropdownAndJobsTableSplitter.setDividerPositionStrategy(Splitter.DividerPositionStrategy.KEEP_PROPORTION);

        /* display/hide projects & jobs panel based on server connection status changes */
        observableServerConnection.addPropertyChangeListener(event -> {
            boolean isServerAlive = observableServerConnection.getValue();
            projectDropdownAndJobsTableSplitter.setVisible(isServerAlive);
        });

        /* initially set the visibility based on server connection status */
        boolean isServerAlive = observableServerConnection.getValue();
        projectDropdownAndJobsTableSplitter.setVisible(isServerAlive);

        return projectDropdownAndJobsTableSplitter;
    }

    @NotNull
    private static SecHubAccess secHubAccess() {
        return SecHubAccessFactory.create();
    }

    private static class ObservableSettingsState {
        private static final String SETTINGS_STATE_PROPERTY = "settingsState";

        private SechubSettings.State state = SechubSettings.getInstance().getState();
        private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

        public void setValue(SechubSettings.State newValue) {
            state = newValue;
            propertyChangeSupport.firePropertyChange(SETTINGS_STATE_PROPERTY, null, newValue);
        }

        public SechubSettings.State getValue() {
            return state;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            propertyChangeSupport.addPropertyChangeListener(listener);
        }

        @NotNull
        private String getServerURL() {
            if (state == null) {
                return SERVER_URL_NOT_CONFIGURED;
            }

            String serverURL = state.serverURL;
            if (serverURL.isBlank()) {
                return SERVER_URL_NOT_CONFIGURED;
            }

            return serverURL;
        }

        private boolean useCustomWebUiUrl() {
            if (state == null) {
                return false;
            }
            return state.useCustomWebUiUrl;
        }

        private String getWebUiURL() {
            if (state == null) {
                return "";
            }
            return state.webUiURL;
        }

        private boolean isSslTrustAll() {
            if (state == null) {
                return false;
            }
            return state.sslTrustAll;
        }

    }

    private static class ObservableServerConnection {

        private static final String IS_SERVER_ALIVE = "isServerAlive";

        private boolean isServerAlive;
        private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

        public void setValue(boolean value) {
            isServerAlive = value;
            /* passing null for old value will force event to be triggered always */
            propertyChangeSupport.firePropertyChange(IS_SERVER_ALIVE, null, value);
        }

        public boolean getValue() {
            return isServerAlive;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            propertyChangeSupport.addPropertyChangeListener(listener);
        }
    }

    /**
     * Observable class to manage the current project ID.
     * This class uses {@link PropertiesComponent} to persist the current project ID, making it available across restarts.
     */
    private static class ObservableCurrentProjectId {

        private static final String SECHUB_PLUGIN_STATE_CURRENT_PROJECT_ID = "sechub.currentProjectId";
        private static final String CURRENT_PROJECT_ID_PROPERTY_NAME = "currentProjectId";

        private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

        public void setValue(String newValue) {
            PropertiesComponent.getInstance().setValue(SECHUB_PLUGIN_STATE_CURRENT_PROJECT_ID, newValue);
            propertyChangeSupport.firePropertyChange(CURRENT_PROJECT_ID_PROPERTY_NAME, null, newValue);
        }

        public String getValue() {
            return PropertiesComponent.getInstance().getValue(SECHUB_PLUGIN_STATE_CURRENT_PROJECT_ID);
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            propertyChangeSupport.addPropertyChangeListener(listener);
        }
    }

    private static class ObservableProjects {

        private static final String PROJECTS_PROPERTY_NAME = "projects";
        private List<ProjectData> projects;
        private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

        public void setValue(List<ProjectData> newValue) {
            projects = newValue;
            propertyChangeSupport.firePropertyChange(PROJECTS_PROPERTY_NAME, null, newValue);
        }

        public List<ProjectData> getValue() {
            return projects;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            propertyChangeSupport.addPropertyChangeListener(listener);
        }
    }

    private static class ObservableJobPage {
        private static final String JOB_PAGE_PROPERTY_NAME = "jobPage";
        private SecHubJobInfoForUserListPage page;
        private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

        public void setValue(SecHubJobInfoForUserListPage newValue) {
            page = newValue;
            propertyChangeSupport.firePropertyChange(JOB_PAGE_PROPERTY_NAME, null, newValue);
        }

        @NotNull
        public SecHubJobInfoForUserListPage getValue() {
            return page;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            propertyChangeSupport.addPropertyChangeListener(listener);
        }

        public boolean previous() {
            int currentPageIndex = page.getPage() == null ? 0 : page.getPage();
            if (currentPageIndex > 0) {
                int nextPageIndex = currentPageIndex - 1;
                page.setPage(nextPageIndex);
                propertyChangeSupport.firePropertyChange(JOB_PAGE_PROPERTY_NAME, null, page);

                return true;
            }

            return false;
        }

        public boolean next() {
            int currentPage = page.getPage() == null ? 0 : page.getPage();
            int totalPages = page.getTotalPages() == null ? 0 : page.getTotalPages();
            if (currentPage < totalPages - 1) {
                /* Note: page starts with 0, so we have to check if currentPage is less than totalPages - 1 */
                int nextPageIndex = currentPage + 1;
                page.setPage(nextPageIndex);
                propertyChangeSupport.firePropertyChange(JOB_PAGE_PROPERTY_NAME, null, page);

                return true;
            }

            return false;
        }

        public int getPage() {
            if (page == null || page.getPage() == null) {
                return 0;
            }

            return page.getPage();
        }

        public int getTotalPages() {
            if (page == null || page.getTotalPages() == null) {
                return 0;
            }

            return page.getTotalPages();
        }

        public void reset() {
            if (page != null) {
                page.setPage(0);
                page.setTotalPages(0);
                page.setProjectId(null);
                page.setContent(List.of());
            }
        }
    }

    private static class TrafficLightRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setIcon(new SecHubServerPanel.TrafficLightRenderer.TrafficLightIcon(value));
            return label;
        }

        static class TrafficLightIcon implements Icon {
            private final Object status;
            public TrafficLightIcon(Object status) { this.status = status; }
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                JBColor color;
                String statusString = String.valueOf(status);

                switch (statusString) {
                    case "GREEN" -> color = JBColor.GREEN;
                    case "YELLOW" -> color = JBColor.YELLOW;
                    case "RED" -> color = JBColor.RED;
                    default -> color = JBColor.GRAY;
                }

                g.setColor(color);
                g.fillOval(x + 4, y + 4, 12, 12);
                g.setColor(JBColor.DARK_GRAY);
                g.drawOval(x + 4, y + 4, 12, 12);
            }
            @Override public int getIconWidth() { return 20; }
            @Override public int getIconHeight() { return 20; }
        }
    }
}
