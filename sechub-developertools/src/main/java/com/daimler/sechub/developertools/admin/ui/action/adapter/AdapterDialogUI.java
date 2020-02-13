package com.daimler.sechub.developertools.admin.ui.action.adapter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.daimler.sechub.developertools.admin.ui.UIContext;

public class AdapterDialogUI {

    private UIContext context;
    private String adapterId;
    private JPanel commonPanel;
    private JPanel mappingPanel;
    private JPanel otherPanel;
    private String[] mappingIdentifiers;

    public AdapterDialogUI(UIContext context, String adapterId, String ...mappingIdentifiers){
        this.context=context;
        this.adapterId=adapterId;
        this.mappingIdentifiers=mappingIdentifiers;
    }
    
    UIContext getContext() {
        return context;
    }

    public void showDialog() {
        JDialog dialog = new JDialog(context.getFrame());
        dialog.setLayout(new BorderLayout());
        
        JTabbedPane mainTabPane = new JTabbedPane();
        
        commonPanel = new JPanel();
        mappingPanel = new JPanel(new BorderLayout());
        otherPanel = new JPanel();
        
        mainTabPane.add("common",commonPanel);
        mainTabPane.add("mapping",mappingPanel);
        mainTabPane.add("other",otherPanel);
        
        mainTabPane.setSelectedComponent(mappingPanel);
        
        fillMappingPanel();
        
        dialog.add(mainTabPane,BorderLayout.CENTER);
        JLabel label = new JLabel("Adapter:"+adapterId);
        dialog.add(label,BorderLayout.NORTH);
        
        dialog.setTitle("Adapter configuration");
        dialog.setModal(true);
        dialog.setSize(new Dimension(800,600));
        dialog.setLocationRelativeTo(context.getFrame());
        dialog.setVisible(true);
        dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
    }

    private void fillMappingPanel() {
        
        JTabbedPane mappingIdTabPane = new JTabbedPane();
        mappingIdTabPane.setTabPlacement(JTabbedPane.LEFT);
        
        for (String mappingId: mappingIdentifiers) {
            MappingUI part = new MappingUI(this, mappingId);
            mappingIdTabPane.add(part.getTitle(), part.getComponent());
        }
        
        mappingPanel.add(mappingIdTabPane);
        
    }
}
