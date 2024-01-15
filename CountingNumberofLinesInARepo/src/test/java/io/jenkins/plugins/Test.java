package io.jenkins.plugins;

import io.jenkins.plugins.VersionControl.GitHubRepoClass;
import io.jenkins.plugins.VersionControl.GitlabRepoClass;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Test {
    public static void main(String args[]) throws InterruptedException, IOException {
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

        final String fullRepoURLForSVN = "https://eu-subversion.assembla.com/svn/wissen%5ETest_SVN.Mir_Test/";

        GitHubRepoClass gitHubRepo =
                new GitHubRepoClass(fullRepoURLForGithub, authenticationKeyForGitHub, branchNameForGitHub);
        HttpURLConnection countingLinesConnectionForGitHub =
                gitHubRepo.urlConnectionForCountingLines(localhostUrlForCounting);
        CompletableFuture<ProjectStats> countingFutureForGitHub =
                CountingLinesClient.makeAsyncApiCallForCounting(countingLinesConnectionForGitHub);
        HttpURLConnection generatingReportConnectionForGitHub =
                gitHubRepo.connectionForGeneratingReport(apiUrlForGeneratingReport);

        GitlabRepoClass gitlabRepoClass =
                new GitlabRepoClass(fullRepoURLForGitlab, authenticationKeyForGitLab, branchNameForGitLab);
        HttpURLConnection countingLinesConnectionForGitLab =
                gitlabRepoClass.urlConnectionForCountingLines(localhostUrlForCounting);
        CompletableFuture<ProjectStats> countingFutureForGitLab =
                CountingLinesClient.makeAsyncApiCallForCounting(countingLinesConnectionForGitLab);
        HttpURLConnection generatingReportConnectionForGitLab =
                gitlabRepoClass.connectionForGeneratingReport(apiUrlForGeneratingReport);
        //        System.out.println("Started counting future 2 at " + java.time.LocalTime.now());
        //        CompletableFuture<ProjectStats> countingFuture2 = CountingLinesClient.makeAsyncApiCallForCounting(
        //                apiUrlForCounting, rootParameterValueForCounting, authenticationKey, 100L);
        //        countingFuture2.join();
        //        ProjectStats pj2 = countingFuture2.join();
        //        ProjectStats pj1 = countingFuture1.join();
        //        countingFuture1.join();
        countingFutureForGitHub
                .thenCompose(pj -> CountingLinesClient.makeAsyncApiCallForGeneratingReport(
                        generatingReportConnectionForGitHub, pj))
                .thenAccept(report -> {
                    if (report != null) {
                        try {
                            System.out.println("The value of counting lines: "
                                    + countingFutureForGitHub.get().getNumberOfLines());
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                        System.out.println("Received Report from Github URL: " + report);
                    } else {
                        System.out.println("Failed to fetch Report");
                    }
                });
        countingFutureForGitLab
                .thenCompose(pj -> CountingLinesClient.makeAsyncApiCallForGeneratingReport(
                        generatingReportConnectionForGitLab, pj))
                .thenAccept(report -> {
                    if (report != null) {
                        try {
                            System.out.println("The value of counting lines: "
                                    + countingFutureForGitLab.get().getNumberOfLines());
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                        System.out.println("Received Report from Gitlab URl: " + report);
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
        Thread.sleep(2 * 60 * 1000);
    }
}
