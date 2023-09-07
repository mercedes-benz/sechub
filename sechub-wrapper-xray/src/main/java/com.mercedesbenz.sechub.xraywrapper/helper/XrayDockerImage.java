package com.mercedesbenz.sechub.xraywrapper.helper;

public class XrayDockerImage {

    private String docker_name;

    private String docker_tag;

    private String sha256;

    public XrayDockerImage(String docker_name, String docker_tag, String sha256) {
        this.docker_name = docker_name;
        this.docker_tag = docker_tag;
        this.sha256 = sha256;
    }

    public String getDocker_name() {
        return docker_name;
    }

    public String getDocker_tag() {
        return docker_tag;
    }

    public String getSHA256() {
        return sha256;
    }
}
