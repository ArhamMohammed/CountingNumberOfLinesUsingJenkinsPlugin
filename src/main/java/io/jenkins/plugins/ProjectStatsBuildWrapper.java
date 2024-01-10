package io.jenkins.plugins;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import io.jenkins.plugins.VersionControl.GitHubRepoClass;
import io.jenkins.plugins.VersionControl.GitlabRepoClass;
import java.io.*;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nonnull;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

public class ProjectStatsBuildWrapper extends BuildWrapper {
    // This class extends BuildWrapper, which is part of the Jenkins extension
    // points for build process customization.
    //    static:
    //    When a member (either a variable or a method) of a class is declared as static,
    //    it belongs to the class itself rather than to any specific instance of the class.
    //    This means that there is only one copy of the static member in memory,
    //    regardless of how many instances of the class are created.
    //    static members can be accessed directly using the class name without needing an instance of the class.

    //    final:
    //    When a variable is declared as final, it means that once the variable is assigned a value,
    //    it cannot be changed or reassigned. It becomes a constant.
    //    For methods, final means that the method cannot be overridden by subclasses.
    //    For classes, final means that the class cannot be subclassed (i.e., cannot be extended).

    public static final String REPORT_TEMPLATE_PATH = "/stats.html";

    static final String localhostUrlForCounting = "http://localhost:9091/counting";
    String versionControl = StringUtils.EMPTY;
    static final String authenticationKeyForGitHub = "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";
    static final String authenticationKeyForGitLab = "glpat-n6bxZQDixKNu1u9D4CEW";

    static final String localhostUrlForGeneratingReport = "http://localhost:9091/generateReport";
    String root = StringUtils.EMPTY;
    String branchName = StringUtils.EMPTY;
    //    HttpURLConnection connectionForCountingLines = null;
    //    HttpURLConnection connectionForGeneratingReport = null;

    // These are constants defining paths and placeholders used in the code.

    @DataBoundConstructor
    public ProjectStatsBuildWrapper() {}
    // This is the constructor for the class. It is annotated with @DataBoundConstructor,
    // indicating that it should be used for data binding during the configuration of the Jenkins job.

    @Override
    public Environment setUp(AbstractBuild build, final Launcher launcher, BuildListener listener)
            throws IOException, InterruptedException {
        EnvVars envVars = build.getEnvironment(listener);
        root = envVars.get("GIT_URL");
        branchName = envVars.get("GIT_BRANCH");
        versionControl = extractVersionControlName(root);

        // This method is part of the BuildWrapper extension point. It sets up the environment for the build.
        // Inside the setUp method, an anonymous subclass of Environment is created with a customized tearDown method.
        // This tearDown method is responsible for generating project statistics, creating an HTML report,
        // and saving it to the artifacts' directory.
        // This method is overridden from the BuildWrapper class and is called at the beginning of the build process.
        // It returns an instance of an anonymous subclass of the Environment class.
        return new Environment() {
            @Override
            public boolean tearDown(AbstractBuild build, BuildListener listener)
                    throws IOException, InterruptedException {
                // This method is called at the end of the build process.
                // It is responsible for generating project statistics, creating an HTML report,
                // and saving it to the artifacts' directory.
                //                boolean useAsync = false;
                //                if(useAsync){
                //                    ProjectStats stats1 =
                //                            CountingLinesClient.makeApiCallForCountingAsync(apiUrlForCounting, root,
                // authenticationKey);
                //                    ProjectStats stats2 =
                //                            CountingLinesClient.makeApiCallForCountingAsync(apiUrlForCounting, root,
                // authenticationKey);
                //                }
                //                else{
                //                ProjectStats stats1 =
                //                            CountingLinesClient.makeApiCallForCounting(apiUrlForCounting, root,
                // authenticationKey);
                //                ProjectStats stats2 =
                //                        CountingLinesClient.makeApiCallForCounting(apiUrlForCounting, root,
                // authenticationKey);
                //                }
                // String report =
                // CountingLinesUtilClass.generateReport(build.getProject().getDisplayName(), stats);

                //                ProjectStats stats1 =
                //                            CountingLinesClient.makeApiCallForCounting(apiUrlForCounting, root,
                // authenticationKey);
                //                String report =
                // CountingLinesClient.makeApiCallForGeneratingReport(apiUrlForGeneratingReport, stats1);
                HttpURLConnection connectionForCountingLines = null, connectionForGeneratingReport = null;
                if (versionControl.equalsIgnoreCase("github")) {

                    GitHubRepoClass gitHubRepo = new GitHubRepoClass(root, authenticationKeyForGitHub, branchName);
                    connectionForCountingLines = gitHubRepo.urlConnectionForCountingLines(localhostUrlForCounting);

                    connectionForGeneratingReport =
                            gitHubRepo.connectionForGeneratingReport(localhostUrlForGeneratingReport);

                } else if (versionControl.equalsIgnoreCase("gitlab")) {
                    GitlabRepoClass gitlabRepo = new GitlabRepoClass(root, authenticationKeyForGitLab, branchName);
                    connectionForCountingLines = gitlabRepo.urlConnectionForCountingLines(localhostUrlForCounting);

                    connectionForGeneratingReport =
                            gitlabRepo.connectionForGeneratingReport(localhostUrlForGeneratingReport);
                }

                CompletableFuture<ProjectStats> statisticsOfCountingLines =
                        CountingLinesClient.makeAsyncApiCallForCounting(connectionForCountingLines);
                CompletableFuture<String> report, report2 = null;
                try {
                    report = CountingLinesClient.makeAsyncApiCallForGeneratingReport(
                            connectionForGeneratingReport, statisticsOfCountingLines.get());
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }

                //                CompletableFuture<ProjectStats> stats2 =
                // CountingLinesClient.makeAsyncApiCallForCounting(
                //                        versionControl, apiUrlForCounting, root, authenticationKey, branchName, 100L);
                //                try {
                //                    report2 = CountingLinesClient.makeAsyncApiCallForGeneratingReport(
                //                            apiUrlForGeneratingReport, stats2.get());
                //                } catch (ExecutionException e) {
                //                    throw new RuntimeException(e);
                //                }

                File artifactsDir = build.getArtifactsDir();
                if (!artifactsDir.isDirectory()) {
                    boolean success = artifactsDir.mkdirs();
                    if (!success) {
                        listener.getLogger()
                                .println("Can't create artifacts directory at " + artifactsDir.getAbsolutePath());
                    }
                }
                String path = artifactsDir.getCanonicalPath() + REPORT_TEMPLATE_PATH;
                try (BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8))) {
                    writer.write(report.join());
                    //                    writer.write(report2.join());
                }
                //                Thread.sleep(5000);
                return super.tearDown(build, listener);
            }
        };
    }

    @Extension
    public static final class DescriptorImpl extends BuildWrapperDescriptor {
        // This is an extension point descriptor for the build wrapper.
        // It provides information about the build wrapper, such as its display name and
        // whether it is applicable to a specific project.
        // isApplicable Method: Determines if the build wrapper is applicable to a given project.
        // getDisplayName Method: Returns the display name of the build wrapper.

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Construct project stats during build";
        }
    }

    public static String extractVersionControlName(String fullRepoURL) {
        String[] parts = fullRepoURL.split("/");

        for (String part : parts) {
            if ("github.com".equalsIgnoreCase(part)) {
                return "github";
            } else if ("gitlab.com".equalsIgnoreCase(part)) {
                return "gitlab";
            }
        }
        return "The provided url is wrong. Please check whether the provided url is of Github or Gitlab";
    }
}
