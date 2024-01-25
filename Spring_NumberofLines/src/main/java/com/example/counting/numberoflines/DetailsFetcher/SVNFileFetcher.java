package com.example.counting.numberoflines.detailsfetcher;

import com.example.counting.numberoflines.methods.CountLinesInTheFile;
import com.example.counting.numberoflines.methods.GetValuesFromConfigFile;
import com.example.counting.numberoflines.exceptions.FileReadingException;
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
    GetValuesFromConfigFile getValuesFromConfigFile = new GetValuesFromConfigFile();
    private static final Logger logger = LoggerFactory.getLogger(SVNFileFetcher.class);
    private final Object lock = new Object();

    public void cloneRepositoryForSVN(String repoURL, String targetFolder) throws SVNException {
        SVNURL svnurl = SVNURL.parseURIEncoded(repoURL);
        SVNUpdateClient updateClient = SVNClientManager.newInstance().getUpdateClient();

        try {
            long checkoutRevision = updateClient.doCheckout(
                    svnurl, new File(targetFolder), SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, false);

            if (checkoutRevision > 0) {
                logger.info(getValuesFromConfigFile.getSuccessfulCheckoutMessage(),checkoutRevision);
            } else {
                logger.error(getValuesFromConfigFile.getUnsuccessfulCheckoutMessage(),checkoutRevision);
            }
        } catch (SVNException e) {
            logger.error(getValuesFromConfigFile.getErrorMessageForCloningRepo(),e.getMessage());
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
        //        false: Indicates that the checkout should not force to overwrite of local changes.
    }

    public Map<String, Integer> fileAndLines(String targetFolder) throws InterruptedException {
        // This method takes a path representing the root of the project workspace, iterates through Java files,
        // counts the number of classes and lines in those files, and returns a ProjectStats object.
        Queue<File> toProcess = new LinkedList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        toProcess.add(new File(targetFolder));

        Map<String, Integer> numberOfLines = new LinkedHashMap<>();

        executorService.submit(() -> processFiles(toProcess,numberOfLines));
        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.SECONDS);
        return numberOfLines;
    }

    private void processFiles(Queue<File> toProcess, Map<String,Integer> numberOfLines){
        while (!toProcess.isEmpty()) {
            File file = toProcess.remove();
            // is used to add all files and subdirectories within the current directory to the queue
            // file.listFiles(): This method returns an array of File objects representing the files and
            // directories contained in the specified directory.
            // Arrays.asList(...): This method is used to convert the array of File objects into a List<File>.

            if (file.isDirectory()) {
                // This method adds all elements from the specified collection to the end of the queue.
                // It effectively enqueues all files and subdirectories for further processing.
                File[] files = file.listFiles();
                enqueueFiles(toProcess,files);

            } else if (file.getName().endsWith(".java")) {
                // If the current file is a directory, the line enqueues all files and subdirectories within that
                // directory.
                // If the current file is a Java file (ends with ".java"), it counts the lines in that file.
                countLinesAndPutInMap(file,numberOfLines);
            }
        }
    }

    private void enqueueFiles(Queue<File> toProcess, File[] files){
        if (files != null) {
            // This method adds all elements from the specified collection to the end of the queue.
            // It effectively enqueues all files and subdirectories for further processing.
            toProcess.addAll(Arrays.asList(files));
        }
    }

    private void countLinesAndPutInMap(File file, Map<String,Integer> numberOfLines){
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            CountLinesInTheFile countLinesInTheFile = new CountLinesInTheFile();
            String classesName;
            int linesNumber;
                classesName = file.getName();
                synchronized (lock) {
// The synchronized keyword in Java is used to create a block of code (or method)
// that can be accessed by only one thread at a time.
// It provides a way to prevent concurrent access to shared resources,
// ensuring that only one thread can execute the synchronized block of code at any given time.

// This helps in avoiding potential race conditions and maintaining data consistency.
// The issue here is that numberOfLines is a method parameter,
// and using it for synchronization might lead to unexpected behavior or race conditions
// in a multi-threaded environment.
// The purpose of synchronization is to ensure that only one thread can access the
// critical section of code at a time, preventing data inconsistency or corruption
// when multiple threads are involved.

// When you use a method parameter (like numberOfLines) for synchronization,
// you are essentially using the object that is passed to the method as a monitor for synchronization.
// However, this can lead to unintended consequences because different threads may use different instances of the object
// when invoking the method, and the synchronization won't work as expected.
// To address this issue, it's recommended to use a dedicated object (such as a private field) for synchronization within the method,
// rather than relying on a method parameter.
// lock is a dedicated object used for synchronization.
// It ensures that only one thread can execute the critical section inside the synchronized block at a time,
// regardless of which instance of the class is being used.
                    linesNumber = countLinesInTheFile.countingLinesInTheFile(reader);
                    numberOfLines.put(classesName, linesNumber);
                }
        } catch (IOException e) {
            throw new FileReadingException(getValuesFromConfigFile.getErrorMessageForFileReading(),e);
        }
    }
}
