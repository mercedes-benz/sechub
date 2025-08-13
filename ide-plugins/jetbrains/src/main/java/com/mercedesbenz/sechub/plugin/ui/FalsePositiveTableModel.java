// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.ui;

import com.mercedesbenz.sechub.api.internal.gen.model.ScanType;

import static java.util.Objects.requireNonNull;

class FalsePositiveTableModel {
    private boolean isChecked;
    private final boolean isAlreadyMarkedAsFalsePositive;
    private final ScanType scanType;

    public FalsePositiveTableModel(boolean isAlreadyMarkedAsFalsePositive, ScanType scanType) {
        this.isAlreadyMarkedAsFalsePositive = isAlreadyMarkedAsFalsePositive;
        this.isChecked = isAlreadyMarkedAsFalsePositive;
        this.scanType = requireNonNull(scanType, "Property 'scanType' may not be null");
    }

    public FalsePositiveTableModel(boolean isAlreadyMarkedAsFalsePositive, boolean isChecked, ScanType scanType) {
        this.isAlreadyMarkedAsFalsePositive = isAlreadyMarkedAsFalsePositive;
        this.isChecked = isChecked;
        this.scanType = requireNonNull(scanType, "Property 'scanType' may not be null");
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean selected) {
        if (isAlreadyMarkedAsFalsePositive) {
            throw new IllegalStateException("Cannot change current value, because the false positive marking was initially true!");
        }
        this.isChecked = selected;
    }

    public boolean isAlreadyMarkedAsFalsePositive() {
        return isAlreadyMarkedAsFalsePositive;
    }

    public boolean setAlreadyMarkedAsFalsePositive(boolean alreadyMarkedAsFalsePositive) {
        if (alreadyMarkedAsFalsePositive) {
            this.isChecked = true;
        }
        return this.isAlreadyMarkedAsFalsePositive;
    }

    public ScanType getScanType() {
        return scanType;
    }
}
