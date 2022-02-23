/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qed.test;

import com.qed.aes.test.ui.IdentityBean;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Test Application
 * @author jimmc
 */
@Named
@SessionScoped
public class AppABean implements java.io.Serializable {
    
    /**
     * Our login and security UI
     */
    @Inject
    private IdentityBean identityBean;
    
    /**
     * Our person's userName
     */
    private String userName;
    
    /**
     * A message to display
     */
    private String message;
    
    
    /**
     * Create our UI
     */
    public AppABean() {
        
    }
    
    /**
     * Initialize
     */
    @PostConstruct
    private void initialize() {
        userName = "";
        message = "";
    }
    
    /**
     * Say hello
     */
    public void sayHello() {
        message = "Hello " + userName + " from Application A!";
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
}
