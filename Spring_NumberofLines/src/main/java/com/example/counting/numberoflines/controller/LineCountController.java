package com.example.counting.numberoflines.controller;

import com.example.counting.numberoflines.model.ProjectStats;
import com.example.counting.numberoflines.service.CountingLinesService;
import org.gitlab4j.api.GitLabApiException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.tmatesoft.svn.core.SVNException;

import java.io.IOException;

@RestController
public class LineCountController {

    @GetMapping("/test")
    public String countLinesTest(@RequestParam String versionControl,@RequestParam String root,
                                 @RequestHeader("key") String key, @RequestParam String branchName) {
        return "Hello. Test is working";
    }

    @GetMapping("/counting")
    public ProjectStats countLines(@RequestParam String versionControl,@RequestParam String root,
                                   @RequestHeader("key") String key, @RequestParam String branchName) throws IOException, InterruptedException, GitLabApiException {
        System.out.println("Calling the controller now: "+java.time.LocalTime.now());
        return CountingLinesService.buildStats(versionControl,root,key,branchName);
    }

    @GetMapping("/countingForSVN")
    public ProjectStats countLines(@RequestParam String versionControl,@RequestParam String root,
                                   @RequestParam String userName, @RequestHeader("password") String password,
                                   @RequestParam String branchName)
            throws SVNException {
        System.out.println("Calling the controller now: "+java.time.LocalTime.now());
        return CountingLinesService.buildStats(versionControl,root,userName,password,branchName);
    }
    @PostMapping("/generateReport")
    public String generateReport(@RequestBody ProjectStats pj) throws IOException {
        if(pj != null)
            return CountingLinesService.generateReport("ArhamProject",pj);
        else
            return "The value of PJ is null";
    }

    @RequestMapping("/hello")
    public String hello(){
        return "Hello World";
    }
}
