package com.mercedesbenz.sechub.plugin.ui;

class FalsePositiveTableModel {
    private boolean isChecked;
    private final boolean isAlreadyMarkedAsFalsePositive;

    public FalsePositiveTableModel(boolean isAlreadyMarkedAsFalsePositive) {
        this.isAlreadyMarkedAsFalsePositive = isAlreadyMarkedAsFalsePositive;
        this.isChecked = isAlreadyMarkedAsFalsePositive;
    }

    public FalsePositiveTableModel(boolean isAlreadyMarkedAsFalsePositive, boolean isChecked) {
        this.isAlreadyMarkedAsFalsePositive = isAlreadyMarkedAsFalsePositive;
        this.isChecked = isChecked;
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
}
