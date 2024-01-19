package io.jenkins.plugins.BaseRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jenkins.plugins.ProjectStats;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseRepoClass {
    private static Logger logger = LoggerFactory.getLogger(BaseRepoClass.class);
    protected String versionControl;
    protected String fullRepoURL;
    protected String key;

    public BaseRepoClass(String fullRepoURL, String key) {
        this.fullRepoURL = fullRepoURL;
        this.key = key;
    }

    public BaseRepoClass(String fullRepoURL) {
        this.fullRepoURL = fullRepoURL;
    }

    public abstract HttpURLConnection urlConnectionForCountingLines(String serverUrl) throws IOException;

    public HttpURLConnection urlConnectionForCountingLines(String localhostUrl, Long delay)
            throws InterruptedException, IOException {
        Thread.sleep(delay);
        return urlConnectionForCountingLines(localhostUrl);
    }

    public CompletableFuture<HttpResponse<String>> generatingReport(String urlForGeneratingReport, ProjectStats pj) {
        HttpClient httpClient = HttpClient.newHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(pj);
            //            objectMapper is an instance of the ObjectMapper class from the Jackson library.
            //            ObjectMapper is a versatile tool for converting between Java objects and JSON.

            //            writeValueAsString:
            //            This method of the ObjectMapper class is used to convert a Java object into its JSON
            // representation as a String.
            //
            //            pj: The writeValueAsString method takes this object and converts it into a JSON string
        } catch (JsonProcessingException e) {
            //            CompletableFuture Creation:
            //            A new instance of CompletableFuture<HttpResponse<String>> named failedFuture is created.
            //            CompletableFuture is a class in Java that represents a future result of an asynchronous
            // operation.

            //            completeExceptionally: The completeExceptionally method is called on failedFuture.
            //                    This method completes the CompletableFuture exceptionally with the specified exception
            // (e).
            //                    It essentially marks the future as completed with an exceptional result.

            //            Return: The completed failedFuture is returned immediately.
            //            This returned future is now completed exceptionally with the provided exception (e).
            CompletableFuture<HttpResponse<String>> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlForGeneratingReport))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();
        //        HttpRequest.newBuilder():
        //        This creates a new HttpRequest.Builder instance. The Builder is used to construct an HttpRequest.
        //
        //        .uri(URI.create(urlForGeneratingReport)):
        //        This sets the URI (Uniform Resource Identifier) for the request.
        //        The URI.create(urlForGeneratingReport) creates a URI object from the specified URL string
        // (urlForGeneratingReport).
        //
        //        .header("Content-Type", "application/json"):
        //        This sets the HTTP header for the request.
        //        In this case, it's setting the "Content-Type" header to "application/json".
        //        This indicates to the server that the body of the request will be in JSON format.
        //
        //        .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8)):
        //        This configures the HTTP method for the request, and in this case, it's set to a POST request.
        //        It also sets the request body using the ofString method of HttpRequest.BodyPublishers,
        //                and the body content is taken from the jsonBody variable.
        //                The StandardCharsets.UTF_8 specifies the character set encoding to be used when converting the
        // string to bytes.
        //
        //       .build(): This finalizes the construction of the HttpRequest by building an immutable instance of it.

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        //        sendAsync: This is a method of the HttpClient class used for sending an asynchronous HTTP request.
        //        It returns a CompletableFuture<HttpResponse<T>> where T is the response body type.
        //
        //        request: This is the HttpRequest object representing the HTTP request to be sent.
        //        It was created earlier using the HttpRequest.newBuilder() method.
        //
        //        HttpResponse.BodyHandlers.ofString():
        //        This part specifies the response body handler to be used for processing the response body.
        //        In this case, ofString() indicates that the response body should be converted to a String.
        //        The BodyHandlers class provides various predefined handlers for different response body types.
    }
}
