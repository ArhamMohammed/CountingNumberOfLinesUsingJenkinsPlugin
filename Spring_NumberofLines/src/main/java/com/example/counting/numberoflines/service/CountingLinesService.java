package com.example.counting.numberoflines.service;


import com.example.counting.numberoflines.methods.GetValuesFromConfigFile;
import com.example.counting.numberoflines.detailsfetcher.GitHubFileFetcher;
import com.example.counting.numberoflines.detailsfetcher.GitLabFileFetcher;
import com.example.counting.numberoflines.detailsfetcher.SVNFileFetcher;
import com.example.counting.numberoflines.model.ProjectStats;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.tmatesoft.svn.core.SVNException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class CountingLinesService {

    private CountingLinesService(){}
    private static final Logger logger = LoggerFactory.getLogger(CountingLinesService.class);
    static GetValuesFromConfigFile getValuesFromConfigFile = new GetValuesFromConfigFile();
    protected static Map<String, Integer> numberOfLines = new LinkedHashMap<>();
    public static ProjectStats buildStats(@NotNull String versionControl, @NotNull String root,
                                          @NotNull String key, @NotNull String branchName)
            throws IOException, InterruptedException, GitLabApiException {
        // This method takes a path representing the root of the project workspace, iterates through Java files,
        // counts the number of classes and lines in those files, and returns a ProjectStats object.
        int sum = 0;
        if(versionControl.equalsIgnoreCase("github"))
        {
            GitHubFileFetcher gitHubFileFetcher = new GitHubFileFetcher();

            GitHub github = gitHubFileFetcher.authenticate(key);
            GHRepository repo = gitHubFileFetcher.repo(github,root);
            numberOfLines = gitHubFileFetcher.numberOfLines(repo);
        }
        else if(versionControl.equalsIgnoreCase("gitlab")){
            GitLabFileFetcher gitLabFileFetcher = new GitLabFileFetcher();
            GitLabApi gitLabApi = gitLabFileFetcher.authenticate(key);
            Project project = gitLabFileFetcher.projectName(gitLabApi,root);

            numberOfLines = gitLabFileFetcher.fileAndLines(gitLabApi,project,"/", branchName);
        }

        for(Integer value:numberOfLines.values()){
            sum+=value;
        }
        numberOfLines.put("Total Number of Lines :",sum);
        return new ProjectStats(numberOfLines);
    }

    public static ProjectStats buildStats(@NotNull String versionControl,
                                          @NotNull String root,
                                          @NotNull String localDirectoryUrl)
            throws SVNException, InterruptedException {
        // This method takes a path representing the root of the project workspace, iterates through Java files,
        // counts the number of classes and lines in those files, and returns a ProjectStats object.
        int sum = 0;
        if(versionControl.equalsIgnoreCase("svn"))
        {
            SVNFileFetcher svnFileFetcher = new SVNFileFetcher();
            svnFileFetcher.cloneRepositoryForSVN(root, localDirectoryUrl);
            numberOfLines = svnFileFetcher.fileAndLines(localDirectoryUrl);
        }

        for(Integer value:numberOfLines.values()){
            sum+=value;
        }
        numberOfLines.put("Total Number of Lines :",sum);
        return new ProjectStats(numberOfLines);
    }


    public static String generateReport(String projectName, ProjectStats stats) {
        try {
            // This method generates an HTML report based on a template. It reads the template from the classpath,
            // replaces placeholders with actual values, and returns the resulting content as a string.

            // ByteArrayOutputStream is a class in Java that provides
            // an output stream where data is written into a byte array.
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            // ProjectStatsBuildWrapper.class.getResourceAsStream(REPORT_TEMPLATE_PATH) obtains an input stream
            // for reading the specified resource (REPORT_TEMPLATE_PATH) associated with the class ProjectStatsBuildWrapper.
            Resource resource = new ClassPathResource(getValuesFromConfigFile.getReportTemplatePathFromConfig());
            try (InputStream in = resource.getInputStream()) {
                // A byte array (buffer) of size 1024 is created to read data from the input stream in chunks.
                // The read variable stores the number of bytes read in each iteration.
                // The while loop reads data from the input stream into the buffer until the end of the stream is reached
                // (when read becomes negative).
                // The write method writes the read bytes from the buffer into the ByteArrayOutputStream.
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) >= 0) {
                    bOut.write(buffer, 0, read);
                }
            }
            String content = bOut.toString(StandardCharsets.UTF_8);
            // StringBuilder provides a mutable sequence of characters and is designed for
            // situations where strings are dynamically built or modified.
            // It allows you to efficiently append, insert, or delete characters from a sequence of characters.
            StringBuilder entriesHtml = new StringBuilder();
            for (Map.Entry<String, Integer> entry : stats.getNumberOfLines().entrySet()) {
                // append method is used to concatenate strings in a more efficient manner.
                // This helps avoid creating unnecessary intermediate string objects and
                // results in better performance, especially in situations where many string manipulations are performed.
                entriesHtml.append("<tr>");
                entriesHtml.append("<td>").append(entry.getKey()).append("</td>");
                entriesHtml.append("<td>").append(entry.getValue()).append("</td>");
                entriesHtml.append("</tr>");
            }
            content = content.replace("$PROJECT_NAME$", projectName);
            content = content.replace("$ENTRIES$", entriesHtml.toString());
            return content;
        }
        catch(IllegalArgumentException e){
            logger.info("The path to fetch the properties from a config file is null. Please check. {}",e.getLocalizedMessage());
            return null;
        }
        catch (Exception e){
            logger.info(getValuesFromConfigFile.getErrorMessageForGeneratingReport(),e);
            return null;
        }
    }
}