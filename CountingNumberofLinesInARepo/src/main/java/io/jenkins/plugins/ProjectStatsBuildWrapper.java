package io.jenkins.plugins;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import io.jenkins.plugins.exceptions.ReportGenerationException;
import io.jenkins.plugins.generatereport.GenerateReport;
import io.jenkins.plugins.versioncontrol.GitHubRepoClass;
import io.jenkins.plugins.versioncontrol.GitlabRepoClass;
import io.jenkins.plugins.versioncontrol.SvnRepoClass;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nonnull;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    static Logger logger = LoggerFactory.getLogger(ProjectStatsBuildWrapper.class);
    private static final String LOCALHOST_URL_FOR_COUNTING = "http://localhost:9091/counting";
    private static final String LOCALHOST_URL_FOR_COUNTING_SVN_REPOSITORY = "http://localhost:9091/countingForSVN";
    private static final String LOCALHOST_URL_FOR_GENERATING_REPORT = "http://localhost:9091/generateReport";
    private static final Long DELAY = 60000L;

    private static final String VERSION_CONTROL_KEY = "versionControl";
    private static final String AUTHENTICATION_KEY = "authenticationKey";
    private static final String BRANCH_NAME = "branchName";
    private static final String ROOT_KEY = "root";
    private static final String GITHUB = "github";
    private static final String GITLAB = "gitlab";
    private static final String SVN = "svn";
    private static final String GITHUB_KEY = "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";
    private static final String GITLAB_KEY = "glpat-n6bxZQDixKNu1u9D4CEW";
    private static final String RELATIVE_PATH = "CloneCOde\\SVN_Assembla";
    private static final String CONNECTION_SUCCESSFUL_MESSAGE = "Connection Successfully made for counting lines";
    private static final String CONNECTION_UNSUCCESSFUL_MESSAGE = "Something wrong while connecting for counting lines";
    String localDirectoryUrlForSvnWithEncoding = encodeUrl();
    String versionControl = StringUtils.EMPTY;
    String authenticationKey = StringUtils.EMPTY;
    String root = StringUtils.EMPTY;
    String branchName = StringUtils.EMPTY;
    String encodedRoot = StringUtils.EMPTY;

    @DataBoundConstructor
    public ProjectStatsBuildWrapper() {
        // This constructor is provided to satisfy the data binding requirements of Jenkins.
        // The actual initialization and setup are performed in the overridden setUp method.
        // If additional setup is needed, it should be performed in the setUp method instead of this constructor.
    }
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
                try {
                    setupEnvironment(build, listener);
                    Thread.sleep(DELAY);
                } catch (ExecutionException e) {
                    throw new ReportGenerationException("Error during generating report", e);
                }
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

    private void setupEnvironment(AbstractBuild<?, ?> build, BuildListener listener)
            throws IOException, InterruptedException, ExecutionException {
        Map<String, String> repoDetails = extractRepoDetails(build, listener);
        root = repoDetails.get(ROOT_KEY);
        encodedRoot = URLEncoder.encode(root, StandardCharsets.UTF_8);
        versionControl = repoDetails.get(VERSION_CONTROL_KEY);
        branchName = repoDetails.get(BRANCH_NAME);
        authenticationKey = repoDetails.get(AUTHENTICATION_KEY);

        HttpURLConnection connectionForCountingLines = createConnectionForCountingLines();
        if (connectionForCountingLines.getResponseCode() == HttpURLConnection.HTTP_OK) {
            logger.info(CONNECTION_SUCCESSFUL_MESSAGE);
        } else {
            logger.error(CONNECTION_UNSUCCESSFUL_MESSAGE);
        }

        CompletableFuture<ProjectStats> statisticsForCountingLines =
                statisticsForCountingLines(connectionForCountingLines);
        CompletableFuture<HttpResponse<String>> report =
                generateReport(LOCALHOST_URL_FOR_GENERATING_REPORT, statisticsForCountingLines.get());
        writeReportToFile(report, build, listener);
    }

    private HttpURLConnection createConnectionForCountingLines() throws IOException {
        HttpURLConnection connectionForCountingLines;
        if (versionControl.equalsIgnoreCase(GITHUB)) {
            GitHubRepoClass gitHubRepo = new GitHubRepoClass(encodedRoot, authenticationKey, branchName);
            connectionForCountingLines = gitHubRepo.urlConnectionForCountingLines(LOCALHOST_URL_FOR_COUNTING);
        } else if (versionControl.equalsIgnoreCase(GITLAB)) {
            GitlabRepoClass gitlabRepo = new GitlabRepoClass(encodedRoot, authenticationKey, branchName);
            connectionForCountingLines = gitlabRepo.urlConnectionForCountingLines(LOCALHOST_URL_FOR_COUNTING);
        } else if (versionControl.equalsIgnoreCase(SVN)) {
            SvnRepoClass svnRepoClass = new SvnRepoClass(encodedRoot, localDirectoryUrlForSvnWithEncoding);
            connectionForCountingLines =
                    svnRepoClass.urlConnectionForCountingLines(LOCALHOST_URL_FOR_COUNTING_SVN_REPOSITORY);
        } else {
            logger.error("Wrong Version Control Used: {}", versionControl);
            throw new IllegalArgumentException("Wrong Version Control Used: " + versionControl);
        }
        return connectionForCountingLines;
    }

    private CompletableFuture<ProjectStats> statisticsForCountingLines(HttpURLConnection connectionForCountingLines) {
        return CountingLinesClient.makeAsyncApiCallForCounting(connectionForCountingLines);
    }

    private CompletableFuture<HttpResponse<String>> generateReport(String urlForGeneratingReport, ProjectStats pj) {
        GenerateReport generateReport = new GenerateReport();
        return generateReport.generatingReport(urlForGeneratingReport, pj);
    }

    private void writeReportToFile(
            CompletableFuture<HttpResponse<String>> report, AbstractBuild<?, ?> build, BuildListener listener)
            throws IOException, InterruptedException, ExecutionException {

        GetValuesFromConfigFile getValuesFromConfigFile = new GetValuesFromConfigFile();
        String reportTemplatePath = getValuesFromConfigFile.getReportTemplatePathFromConfig();

        File artifactsDir = createArtifactsDirectory(build, listener);
        String path = artifactsDir.getCanonicalPath() + reportTemplatePath;

        try (BufferedWriter writer =
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8))) {
            if (report != null && report.get() != null && report.get().body() != null) {
                writer.write(report.get().body());
            } else {
                logger.error("The report is null, please check.");
            }
        }
    }

    private File createArtifactsDirectory(AbstractBuild<?, ?> build, BuildListener listener) {
        File artifactsDir = build.getRootDir().toPath().resolve("archive").toFile();

        if (!artifactsDir.isDirectory()) {
            boolean success = artifactsDir.mkdirs();
            if (!success) {
                listener.getLogger().println("Can't create artifacts directory at " + artifactsDir.getAbsolutePath());
            }
        }

        return artifactsDir;
    }

    public static Map<String, String> extractRepoDetails(AbstractBuild<?, ?> build, BuildListener listener)
            throws IOException, InterruptedException {
        EnvVars envVars = build.getEnvironment(listener);
        String key = build.getProject().getScm().getKey();
        Map<String, String> repoDetails = new LinkedHashMap<>();
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

    private static void extractGitRepoDetails(String gitRepoDetails, EnvVars envVars, Map<String, String> repoDetails) {
        repoDetails.put(BRANCH_NAME, envVars.get("GIT_BRANCH"));
        repoDetails.put(ROOT_KEY, envVars.get("GIT_URL"));

        for (String part : gitRepoDetails.split("/")) {
            if ("github.com".equalsIgnoreCase(part)) {
                repoDetails.put(VERSION_CONTROL_KEY, GITHUB);
                repoDetails.put(AUTHENTICATION_KEY, GITHUB_KEY);
            } else if ("gitlab.com".equalsIgnoreCase(part)) {
                repoDetails.put(VERSION_CONTROL_KEY, GITLAB);
                repoDetails.put(AUTHENTICATION_KEY, GITLAB_KEY);
            }
        }
    }

    private static void extractSvnRepoDetails(String svnRepoDetails, Map<String, String> repoDetails) {
        repoDetails.put(VERSION_CONTROL_KEY, SVN);
        repoDetails.put(ROOT_KEY, svnRepoDetails);
    }

    private static void setDefaultRepoDetails(Map<String, String> repoDetails) {
        repoDetails.put(VERSION_CONTROL_KEY, null);
    }

    private static String encodeUrl() {
        final String localDirectoryUrlForSvnWithoutEncoding =
                System.getProperty("user.dir") + File.separator + ProjectStatsBuildWrapper.RELATIVE_PATH;
        String localDirectoryUrlForSvn = null;
        localDirectoryUrlForSvn = URLEncoder.encode(localDirectoryUrlForSvnWithoutEncoding, StandardCharsets.UTF_8);

        return localDirectoryUrlForSvn;
    }
}
