package io.jenkins.plugins;

import io.jenkins.plugins.VersionControl.GitHubRepoClass;
import io.jenkins.plugins.VersionControl.GitlabRepoClass;
import io.jenkins.plugins.VersionControl.SvnRepoClass;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Test {
    public static void main(String args[]) throws IOException, InterruptedException, ExecutionException {
        final String localhostUrlForCounting = "http://localhost:9091/counting";
        final String localhostUrlForCountingForSVN = "http://localhost:9091/countingForSVN";

        final String fullRepoURLForGithub = "https://github.com/ArhamMohammed/CountingNumberOfLinesUsingJenkinsPlugin";
        final String authenticationKeyForGitHub = "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";
        final String branchNameForGitHub = "origin/master";
        final String apiUrlForGeneratingReport = "http://localhost:9091/generateReport";

        final String apiUrlForCountingWrong = "http://localhost:9091/counting";
        final String rootParameterValueForCountingWrong =
                "https://github.com/ArhamMohammed/JenkinsPracticeWithTwoFiles";
        final String authenticationKeyWrong = "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1";
        final String apiUrlForGeneratingReportWrong = "http://localhost:9091/generateRepor";

        final String authenticationKeyForGitLab = "glpat-n6bxZQDixKNu1u9D4CEW";
        final String fullRepoURLForGitlab = "https://gitlab.com/ArhamWissen/countingnumberoflines";
        final String branchNameForGitLab = "origin/main";

        //        final String fullRepoURLForSVN = "https://eu-subversion.assembla.com/svn/wissen%5ETest_SVN.Mir_Test/";
        //        final String localDirectoryUrlSVN = "///C:/Users/User/Desktop/CloneCOde/SVN_Assembla";
        final String fullRepoURLForSVN = "https://eu-subversion.assembla.com/svn/wissen^Test_SVN.Mir_Test";
        final String fullRepoURLForSVNEncoded = URLEncoder.encode(fullRepoURLForSVN, StandardCharsets.UTF_8.toString());
        final String localDirectoryUrlSVN = "C:/Users/User/Desktop/CloneCOde/SVN_Assembla";
        final String localDirectoryUrlSVNEncoded =
                URLEncoder.encode(localDirectoryUrlSVN, StandardCharsets.UTF_8.toString());
        /* -------------------------------- GitHub ----------------------------- */
        GitHubRepoClass gitHubRepo =
                new GitHubRepoClass(fullRepoURLForGithub, authenticationKeyForGitHub, branchNameForGitHub);
        HttpURLConnection countingLinesConnectionForGitHub =
                gitHubRepo.urlConnectionForCountingLines(localhostUrlForCounting);
        CompletableFuture<ProjectStats> countingFutureForGitHub =
                CountingLinesClient.makeAsyncApiCallForCounting(countingLinesConnectionForGitHub);
        //        /* -------------------------------- GitLab ----------------------------- */
        GitlabRepoClass gitlabRepoClass =
                new GitlabRepoClass(fullRepoURLForGitlab, authenticationKeyForGitLab, branchNameForGitLab);
        HttpURLConnection countingLinesConnectionForGitLab =
                gitlabRepoClass.urlConnectionForCountingLines(localhostUrlForCounting);
        CompletableFuture<ProjectStats> countingFutureForGitLab =
                CountingLinesClient.makeAsyncApiCallForCounting(countingLinesConnectionForGitLab);
        /* -------------------------------- SVN Assembla ----------------------------- */
        SvnRepoClass svnRepoClass = new SvnRepoClass(fullRepoURLForSVNEncoded, localDirectoryUrlSVNEncoded);
        HttpURLConnection countingLinesConnectionForSVN =
                svnRepoClass.urlConnectionForCountingLines(localhostUrlForCountingForSVN);
        CompletableFuture<ProjectStats> countingFutureForSvn =
                CountingLinesClient.makeAsyncApiCallForCounting(countingLinesConnectionForSVN);

        countingFutureForGitHub
                .thenCompose(pj -> {
                    try {
                        return gitHubRepo.generatingReport(apiUrlForGeneratingReport, countingFutureForGitHub.get());
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                })
                .thenAccept(response -> {
                    if (response != null) {
                        System.out.println("Received Report from Github URL: " + response.body());
                    } else {
                        System.out.println("Failed to fetch Report");
                    }
                });
        countingFutureForGitLab
                .thenCompose(pj -> {
                    try {
                        return gitlabRepoClass.generatingReport(
                                apiUrlForGeneratingReport, countingFutureForGitLab.get());
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                })
                .thenAccept(response -> {
                    if (response != null) {
                        System.out.println("Received Report from GitLab URL: " + response.body());
                    } else {
                        System.out.println("Failed to fetch Report");
                    }
                });
        countingFutureForSvn
                .thenCompose(pj -> {
                    try {
                        return svnRepoClass.generatingReport(apiUrlForGeneratingReport, countingFutureForSvn.get());
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                })
                .thenAccept(response -> {
                    if (response != null) {
                        System.out.println("Received Report from SVN URL: " + response.body());
                    } else {
                        System.out.println("Failed to fetch Report");
                    }
                });
        //        countingFuture2
        //                .thenCompose(
        //                        pj ->
        // CountingLinesClient.makeAsyncApiCallForGeneratingReport(apiUrlForGeneratingReport, pj))
        //                .thenAccept(report -> {
        //                    if (report != null) {
        //                        System.out.println("Received Report for second: " + report);
        //                    } else {
        //                        System.out.println("Failed to fetch Report");
        //                    }
        //                });
        Thread.sleep(60 * 1000);
    }
}
