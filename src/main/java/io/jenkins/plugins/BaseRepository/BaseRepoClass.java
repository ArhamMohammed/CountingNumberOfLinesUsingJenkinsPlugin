package io.jenkins.plugins.BaseRepository;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class BaseRepoClass {
    protected String versionControl;
    protected String fullRepoURL;
    protected String key;

    public BaseRepoClass(String fullRepoURL, String key) {
        this.fullRepoURL = fullRepoURL;
        this.key = key;
    }

    public abstract HttpURLConnection urlConnectionForCountingLines(String serverUrl) throws IOException;

    public HttpURLConnection urlConnectionForCountingLines(String localhostUrl, Long delay)
            throws InterruptedException, IOException {
        Thread.sleep(delay);
        return urlConnectionForCountingLines(localhostUrl);
    }

    public HttpURLConnection connectionForGeneratingReport(String urlForGeneratingReport) throws IOException {
        URL url = new URL(urlForGeneratingReport);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "*/*");
        connection.setDoOutput(true);
        return connection;
    }
}
