package io.jenkins.plugins.VersionControl;

import io.jenkins.plugins.BaseRepository.BaseRepoClass;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class GitHubRepoClass extends BaseRepoClass {
    //    String owner, repoName;
    String branchName;

    public GitHubRepoClass(String fullRepoURL, String key, String branchName) {
        super(fullRepoURL, key);

        //        String[] parts = fullRepoURL.split("/");
        //        owner = parts[parts.length - 2];
        //        repoName = parts[parts.length - 1];
        versionControl = "github";
        this.branchName = branchName;
    }

    public HttpURLConnection urlConnectionForCountingLines(String serverUrl) throws IOException {
        URL url = new URL(
                serverUrl + "?versionControl=" + versionControl + "&root=" + fullRepoURL + "&branchName=" + branchName);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("key", key);
        return connection;
    }
}
