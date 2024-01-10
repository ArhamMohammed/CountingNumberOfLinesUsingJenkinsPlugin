package io.jenkins.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CountingLinesClient {
    private static Logger logger = LoggerFactory.getLogger(CountingLinesClient.class);

    //    you typically provide the name of the class for which you are creating the logger.
    //    in the getLogger() method for best logging practices.
    public static ProjectStats makeApiCallForCounting(String urlPassed, String root, String key) throws IOException {
        URL url = new URL(urlPassed + "?root=" + root + "&key=" + key);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int response = connection.getResponseCode();
        System.out.println("Response Code: " + response);

        ProjectStats pj = null;
        if (response == HttpURLConnection.HTTP_OK) {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader =
                    new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                // getInputStream() triggers the execution of the HTTP request if it hasn't already been executed,
                // and it returns the input stream for reading the response body.
                // The .execute() method is a more explicit way to execute the request and
                // obtain the response code but is not required when using getInputStream().
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String jsonResponse = sb.toString();
            ObjectMapper objectMapper = new ObjectMapper();

            pj = objectMapper.readValue(jsonResponse, ProjectStats.class);
        } else {
            System.out.println("There has been error");
        }
        return pj;
    }

    public static String makeApiCallForGeneratingReport(String urlPassed, ProjectStats pj) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String projectStatsJson = objectMapper.writeValueAsString(pj);

        URL url = new URL(urlPassed);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // Set request method to POST
        connection.setRequestMethod("POST");

        // Set content type to JSON
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "*/*");
        // Enable output
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = projectStatsJson.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int response = connection.getResponseCode();
        System.out.println("Response Code: " + response);

        if (response == HttpURLConnection.HTTP_OK) {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader =
                    new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            return sb.toString();
        } else {
            return "Error sending a request";
        }
    }

    public static CompletableFuture<ProjectStats> makeAsyncApiCallForCounting(
            HttpURLConnection connectionForCountingLines) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                //                if (urlPassed.equals(StringUtils.EMPTY)) {
                //                    //                    logger.debug("The protocol is empty. Please check");
                //                    //                    logger.debug("Protocol: "+urlPassed);
                //                    System.out.println("The protocol is empty. Please check");
                //                    System.out.println("Protocol: " + urlPassed);
                //                    return null;
                //                } else if (root.equals(StringUtils.EMPTY)) {
                //                    //                    logger.debug("The root is empty. Please check");
                //                    //                    logger.debug("Root: "+root);
                //                    System.out.println("The root is empty. Please check");
                //                    System.out.println("Root: " + root);
                //                    return null;
                //                } else if (key.equals(StringUtils.EMPTY)) {
                //                    logger.debug("Please check the key provided, as it is EMPTY");
                //                    System.out.println("Please check the key provided, as it is EMPTY");
                //                    return null;
                //                }
                int response = connectionForCountingLines.getResponseCode();
                //                logger.debug("Response Code for counting: " + response);
                //                System.out.println("Response Code for counting: " + response);
                ProjectStats pj = null;
                if (response == HttpURLConnection.HTTP_OK) {
                    String jsonResponse;
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                            connectionForCountingLines.getInputStream(), StandardCharsets.UTF_8))) {
                        jsonResponse = IOUtils.toString(reader);
                    }
                    ObjectMapper objectMapper = new ObjectMapper();
                    pj = objectMapper.readValue(jsonResponse, ProjectStats.class);
                }
                //                else if (response == 404 || response == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                //                    logger.debug("Please check the complete URL provided ");
                //                    logger.debug("Protocol: " + urlPassed);
                //                    logger.debug("Root: " + root);
                //                    System.out.println(
                //                            "The resource which you have provided is unavailable. Please check the URL
                // provided ");
                //                    System.out.println("Protocol: " + urlPassed);
                //                    System.out.println("Root: " + root);
                //                } else {
                //                    logger.debug("Error occurred " + url);
                //                    System.out.println("Error occurred " + url);
                //                }
                //                Thread.sleep(delay);
                //                System.out.println(pj.getNumberOfLines());
                return pj;
            } catch (MalformedURLException e) {
                System.out.println("Protocol is missing from the URL ");
                System.out.println(e.getLocalizedMessage());
            } catch (Exception e) {
                System.out.println("The exception occurred is :" + e);
            }
            return null;
        });
    }

    public static CompletableFuture<String> makeAsyncApiCallForGeneratingReport(
            HttpURLConnection connectionForGeneratingReport, ProjectStats pj) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String projectStatsJson = objectMapper.writeValueAsString(pj);

                //                URL url = new URL(urlPassed);
                //                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                //
                //                connection.setRequestMethod("POST");
                //                connection.setRequestProperty("Content-Type", "application/json");
                //                connection.setRequestProperty("Accept", "*/*");
                //                connection.setDoOutput(true);
                //                UrlConnectionForGeneratingReport generateReport = new
                // UrlConnectionForGeneratingReport();
                //                HttpURLConnection connection =
                // generateReport.connectionForGeneratingReport(urlPassed);

                try (OutputStream os = connectionForGeneratingReport.getOutputStream()) {
                    byte[] input = projectStatsJson.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int response = connectionForGeneratingReport.getResponseCode();
                System.out.println("Response Code for generating report: " + response);

                if (response == HttpURLConnection.HTTP_OK) {
                    StringBuilder sb = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                            connectionForGeneratingReport.getInputStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                    }
                    //                    Thread.sleep(5000);
                    return sb.toString();
                } else {
                    return "Error sending a request for generating report";
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to make API call for counting", e);
            }
        });
    }
}
