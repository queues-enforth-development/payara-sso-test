/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qed.aes.system.ui;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import static javax.security.enterprise.identitystore.CredentialValidationResult.INVALID_RESULT;
import javax.security.enterprise.identitystore.IdentityStore;
import javax.sql.DataSource;

/**
 * AES Identity Store
 * @author jimmc
 */
@ApplicationScoped
public class AesIdentityStore implements IdentityStore {
    
    /**
     * Our logger
     */
    private static final Logger LOGGER = Logger.getLogger("com.qed.aes.security");   

    /**
     * Our data source
     */
    @Resource(mappedName = "jdbc/aes")
    private DataSource dataSource;
        
    
    /**
     * Create our identity store
     */
    public AesIdentityStore() {
        
    }
    
    /**
     * Initialize
     */
    @PostConstruct
    private void initialize() {
        
    }
    
    /**
     * Validate a user and password
     *
     * @param userCredential
     * @return
     */
    public CredentialValidationResult validate(UsernamePasswordCredential userCredential) {
        CredentialValidationResult result;
        
        // Extract information from our credentials
        String userName = userCredential.getCaller();
        String password = userCredential.getPasswordAsString();
        
        LOGGER.log(Level.FINE, "AesIdentityStore: Validating credentials for: {0}", userName);
        
        // See if the database will validate the user
        boolean success = checkInformixUser(userName, password);
                
        LOGGER.log(Level.FINE, "AesIdentityStore: Success: {0}", success);

        // If not successful
        if (! success) {
            result = INVALID_RESULT;
        } else {
            
            // Fetch our groups
            Set<String> groupNames = getUserGroupPermissions(userName, password);
            
            // Create new result
            result = new CredentialValidationResult(userName, groupNames);
                        
        }
        
        return result;
    }
    
    /**
     * See if user is recognized by Informix
     * @param user
     * @param password
     * @return 
     */
    private boolean checkInformixUser(String userId, String password) {
        boolean success;
        
        // Try to get connection
        try (Connection con = dataSource.getConnection(userId, password)) {
            
            // This is really all we need
            success = true;
            
        } catch (SQLException e) {
            
            // If we couldn't get a connection, it was probably b/c user/password wrong
            success = false;
            
        }

        return success;
    }
     
    /**
     * Get list of groups for user
     */
    private Set<String> getUserGroupPermissions(String userName, String password) {
        Set<String> groupNames = new HashSet<>();
        
        // Set up our query
        String sql = "select distinct fp.appname, fp.appfunc from qedappfuncperms fp, qedappgrpmembers ag, qedappperms ap, qedusergrpmembers ug where ag.appname = fp.appname and ug.loginid = " + quote(userName) + " and ap.appgroup = ag.appgroup and ap.usergroup = ug.usergroup and fp.approle = ap.approle";
        
        try (
                // Fetch list of application names and user roles
                Connection con = dataSource.getConnection(userName, password);
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
            ) {
            
            // For each result
            while (rs.next()) {
            
                // Get the app name and function name
                String appName = rs.getString(1);
                String functionName = rs.getString(2);

                // Join them
                String applicationFunction = appName.trim() + ":" + functionName.trim();

                // Add to list
                groupNames.add(applicationFunction);
            
            }
            
        } catch (SQLException e) {
            // Leave list empty
            LOGGER.log(Level.WARNING, "Unable to fetch user permissions: " + userName, e);
        }
        
        return groupNames;
    }
    
    /**
     * Surround string with quotes
     */
    private String quote(String s) {
        return "'" + s + "'";
    }
    
}