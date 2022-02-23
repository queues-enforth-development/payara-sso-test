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
public class AppBBean implements java.io.Serializable {
    
    /**
     * Our login and security UI
     */
    @Inject
    private IdentityBean identityBean;
    
    /**
     * Our person's guestName
     */
    private String guestName;
    
    /**
     * A message to display
     */
    private String message;
    
    
    /**
     * Create our UI
     */
    public AppBBean() {
        
    }
    
    /**
     * Initialize
     */
    @PostConstruct
    private void initialize() {
        guestName = "";
        message = "";
    }
    
    /**
     * Say hello
     */
    public void sayHello() {
        message = "Hello " + guestName + " from Application B!";
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
}
