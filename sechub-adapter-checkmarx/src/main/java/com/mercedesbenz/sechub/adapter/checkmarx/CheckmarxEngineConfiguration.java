// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.checkmarx;

import java.util.Objects;

public class CheckmarxEngineConfiguration {
    private Long id;
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "CheckmarxEngineConfiguration [id=" + id + ", name=" + name + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CheckmarxEngineConfiguration other = (CheckmarxEngineConfiguration) obj;
        return Objects.equals(id, other.id) && Objects.equals(name, other.name);
    }
}
