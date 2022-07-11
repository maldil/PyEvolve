package com.utils;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;

public class GitUtils {
    public static Repository connect(String url) {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository=null;
        try {
            repository = builder.setGitDir(new File(url+ "/.git")).readEnvironment() // scan
                    // environment
                    // GIT_*
                    // variables
                    .findGitDir() // scan up the file system tree
                    .build();

        } catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        }
        try {
            if (repository.getBranch() == null)
                return null;
        } catch (IOException e) {
            return null;
        }
        return repository;
    }

    public static String getBranch(Repository repo){
        try {
            return repo.getBranch();
        } catch (  IOException | NullPointerException e) {
            return "";
        }
    }

}
