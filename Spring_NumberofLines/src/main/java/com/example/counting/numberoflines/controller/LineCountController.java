package com.example.counting.numberoflines.controller;

import com.example.counting.numberoflines.model.ProjectStats;
import com.example.counting.numberoflines.service.CountingLinesService;
import org.gitlab4j.api.GitLabApiException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class LineCountController {

//    @GetMapping("/test")
//    public String countLinesTest(@RequestParam String root, @RequestParam String key) throws IOException, InterruptedException {
//        ProjectStats pj = CountingLinesService.buildStats(root,key);
//        return CountingLinesService.generateReport("ArhamProject",pj);
//    }

    @GetMapping("/counting")
    public ProjectStats countLines(@RequestParam String versionControl,@RequestParam String root,
                                   @RequestHeader("key") String key, @RequestParam String branchName) throws IOException, InterruptedException, GitLabApiException {
        return CountingLinesService.buildStats(versionControl,root,key,branchName);
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
