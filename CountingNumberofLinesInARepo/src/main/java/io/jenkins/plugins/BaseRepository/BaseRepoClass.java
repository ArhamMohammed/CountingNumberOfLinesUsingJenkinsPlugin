package io.jenkins.plugins.baserepository;

import java.io.IOException;
import java.net.HttpURLConnection;

public abstract class BaseRepoClass {
    protected String versionControl;
    protected String fullRepoURL;
    protected String key;

    protected BaseRepoClass(String fullRepoURL, String key) {
        this.fullRepoURL = fullRepoURL;
        this.key = key;
    }

    protected BaseRepoClass(String fullRepoURL) {
        this.fullRepoURL = fullRepoURL;
    }

    public abstract HttpURLConnection urlConnectionForCountingLines(String serverUrl) throws IOException;

    public HttpURLConnection urlConnectionForCountingLines(String localhostUrl, Long delay)
            throws InterruptedException, IOException {
        Thread.sleep(delay);
        return urlConnectionForCountingLines(localhostUrl);
    }
}
