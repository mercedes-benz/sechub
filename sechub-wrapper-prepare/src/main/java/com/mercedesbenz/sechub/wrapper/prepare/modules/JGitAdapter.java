package com.mercedesbenz.sechub.wrapper.prepare.modules;

import java.nio.file.Paths;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Component;

@Component
public class JGitAdapter {

    private void clone(String location, String uploadDirectory, boolean cloneWithoutHistory, String username, String password) {
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

    public void clonePublic(GitContext contextGit) {
        clone(contextGit.getLocation(), contextGit.getUploadDirectory(), contextGit.isCloneWithoutHistory(), null, null);
    }

    public void clonePrivate(GitContext contextGit, String username, String password) {
        clone(contextGit.getLocation(), contextGit.getUploadDirectory(), contextGit.isCloneWithoutHistory(), username, password);
    }
}