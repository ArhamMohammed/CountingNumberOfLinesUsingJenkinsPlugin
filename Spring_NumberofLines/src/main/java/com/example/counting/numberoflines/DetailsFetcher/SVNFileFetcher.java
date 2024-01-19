package com.example.counting.numberoflines.DetailsFetcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SVNFileFetcher {
    private static Logger logger = LoggerFactory.getLogger(SVNFileFetcher.class);

    public void cloneRepositoryForSVN(String repoURL, String targetFolder) throws SVNException {
        SVNURL svnurl = SVNURL.parseURIEncoded(repoURL);
        SVNUpdateClient updateClient = SVNClientManager.newInstance().getUpdateClient();

        try {
            long checkoutRevision = updateClient.doCheckout(
                    svnurl, new File(targetFolder), SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, false);

            if (checkoutRevision > 0) {
                logger.info("Checkout successful. Working copy is now at revision: " + checkoutRevision);
            } else {
                logger.error("Checkout failed. The returned revision is not valid: " + checkoutRevision);
            }
        } catch (SVNException e) {
            System.err.println("Something wrong while cloning the repo : " + e.getMessage());
            logger.error("Something wrong while cloning the repo : " + e.getMessage());
        }
        //        SVNURL.parseURIEncoded(repoURL):
        //        Parses the repository URL (repoURL) into an SVNURL object.
        //        The repoURL should be a properly formatted SVN repository URL.
        //        This code is used to parse a string representation of an SVN repository URL (repoURL) into an SVNURL
        // object.
        //        This is necessary because SVNKit, the library being used here,
        //        expects an SVNURL object to represent the location of the SVN repository.
        //
        //        SVNClientManager.newInstance().getUpdateClient():
        //        Creates a new instance of SVNUpdateClient through the SVNClientManager.
        //        The SVNUpdateClient is used for updating (checking out or updating) working copies.
        //
        //        updateClient.doCheckout(...): Performs the checkout operation.
        //
        //        svnurl: The URL of the SVN repository.
        //        new File(targetFolder): The local directory where the repository will be checked out.
        //        SVNRevision.HEAD: Specifies that the checkout operation should retrieve the latest revision of the
        // repository.
        //        SVNDepth.INFINITY: Specifies that the checkout should include all child directories and files
        // recursively.
        //        false: Indicates that the checkout should not force the overwrite of local changes.
    }

    public LinkedHashMap<String, Integer> fileAndLines(String targetFolder) throws InterruptedException {
        // This method takes a path representing the root of the project workspace, iterates through Java files,
        // counts the number of classes and lines in those files, and returns a ProjectStats object.
        Queue<File> toProcess = new LinkedList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
            toProcess.add(new File(targetFolder));
//        toProcess.add(new File(targetFolder));

        //  LinkedHashMap Maintains the order in which key-value pairs are inserted.
        //  It is used when you need to get the keys back in the order they were inserted.
        LinkedHashMap<String, Integer> numberOfLines = new LinkedHashMap<>();

        executorService.submit(() -> {
        while (!toProcess.isEmpty()) {

            int linesNumber = 0;
            String classesName;
            File file = toProcess.remove();
            // is used to add all files and subdirectories within the current directory to the queue
            // file.listFiles(): This method returns an array of File objects representing the files and
            // directories contained in the specified directory.
            // Arrays.asList(...): This method is used to convert the array of File objects into a List<File>.

            if (file.isDirectory()) {
                // This method adds all elements from the specified collection to the end of the queue.
                // It effectively enqueues all files and subdirectories for further processing.
                File[] files = file.listFiles();
                if (files != null) {
                    // This method adds all elements from the specified collection to the end of the queue.
                    // It effectively enqueues all files and subdirectories for further processing.
                    toProcess.addAll(Arrays.asList(files));
                }
            } else if (file.getName().endsWith(".java")) {
                // If the current file is a directory, the line enqueues all files and subdirectories within that
                // directory.
                // If the current file is a Java file (ends with ".java"), it counts the lines in that file.
                try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                    while (reader.readLine() != null) {
                        linesNumber++;
                        classesName = file.getName();
                        synchronized (numberOfLines) {
// The synchronized keyword in Java is used to create a block of code (or method)
// that can be accessed by only one thread at a time.
// It provides a way to prevent concurrent access to shared resources,
// ensuring that only one thread can execute the synchronized block of code at any given time.
// This helps in avoiding potential race conditions and maintaining data consistency.
                            numberOfLines.put(classesName, linesNumber);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        });
        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.SECONDS);
        return numberOfLines;
    }
}
