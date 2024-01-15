package com.example.counting.numberoflines.DetailsFetcher;

import com.example.counting.numberoflines.methods.countLinesInTheFile;
import com.example.counting.numberoflines.methods.readContentFromTheFile;
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
import java.util.stream.Collectors;

public class GitHubFileFetcher {
    private GitHub github;
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

    public LinkedHashMap<String, Integer> numberOfLines(GHRepository repo) throws IOException, InterruptedException {
        LinkedHashMap<String, Integer> numberOfLines = new LinkedHashMap<>();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        System.out.println("Before finding java files: "+java.time.LocalTime.now());

        List<String> javaFiles = findJavaFiles(repo);
        System.out.println("After finding java files: "+java.time.LocalTime.now());

        for (String singleJavaFile : javaFiles) {
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
                String fileContent;
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

    private static List<String> findJavaFiles(GHRepository repo) throws IOException {
            List<String> allJavaFiles = new ArrayList<>();
            List<GHContent> contentList = repo.getDirectoryContent("");

            while(!contentList.isEmpty()){
                List<GHContent> javaFiles = contentList.stream()
                                            .filter(content -> !content.isDirectory() && content.getName().endsWith(".java"))
                                            .collect(Collectors.toList());
                allJavaFiles.addAll(javaFiles.stream().map(GHContent::getPath).collect(Collectors.toList()));

                contentList = contentList.stream().filter(GHContent::isDirectory).flatMap(content -> {
                    try {
                        return repo.getDirectoryContent(content.getPath()).stream();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
            }
            return allJavaFiles;
    }
}
