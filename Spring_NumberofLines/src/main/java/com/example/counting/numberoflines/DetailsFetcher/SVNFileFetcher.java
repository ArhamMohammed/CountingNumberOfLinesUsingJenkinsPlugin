package com.example.counting.numberoflines.DetailsFetcher;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.IOException;

public class SVNFileFetcher {

    public void authenticate(String svnUrl,String username, String password) throws SVNException {

        SVNURL svnURL = SVNURL.parseURIEncoded(svnUrl);
        SVNRepository repository = SVNRepositoryFactory.create(svnURL);

        ISVNAuthenticationManager authManager = null;
        repository.setAuthenticationManager(authManager);;


        SVNURL repositoryUrl = SVNURL.parseURIEncoded(svnUrl);

        DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);
        SVNClientManager clientManager = SVNClientManager.newInstance(options, "username", "password");

        // Set authentication credentials if required
//        repository.setAuthenticationManager(SVNWCUtil.createDefaultAuthenticationManager(username, password));

        // Set up authentication manager with token
//        DefaultSVNAuthenticationManager authManager = (DefaultSVNAuthenticationManager) SVNWCUtil.createDefaultAuthenticationManager();
//        authManager.setAuthenticationProvider(new TokenAuthenticationProvider(token));

        // Use authentication manager for SVNRepository
//        SVNRepository repository = SVNRepositoryFactory.create(repositoryUrl, null);
//        repository.setAuthenticationManager(authManager);

        // Now you can use the SVNRepository to fetch details, perform operations, etc.
        long latestRevision = repository.getLatestRevision();
        System.out.println("Latest revision: " + latestRevision);
    }
}
