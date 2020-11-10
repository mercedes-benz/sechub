// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.developertools.admin.ui.action.config.ExecutionProfileDialogUI;

public abstract class FailsafeAction extends AbstractAction{
        private static final long serialVersionUID = 1L;

        private static final Logger LOG = LoggerFactory.getLogger(ExecutionProfileDialogUI.class);


        public final void actionPerformed(ActionEvent e) {
            try {
                safeActionPerformed(e);
            }catch(Exception ex) {
                LOG.error("Action '{}' failed ({})", this.getValue(Action.NAME),getClass().getSimpleName(), ex);
            }
        }
        
        protected abstract void safeActionPerformed(ActionEvent e) ;
    }