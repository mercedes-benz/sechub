package com.mercedesbenz.sechub.wrapper.prepare.modules;

import java.nio.file.Paths;
import java.util.List;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Component;

@Component
public class JGitAdapter {

    public void clonePublicRepository(GitContext contextGit) {
        clone(contextGit.getLocation(), contextGit.getUploadDirectory(), contextGit.isCloneWithoutHistory(), null, null);
    }

    public void clonePrivateRepository(GitContext contextGit, String username, String password) {
        clone(contextGit.getLocation(), contextGit.getUploadDirectory(), contextGit.isCloneWithoutHistory(), username, password);
    }

    private void clone(String location, String uploadDirectory, boolean cloneWithoutHistory, String username, String password) {
        location = transformLocationToURL(location);
        CloneCommand command = Git.cloneRepository().setURI(location).setDirectory(Paths.get(uploadDirectory).toFile());

        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            command = command.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));
        }

        if (cloneWithoutHistory) {
            command = command.setDepth(1);
        }

        try (Git git = command.call()) {
        } catch (GitAPIException e) {
            throw new RuntimeException("Error while cloning from repository: " + location, e);
        }
    }

    private String transformLocationToURL(String location) {

        if (location.startsWith("https://")) {
            return location;
        }

        /* clone with password and username does only work with URL */
        String URLPrefix = "https://";
        List<String> prefixes = List.of("git@", "git://", "http://", "ssh://");
        for (String prefix : prefixes) {
            if (location.startsWith(prefix)) {
                if (prefix.equals("git@")) {
                    location = location.replace(":", "/");
                }
                location = location.replaceAll(prefix, URLPrefix);
                break;
            }
        }
        try {
            new java.net.URL(location);
        } catch (java.net.MalformedURLException e) {
            throw new IllegalArgumentException("Location is not a valid URL: " + location);
        }
        return location;
    }
}