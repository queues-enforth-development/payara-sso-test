package com.qed.aes.system.ui;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.qed.aes.system.entities.AesUserPreferences;
import com.qed.aes.system.exceptions.UserPreferenceException;
import com.qed.aes.system.identitymanager.Identity;
import com.qed.aes.system.identitymanager.IdentityException;
import com.qed.aes.system.managers.UserPreferenceManager;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Configuration bean
 * @author tgorman
 */
@Named
@ViewScoped
public class UserPreferenceBean implements java.io.Serializable {
    
    /**
     * Our error logger
     */
    private static final Logger LOGGER = Logger.getLogger("com.qed.aes.system.ui.theme");
        
    /**
     * Manager for this application
     */
    @EJB 
    private UserPreferenceManager manager; 
        
    /**
     * Basic UI services
     */
    @Inject
    private BasicBean basicBean;
    
    /**
     * Our identity manager
     */
    @Inject
    private IdentityBean identityBean;    
        
    /**
     * Our UI themes
     */
    @Inject
    private ThemeBean themeBean;
    
    /**
     * Redirect info
     */
    private String backurl;
    
    /**
     * Redirect description
     */
    private String backdesc;
    
    /**
     * Our user preferences
     */
    private AesUserPreferences preferences;
    
    /**
     * Our day theme
     */
    private String dayTheme;
    
    /**
     * Our night theme
     */
    private String nightTheme;    
        
    
    /**
     * Create our configuration bean
     */
    public UserPreferenceBean() {
        
    }
    
    /**
     * Initialize our bean
     */
    @PostConstruct
    private void initialize() {
        
    }
    
    /**
     * Get our identity
     * @return 
     * @throws com.qed.aes.system.identitymanager.IdentityException
     */
    public Identity getIdentity() throws IdentityException {
        return identityBean.getIdentity();
    }
    
    /**
     * Get our user ID
     * @return 
     * @throws com.qed.aes.system.identitymanager.IdentityException 
     */
    public String getUserId() throws IdentityException {
        return getIdentity().getLoginId();
    }
    
    /**
     * Load our information
     * @throws com.qed.aes.system.exceptions.UserPreferenceException
     * @throws com.qed.aes.system.identitymanager.IdentityException
     */
    public void load() throws UserPreferenceException, IdentityException {
            
        // Load our themes
        loadThemes();

        // Load our preferences
        preferences = loadPreferences();
                    
    }
    
    /**
     * Load our current themes
     */
    private void loadThemes() {
        
        // Fetch them from our current settings
        dayTheme = themeBean.getDayTheme();
        nightTheme = themeBean.getNightTheme();
        
    }    

    public String getDayTheme() {
        return dayTheme;
    }

    public void setDayTheme(String dayTheme) {
        this.dayTheme = dayTheme;
    }

    public String getNightTheme() {
        return nightTheme;
    }

    public void setNightTheme(String nightTheme) {
        this.nightTheme = nightTheme;
    }

    /**
     * @return the backurl
     */
    public String getBackurl() {
        return backurl;
    }

    /**
     * @param backurl the backurl to set
     */
    public void setBackurl(String backurl) {
        this.backurl = backurl;
    }

    /**
     * @return the backdesc
     */
    public String getBackdesc() {
        return backdesc;
    }

    /**
     * @param backdesc the backdesc to set
     */
    public void setBackdesc(String backdesc) {
        this.backdesc = backdesc;
    }
        
    /**
     * Save theme information
    */
    public void saveTheme() {
        
        // Reset our current themes
        themeBean.setDayTheme(dayTheme);
        themeBean.setNightTheme(nightTheme);

        // Save our preferences
        savePreferences();
        
    } 
    
    /**
     * Save our preferences
     */
    private void savePreferences() {
                
        // File away our preferences
        preferences.setDayTheme(themeBean.getDayTheme());
        preferences.setNightTheme(themeBean.getNightTheme());            

        // Save or update
        manager.savePreferences(preferences);
        
    }
    
    public String goToBackUrl(){
        return basicBean.redirect(backurl);
    }

    /**
     * Load our user preferences
     * @return
     * @throws UserPreferenceException
     * @throws IdentityException 
     */
    public AesUserPreferences loadPreferences() throws UserPreferenceException, IdentityException {
        
        // Retrieve from DB
        String userName = getIdentity().getLoginId();
        return manager.getUserPreferences(userName);
        
    }    
    
}