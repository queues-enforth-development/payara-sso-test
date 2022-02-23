/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qed.aes.system.ui;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

/**
 * JSF bean for managing navigation backwards
 * @author jimmc
 */
@Named
@RequestScoped
public class BackBean {
    
    /**
     * The URL to go back to
     */
    private String backurl;
    
    /**
     * Description of where we are returning (default is "Previous")
     */
    private String backdesc = "Previous";
    
    
    /**
     * An action to go back (i.e., usable in a commandLink or commandButton)
     * @return 
     */
    public String back() {
        return backurl + "?faces-redirect=true";
    }

    public String getBackurl() {
        return backurl;
    }

    public void setBackurl(String backurl) {
        this.backurl = backurl;
    }

    public String getBackdesc() {
        return backdesc;
    }

    public void setBackdesc(String backdesc) {
        this.backdesc = backdesc;
    }
    
}
