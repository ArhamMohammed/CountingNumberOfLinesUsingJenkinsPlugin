package io.jenkins.plugins.versioncontrol;

import io.jenkins.plugins.baserepository.BaseRepoClass;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class SvnRepoClass extends BaseRepoClass {
    protected String localDirectoryUrl;

    public SvnRepoClass(String fullRepoURL, String localDirectoryUrl) {
        super(fullRepoURL);
        versionControl = "svn";
        this.localDirectoryUrl = localDirectoryUrl;
    }

    @Override
    public HttpURLConnection urlConnectionForCountingLines(String serverUrl) throws IOException {
        URL url = new URL(serverUrl + "?versionControl=" + versionControl + "&root=" + fullRepoURL
                + "&localDirectoryUrl=" + localDirectoryUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        return connection;
    }
}
