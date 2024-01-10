package com.example.counting.numberoflines.DetailsFetcher;

import com.example.counting.numberoflines.methods.countLinesInTheFile;
import com.example.counting.numberoflines.methods.readContentFromTheFile;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.TreeItem;
import org.json.JSONArray;
import org.json.JSONObject;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GitHubFileFetcher {
    private GitHub github;
    LinkedHashMap<String, Integer> numberOfLines = new LinkedHashMap<>();
    //TODO: Handle null cases, handle error cases, handle error token cases.

    public GitHub authenticate(String key) throws IOException {
        github = GitHub.connectUsingOAuth(key);
        return github;
    }

    public GHRepository repo(GitHub github, String root) throws IOException {
        String[] parts = root.split("/");
        String owner = parts[parts.length - 2];
        String repoName = parts[parts.length - 1];

        GHRepository repo = github.getRepository(owner + "/" + repoName);
        return repo;
    }

    public LinkedHashMap<String,Integer> numberOfLines(GHRepository repo) throws IOException, InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<String> javaFiles = new ArrayList<>();
        findJavaFiles(repo.getDirectoryContent(""), repo, javaFiles);

            for(String singleJavaFile:javaFiles){
            GHContent content = repo.getFileContent(singleJavaFile);
                executorService.submit(() -> {
//  executorService is an instance of ExecutorService,
//  which is a higher-level interface for executing tasks asynchronously in Java.
//  It provides methods to manage and execute tasks in a thread pool.
//
//  The .submit() method is used to submit a task for execution.
//  It takes a Callable or Runnable object as an argument and
//  returns a Future object representing the result of the task or null if the task completed successfully.
//                    try {
                    String fileContent = null;
                    try {
                        fileContent = readContentFromTheFile.readContent(content.read());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                        int linesNumber = countLinesInTheFile.countLinesFromAString(fileContent);
                        synchronized (numberOfLines) {
// The synchronized keyword in Java is used to create a block of code (or method)
// that can be accessed by only one thread at a time.
// It provides a way to prevent concurrent access to shared resources,
// ensuring that only one thread can execute the synchronized block of code at any given time.
// This helps in avoiding potential race conditions and maintaining data consistency.
                            numberOfLines.put(content.getName(), linesNumber);
                        }
                    });
        }
        executorService.shutdown();
        executorService.awaitTermination(30, TimeUnit.SECONDS);
        return numberOfLines;
    }

    private static void findJavaFiles(List<GHContent> contentList, GHRepository repo, List<String> javaFiles) throws IOException {
        for (GHContent content : contentList) {
            if (content.isDirectory()) {
                List<GHContent> subTree = repo.getDirectoryContent(content.getPath());
                findJavaFiles(subTree, repo, javaFiles);
            } else if (content.getName().endsWith(".java")) {
                String fileContent = content.getPath();
                javaFiles.add(fileContent);
            }
        }
    }
}
