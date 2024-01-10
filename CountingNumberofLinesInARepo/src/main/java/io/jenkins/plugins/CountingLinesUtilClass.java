package io.jenkins.plugins;
//
// import java.io.*;
// import java.nio.charset.StandardCharsets;
// import java.util.*;
//
public class CountingLinesUtilClass {}
//    public static ProjectStats buildStats(String root) {
//        // This method takes a path representing the root of the project workspace, iterates through Java files,
//        // counts the number of classes and lines in those files, and returns a ProjectStats object.
//        Queue<File> toProcess = new LinkedList<>();
//        toProcess.add(new File(root));
//        int linesNumber = 0;
//        String classesName;
//
//        //  LinkedHashMap Maintains the order in which key-value pairs are inserted.
//        //  It is used when you need to get the keys back in the order they were inserted.
//        LinkedHashMap<String, Integer> numberOfLines = new LinkedHashMap<>();
//
//        while (!toProcess.isEmpty()) {
//            File file = toProcess.remove();
//            // is used to add all files and subdirectories within the current directory to the queue
//            // file.listFiles(): This method returns an array of File objects representing the files and
//            // directories contained in the specified directory.
//            // Arrays.asList(...): This method is used to convert the array of File objects into a List<File>.
//
//            if (file.isDirectory()) {
//                // This method adds all elements from the specified collection to the end of the queue.
//                // It effectively enqueues all files and subdirectories for further processing.
//                File[] files = file.listFiles();
//                if (files != null) {
//                    // This method adds all elements from the specified collection to the end of the queue.
//                    // It effectively enqueues all files and subdirectories for further processing.
//                    toProcess.addAll(Arrays.asList(files));
//                }
//            } else if (file.getName().endsWith(".java")) {
//                // If the current file is a directory, the line enqueues all files and subdirectories within that
//                // directory.
//                // If the current file is a Java file (ends with ".java"), it counts the lines in that file.
//                classesName = file.getName();
//                linesNumber = countLines(file);
//                numberOfLines.put(classesName, linesNumber);
//            }
//        }
//        return new ProjectStats(numberOfLines);
//    }
//
//    private static int countLines(File path) {
//        // This method counts the number of lines in a given file (FilePath).
//
//        // try statement: The code within the try block is responsible for reading lines from a file.
//        // It's important to properly close the resources (like file readers) to avoid potential resource leaks.
//        // The try-with-resources statement simplifies this process.
//
//        // This initializes a BufferedReader that reads text from a character-input stream,
//        // and it's wrapped around a FileReader which is used to read characters from a file.
//        // The BufferedReader provides efficient reading by buffering characters.
//
//        // This loop reads each line from the file using the readLine() method of the BufferedReader.
//
//        // The try-with-resources statement ensures that the BufferedReader is closed automatically
//        // when the try block is exited, even if an exception occurs within the block.
//        // This helps in resource management and avoids explicit close calls.
//
//        int lines = 0;
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"))) {
//            // new FileInputStream(path): This part creates a FileInputStream object,
//            // which is used for reading bytes from the specified file (path).
//
//            // new InputStreamReader(new FileInputStream(path), "UTF-8"):
//            // This part wraps the FileInputStream with an InputStreamReader.
//            // The InputStreamReader translates bytes into characters using a specified character encoding.
//            // In this case, "UTF-8" is used as the character encoding.
//
//            // new BufferedReader(new InputStreamReader(...)):
//            // This part wraps the InputStreamReader with a BufferedReader.
//            // The BufferedReader reads text from a character-input stream,
//            // buffering characters to provide efficient reading of characters, arrays, and lines.
//            //            while (reader.readLine() != null) {
//            //                lines++;
//            //            }
//            String line;
//            while ((line = reader.readLine()) != null) {
//                // Process each line or perform actions with the line
//                lines++;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return lines;
//    }
//
//    public static String generateReport(String projectName, ProjectStats stats) throws IOException {
//        // This method generates an HTML report based on a template. It reads the template from the classpath,
//        // replaces placeholders with actual values, and returns the resulting content as a string.
//
//        // ByteArrayOutputStream is a class in Java that provides
//        // an output stream where data is written into a byte array.
//        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
//        // ProjectStatsBuildWrapper.class.getResourceAsStream(REPORT_TEMPLATE_PATH) obtains an input stream
//        // for reading the specified resource (REPORT_TEMPLATE_PATH) associated with the class
// ProjectStatsBuildWrapper.
//        try (InputStream in =
//                ProjectStatsBuildWrapper.class.getResourceAsStream(ProjectStatsBuildWrapper.REPORT_TEMPLATE_PATH)) {
//            // A byte array (buffer) of size 1024 is created to read data from the input stream in chunks.
//            // The read variable stores the number of bytes read in each iteration.
//            // The while loop reads data from the input stream into the buffer until the end of the stream is reached
//            // (when read becomes negative).
//            // The write method writes the read bytes from the buffer into the ByteArrayOutputStream.
//            byte[] buffer = new byte[1024];
//            int read;
//            while ((read = in.read(buffer)) >= 0) {
//                bOut.write(buffer, 0, read);
//            }
//        }
//        String content = new String(bOut.toByteArray(), StandardCharsets.UTF_8);
//        // StringBuilder provides a mutable sequence of characters and is designed for
//        // situations where strings are dynamically built or modified.
//        // It allows you to efficiently append, insert, or delete characters from a sequence of characters.
//        StringBuilder entriesHtml = new StringBuilder();
//        for (Map.Entry<String, Integer> entry : stats.getNumberOfLines().entrySet()) {
//            // append method is used to concatenate strings in a more efficient manner.
//            // This helps avoid creating unnecessary intermediate string objects and
//            // results in better performance, especially in situations where many string manipulations are performed.
//            entriesHtml.append("<tr>");
//            entriesHtml.append("<td>").append(entry.getKey()).append("</td>");
//            entriesHtml.append("<td>").append(entry.getValue()).append("</td>");
//            entriesHtml.append("</tr>");
//        }
//        content = content.replace(ProjectStatsBuildWrapper.PROJECT_NAME_VAR, projectName);
//        content = content.replace("$ENTRIES$", entriesHtml.toString());
//        return content;
//    }
// }
