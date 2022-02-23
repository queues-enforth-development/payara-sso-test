/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qed.aes.test.ui;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import static javax.security.enterprise.identitystore.CredentialValidationResult.INVALID_RESULT;
import javax.security.enterprise.identitystore.IdentityStore;

/**
 * AES Identity Store
 * @author jimmc
 */
@ApplicationScoped
public class MockIdentityStore implements IdentityStore {
    
    /**
     * Our logger
     */
    private static final Logger LOGGER = Logger.getLogger("com.qed.test");   
        
    
    /**
     * Create our identity store
     */
    public MockIdentityStore() {
        
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
        
        // For mock identity store, password should be same as user name
        boolean success = password.equals(userName);
                
        LOGGER.log(Level.FINE, "AesIdentityStore: Success: {0}", success);

        // If not successful
        if (! success) {
            result = INVALID_RESULT;
        } else {
            
            // Fetch our groups
            // Just insert some fixed groups
            Set<String> groupNames = new HashSet<>();
            groupNames.add("GROUP 1");
            groupNames.add("GROUP 2");
            
            // Create new result
            result = new CredentialValidationResult(userName, groupNames);
                        
        }
        
        return result;
    }
        
}