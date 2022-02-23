/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qed.aes.system.managers;

import com.qed.aes.system.entities.AesUserPreferences;
import com.qed.aes.system.exceptions.UserPreferenceException;
import com.qed.aes.system.identitymanager.IdentityException;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Manager to retrieve and update user preferences
 * @author jimmc
 */
@Stateless
public class UserPreferenceManager {
    
    /**
     * Our error logger
     */
    private static final Logger LOGGER = Logger.getLogger("com.qed.aes.system.managers");        
    
    /**
     * Default day theme
     */
    public static final String DEFAULT_DAY_THEME = "cerulean";
    
    /**
     * Default night theme
     */
    public static final String DEFAULT_NIGHT_THEME = "cyborg";

    /**
     * Our persistence unit
     */
    @PersistenceContext(unitName="userPreferencesPersistenceUnit")
    private EntityManager em;  


    /**
     * Create our manager
     */
    public UserPreferenceManager() {
        
    }
    
    /**
     * Initialize
     */
    @PostConstruct
    private void initialize() {
        
    }

    /**
     * 
     * @param userName
     * @return
     * @throws IdentityException
     * @throws UserPreferenceException 
     */
    public AesUserPreferences getUserPreferences(String userName) throws IdentityException, UserPreferenceException{
        AesUserPreferences ourPreferences;
        
        // See if we can get an existing set of preferences
        try {
            ourPreferences = getPreferences(userName);
        } catch (UserPreferenceException e) {
            // If not, let's create a new set
            ourPreferences = createDefaultPreferences(userName);
        }
        
        return ourPreferences;                         
    }
    
    /**
     * Create a default set of preferences
     */
    private AesUserPreferences createDefaultPreferences(String userId) {
        AesUserPreferences newPreferences = new AesUserPreferences(userId);
        
        // Set defaults
        newPreferences.setDayTheme(DEFAULT_DAY_THEME);
        newPreferences.setNightTheme(DEFAULT_NIGHT_THEME);
        
        return newPreferences;
    }
        
    /**
     * Load user preferences
     * @param userId
     * @return 
     * @throws UserPreferenceException
     */
    public AesUserPreferences getPreferences(String userId) throws UserPreferenceException {

        AesUserPreferences preferences = em.find(AesUserPreferences.class, userId);
        
        // If we can't find it
        if (preferences == null) {
            throw new UserPreferenceException("Unable to find user preferences: " + userId);
        }
        
        return preferences;
    }
    
    /**
     * Save our preferences, either adding or updating them
     * @param preferences
     * @return 
     */
    public AesUserPreferences savePreferences(AesUserPreferences preferences) {
        AesUserPreferences mergedPreferences;
        
        // See if we have existing preferences
        if (em.find(AesUserPreferences.class, preferences.getUserId()) != null) {
            mergedPreferences = em.merge(preferences);
        } else {
            addPreferences(preferences);
            mergedPreferences = preferences;
        }
        
        return mergedPreferences;
    }
    
    /**
     * Update our user preferences
     * @param preferences
     * @return 
     */
    public AesUserPreferences updatePreferences(AesUserPreferences preferences) {
        AesUserPreferences mergedPreferences = em.merge(preferences);
        return mergedPreferences;
    }    
        
    /**
     * Add our user preferences
     * @param preferences
     * @throws UserPreferenceException
     */
    public void addPreferences(AesUserPreferences preferences) {
        em.persist(preferences);
    }     
    
}
