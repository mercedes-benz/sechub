// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Code flow object. See <a href=
 * "https://docs.oasis-open.org/sarif/sarif/v2.1.0/os/sarif-v2.1.0-os.html#_Toc34317740">SARIF
 * 2.1.0 specification entry</a>
 * 
 * @author Albert Tregnaghi
 *
 */
public class CodeFlow {

    private Message message;

    private List<ThreadFlow> threadFlows;

    public CodeFlow() {
        threadFlows = new LinkedList<>();
    }

    public List<ThreadFlow> getThreadFlows() {
        return threadFlows;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, threadFlows);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CodeFlow other = (CodeFlow) obj;
        return Objects.equals(message, other.message) && Objects.equals(threadFlows, other.threadFlows);
    }
}
