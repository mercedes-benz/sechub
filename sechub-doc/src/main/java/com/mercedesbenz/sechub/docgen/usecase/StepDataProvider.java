// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.usecase;

public interface StepDataProvider {
    public String getTitle();

    public int getNumber();

    public int[] getNext();

    public String getDescription();
}