package com.example.counting.numberoflines.detailsfetcher;

import com.example.counting.numberoflines.methods.GetValuesFromConfigFile;
import com.example.counting.numberoflines.exceptions.FileReadingException;
import com.example.counting.numberoflines.methods.CountLinesInTheFile;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.RepositoryFile;
import org.gitlab4j.api.models.TreeItem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GitLabFileFetcher {
    GetValuesFromConfigFile getValuesFromConfigFile = new GetValuesFromConfigFile();
    public GitLabApi authenticate(String key) {
        return new GitLabApi("https://gitlab.com/", key);
    }

    public Project projectName(@NotNull GitLabApi gitLabApi, @NotNull String root) throws GitLabApiException {
        String[] parts = root.split("/");
        String owner = parts[parts.length - 2];
        String repoName = parts[parts.length - 1];
        return gitLabApi.getProjectApi().getProject(owner, repoName);
    }
public Map<String, Integer> fileAndLines(GitLabApi gitLabApi, Project project, String root, String branch) throws GitLabApiException, InterruptedException {
        CountLinesInTheFile countLinesInTheFile = new CountLinesInTheFile();
        Map<String, Integer> numberOfLines = new LinkedHashMap<>();
        List<TreeItem> fileTree = gitLabApi.getRepositoryApi().getTree(project.getId(), root, branch);
        List<String> javaFiles = new ArrayList<>();
        findJavaFiles(fileTree, gitLabApi, project, javaFiles, branch);

        ExecutorService executorService = Executors.newFixedThreadPool(20);
//  executorService is an instance of ExecutorService,
//  which is a higher-level interface for executing tasks asynchronously in Java.
//  It provides methods to manage and execute tasks in a thread pool.
//
//  The .submit() method is used to submit a task for execution.
//  It takes a Callable or Runnable object as an argument and
//  returns a Future object representing the result of the task or null if the task completed successfully.

            for(String singleJavaFile:javaFiles){
                executorService.submit(() -> {
                    RepositoryFile fileContent = null;
                    try {
                        fileContent = gitLabApi.getRepositoryFileApi().getFile(project.getId(), singleJavaFile, branch);
                    } catch (GitLabApiException e) {

                        throw new FileReadingException(getValuesFromConfigFile.getErrorMessageForFileReading(),e);
                    }
                    String fileContentInString = fileContent.getDecodedContentAsString();
                    int lines = countLinesInTheFile.countingLinesInTheFile(fileContentInString);
                    synchronized (numberOfLines) {
// The synchronized keyword in Java is used to create a block of code (or method)
// that can be accessed by only one thread at a time.
// It provides a way to prevent concurrent access to shared resources,
// ensuring that only one thread can execute the synchronized block of code at any given time.
// This helps in avoiding potential race conditions and maintaining data consistency.
                        numberOfLines.put(fileContent.getFileName(),lines);
                    }
                });
            }

            executorService.shutdown();
            executorService.awaitTermination(30, TimeUnit.SECONDS);
            return numberOfLines;
}

    private static void findJavaFiles(List<TreeItem> fileTree, GitLabApi gitLabApi, Project project, List<String> javaFiles, String branch) throws GitLabApiException {
        for(TreeItem treeItem:fileTree){
            if(treeItem.getType().equals(TreeItem.Type.BLOB) && treeItem.getName().endsWith(".java")){
                javaFiles.add(treeItem.getPath());
            }
            else if(treeItem.getType().equals(TreeItem.Type.TREE)){
                List<TreeItem> subTree = gitLabApi.getRepositoryApi().getTree(project.getId(),treeItem.getPath(),branch);
                findJavaFiles(subTree,gitLabApi,project,javaFiles,branch);
            }
        }
    }
}