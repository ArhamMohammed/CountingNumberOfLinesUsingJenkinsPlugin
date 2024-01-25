package com.example.counting.numberoflines.detailsfetcher;
import com.example.counting.numberoflines.methods.GetValuesFromConfigFile;
import com.example.counting.numberoflines.exceptions.FileReadingException;
import com.example.counting.numberoflines.exceptions.RepositoryContentException;
import com.example.counting.numberoflines.methods.CountLinesInTheFile;
import com.example.counting.numberoflines.methods.ReadContentFromTheFile;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GitHubFileFetcher {
    static GetValuesFromConfigFile getValuesFromConfigFile = new GetValuesFromConfigFile();
    public GitHub authenticate(String key) throws IOException {
        return GitHub.connectUsingOAuth(key);
    }

    public GHRepository repo(GitHub github, String root) throws IOException {
        String[] parts = root.split("/");
        String owner = parts[parts.length - 2];
        String repoName = parts[parts.length - 1];

        return github.getRepository(owner + "/" + repoName);
    }

    public Map<String, Integer> numberOfLines(GHRepository repo) throws IOException, InterruptedException {
        Map<String, Integer> numberOfLines = new LinkedHashMap<>();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountLinesInTheFile countLinesInTheFile = new CountLinesInTheFile();
        ReadContentFromTheFile readingContentFromTheFile = new ReadContentFromTheFile();

        List<String> javaFiles = findJavaFiles(repo);

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
                String fileContent;
                try {
                    fileContent = readingContentFromTheFile.readContent(content.read());
                } catch (IOException e) {
                    throw new FileReadingException(getValuesFromConfigFile.getErrorMessageForFileReading(),e);
                }
                int linesNumber = countLinesInTheFile.countingLinesInTheFile(fileContent);
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
                        .filter(content -> !content.isDirectory() && content.getName().endsWith(".java")).toList();
                allJavaFiles.addAll(javaFiles.stream().map(GHContent::getPath).toList());

                contentList = contentList.stream().filter(GHContent::isDirectory).flatMap(content -> {
                    try {
                        return repo.getDirectoryContent(content.getPath()).stream();
                    } catch (IOException e) {
                        throw new RepositoryContentException(getValuesFromConfigFile.getErrorMessageForDirectoryReading(),e);
                    }
                }).toList();
            }
//        The key problem is that .collect(Collectors.toList()) actually returns a mutable kind of List
//        while in the majority of cases unmodifiable lists are preferred.
//        Since Java 16 there is now a better variant to produce an unmodifiable list directly from a stream: Stream.toList().
            return allJavaFiles;
    }
}
