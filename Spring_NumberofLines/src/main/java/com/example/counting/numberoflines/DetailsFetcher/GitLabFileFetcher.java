package com.example.counting.numberoflines.DetailsFetcher;

import com.example.counting.numberoflines.methods.countLinesInTheFile;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.RepositoryFile;
import org.gitlab4j.api.models.TreeItem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GitLabFileFetcher {

    public GitLabApi authenticate(String key) {
        GitLabApi gitLabApi = new GitLabApi("https://gitlab.com/", key);
        return gitLabApi;
    }

    public Project projectName(@NotNull GitLabApi gitLabApi, @NotNull String root) throws GitLabApiException {
        String[] parts = root.split("/");
        String owner = parts[parts.length - 2];
        String repoName = parts[parts.length - 1];
        return gitLabApi.getProjectApi().getProject(owner, repoName);
    }

//    public LinkedHashMap<String, Integer> numberOfLines(Project project, GitLabApi gitLabApi, String branchName) throws InterruptedException, GitLabApiException {
//        ExecutorService executorService = Executors.newFixedThreadPool(10);
//        List<TreeItem> files1 = gitLabApi.getRepositoryApi().getTree(project.getId(), "/", branchName);
//        LinkedHashMap<String, Integer> numberOfLines = fileAndLines(gitLabApi,project,files1,"/", branchName);
//
////            List<TreeItem> files1 = gitLabApi.getRepositoryApi().getTree(project.getId(),"/","main");
////            for (TreeItem treeItem : files1) {
////                if (treeItem.getType().equals("blob")) {  // Check if it's a file
////                    String filePath = treeItem.getPath();
//////                    String content = gitLabApi.getRepositoryFileApi().getRawFileContent(project.getId(), filePath, "main");
////                    String content = gitLabApi.getRepositoryFileApi().getRawFile(project.getId(),"/",filePath).toString();
////                    System.out.println("File Name: " + filePath);
////                    System.out.println("Content:\n" + content);
////                }
////                if(treeItem.getPath().equals("tree")){
////                    List<TreeItem> files2 = gitLabApi.getRepositoryApi().getTree(project.getId(),"/","main");
////                }
//////            System.out.println(files1);
////        }
//
////        gitLabApi.getRepositoryFileApi().getFile(project.getId(),"Test.java","main")
//
////        for (RepositoryFile file : files) {
////            if (file.getFileName().endsWith(".java")) {
////                executorService.submit(() -> {
////                    try {
////                        String fileContent = gitLabApi.getRepositoryFileApi().getFileContent(project.getId(), file.getFilePath(), null, null);
////                        int linesNumber = countLinesInTheFile.countLines(fileContent);
////                        synchronized (numberOfLines) {
////                            numberOfLines.put(file.getFileName(), linesNumber);
////                        }
////                    } catch (Exception e) {
////                        e.printStackTrace();
////                    }
////                });
////            }
////        }
//
//        executorService.shutdown();
//        executorService.awaitTermination(30, TimeUnit.SECONDS);
//        return numberOfLines;
//    }
//    public static LinkedHashMap<String, Integer> fileAndLines(GitLabApi gitLabApi, Project project, List<TreeItem> files1, String root, String branch) {
//        LinkedHashMap<String, Integer> numberOfLines = new LinkedHashMap<>();
//        LinkedHashMap<String, Integer> numberOfLinesFinal;
//
//        for (TreeItem treeItem : files1) {
//            if (treeItem.getType().equals(TreeItem.Type.TREE)) {
//                try {
//                    List<TreeItem> files2 = gitLabApi.getRepositoryApi().getTree(project.getId(), treeItem.getPath(), branch);
//                    numberOfLinesFinal = fileAndLines(gitLabApi, project, files2, treeItem.getPath(), branch);
//                    numberOfLines.putAll(numberOfLinesFinal);
//                } catch (GitLabApiException e) {
//                    throw new RuntimeException(e);
//                }
//            } else if (treeItem.getType().equals(TreeItem.Type.BLOB) && treeItem.getName().endsWith(".java")) {
//                try {
//                    RepositoryFile fileContent = gitLabApi.getRepositoryFileApi().getFile(project.getId(), treeItem.getPath(), branch);
//                    String fileContentInString = fileContent.getDecodedContentAsString();
//                    int lines = countLinesInTheFile.countLinesFromAString(fileContentInString);
//                    numberOfLines.put(fileContent.getFileName(),lines);
//                } catch (GitLabApiException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return numberOfLines;
//    }
public static LinkedHashMap<String, Integer> fileAndLines(GitLabApi gitLabApi, Project project, String root, String branch) throws GitLabApiException, InterruptedException {
        LinkedHashMap<String, Integer> numberOfLines = new LinkedHashMap<>();
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
                        throw new RuntimeException(e);
                    }
                    String fileContentInString = fileContent.getDecodedContentAsString();
                    int lines = countLinesInTheFile.countLinesFromAString(fileContentInString);
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