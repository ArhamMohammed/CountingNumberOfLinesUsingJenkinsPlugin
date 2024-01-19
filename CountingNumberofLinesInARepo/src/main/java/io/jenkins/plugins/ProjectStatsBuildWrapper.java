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
import io.jenkins.plugins.VersionControl.SvnRepoClass;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nonnull;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectStatsBuildWrapper extends BuildWrapper {
    public static final String PROJECT_NAME_VAR = "$PROJECT_NAME$";
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

    private static Logger logger = LoggerFactory.getLogger(ProjectStatsBuildWrapper.class);
    public static final String REPORT_TEMPLATE_PATH = "/stats.html";

    static final String localhostUrlForCounting = "http://localhost:9091/counting";
    static final String localhostUrlForCountingSvnRepository = "http://localhost:9091/countingForSVN";
    final String relativePath = "CloneCOde\\SVN_Assembla";
    final String localDirectoryUrlForSVNWithoutEncoding =
            System.getProperty("user.dir") + File.separator + relativePath;
    final String localDirectoryUrlForSVNWithEncoding =
            URLEncoder.encode(localDirectoryUrlForSVNWithoutEncoding, StandardCharsets.UTF_8.toString());
    static final String localhostUrlForGeneratingReport = "http://localhost:9091/generateReport";
    String versionControl = StringUtils.EMPTY;
    String authenticationKey = StringUtils.EMPTY;
    String root = StringUtils.EMPTY;
    String branchName = StringUtils.EMPTY;
    String encodedRoot = StringUtils.EMPTY;

    @DataBoundConstructor
    public ProjectStatsBuildWrapper() throws UnsupportedEncodingException {}
    // This is the constructor for the class. It is annotated with @DataBoundConstructor,
    // indicating that it should be used for data binding during the configuration of the Jenkins job.

    @Override
    public Environment setUp(AbstractBuild build, final Launcher launcher, BuildListener listener) {

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
                LinkedHashMap<String, String> repoDetails = extractRepoDetails(build, listener);
                root = repoDetails.get("root");
                encodedRoot = URLEncoder.encode(root, StandardCharsets.UTF_8.toString());
                versionControl = repoDetails.get("versionControl");
                branchName = repoDetails.get("branchName");
                authenticationKey = repoDetails.get("authenticationKey");
                HttpURLConnection connectionForCountingLines;
                CompletableFuture<HttpResponse<String>> report = null;
                if (versionControl.equalsIgnoreCase("github")) {

                    GitHubRepoClass gitHubRepo = new GitHubRepoClass(encodedRoot, authenticationKey, branchName);
                    connectionForCountingLines = gitHubRepo.urlConnectionForCountingLines(localhostUrlForCounting);

                    //                    if (connectionForCountingLines.getResponseCode() == HttpURLConnection.HTTP_OK)
                    // {
                    //                        logger.info("Connection Successfull for counting lines");
                    //                    } else {
                    //                        logger.error("Something wrong while connecting  for counting lines");
                    //                    }
                    CompletableFuture<ProjectStats> statisticsOfCountingLines =
                            CountingLinesClient.makeAsyncApiCallForCounting(connectionForCountingLines);
                    try {
                        report = gitHubRepo.generatingReport(
                                localhostUrlForGeneratingReport, statisticsOfCountingLines.get());
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }

                } else if (versionControl.equalsIgnoreCase("gitlab")) {
                    GitlabRepoClass gitlabRepo = new GitlabRepoClass(encodedRoot, authenticationKey, branchName);
                    connectionForCountingLines = gitlabRepo.urlConnectionForCountingLines(localhostUrlForCounting);

                    CompletableFuture<ProjectStats> statisticsOfCountingLines =
                            CountingLinesClient.makeAsyncApiCallForCounting(connectionForCountingLines);
                    try {
                        report = gitlabRepo.generatingReport(
                                localhostUrlForGeneratingReport, statisticsOfCountingLines.get());
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                } else if (versionControl.equalsIgnoreCase("svn")) {
                    SvnRepoClass svnRepoClass = new SvnRepoClass(encodedRoot, localDirectoryUrlForSVNWithEncoding);
                    connectionForCountingLines =
                            svnRepoClass.urlConnectionForCountingLines(localhostUrlForCountingSvnRepository);
                    if (connectionForCountingLines.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        logger.info("Connection Successfull for counting lines");
                    } else {
                        logger.error("Something wrong while connecting for counting lines");
                    }
                    CompletableFuture<ProjectStats> statisticsOfCountingLines =
                            CountingLinesClient.makeAsyncApiCallForCounting(connectionForCountingLines);
                    try {
                        report = svnRepoClass.generatingReport(
                                localhostUrlForGeneratingReport, statisticsOfCountingLines.get());
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }

                } else {
                    System.out.println("Wrong Version Control Used:" + versionControl);
                    return false;
                }
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
                    writer.write(report.get().body());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Thread.sleep(60 * 1000);
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
            //            The isApplicable method helps Jenkins decide whether a certain type of job
            //            (e.g., Freestyle project, Pipeline, Maven project)
            //            should allow the user to configure an instance of the extension point provided by your plugin.
            //            If the method returns false, your extension point won't be available for configuration in that
            // specific type of job.
            return true;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Construct project stats during build";
        }
    }

    public static LinkedHashMap<String, String> extractRepoDetails(AbstractBuild build, BuildListener listener)
            throws IOException, InterruptedException {
        EnvVars envVars = build.getEnvironment(listener);
        String key = build.getProject().getScm().getKey();
        LinkedHashMap<String, String> repoDetails = new LinkedHashMap<>();
        String[] parts = key.split(" ");

        if ("git".equalsIgnoreCase(parts[0])) {
            extractGitRepoDetails(parts[1], envVars, repoDetails);
        } else if ("svn".equalsIgnoreCase(parts[0])) {
            extractSvnRepoDetails(parts[1], repoDetails);
        } else {
            setDefaultRepoDetails(repoDetails);
        }

        return repoDetails;
    }

    private static void extractGitRepoDetails(
            String gitRepoDetails, EnvVars envVars, LinkedHashMap<String, String> repoDetails) {
        repoDetails.put("branchName", envVars.get("GIT_BRANCH"));
        repoDetails.put("root", envVars.get("GIT_URL"));

        for (String part : gitRepoDetails.split("/")) {
            if ("github.com".equalsIgnoreCase(part)) {
                repoDetails.put("versionControl", "github");
                repoDetails.put("authenticationKey", "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");
            } else if ("gitlab.com".equalsIgnoreCase(part)) {
                repoDetails.put("versionControl", "gitlab");
                repoDetails.put("authenticationKey", "glpat-n6bxZQDixKNu1u9D4CEW");
            }
        }
    }

    private static void extractSvnRepoDetails(String svnRepoDetails, LinkedHashMap<String, String> repoDetails) {
        repoDetails.put("versionControl", "svn");
        repoDetails.put("root", svnRepoDetails);
    }

    private static void setDefaultRepoDetails(LinkedHashMap<String, String> repoDetails) {
        repoDetails.put("versionControl", null);
    }
}
