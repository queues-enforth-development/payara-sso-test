/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qed.aes.system.ui;

import com.qed.aes.system.managers.UserPreferenceManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

/**
 * UI for managing visual themes
 * @author jimmc
 */
@Named
@SessionScoped
public class ThemeBean implements java.io.Serializable {
    
    /**
     * A day or night theme mode
     */
    public enum Mode {
        DAY("cerulean"), NIGHT("cyborg");
        
        /**
         * Theme for this mode
         */
        private String theme;
        
        Mode(String theme) {
            this.theme = theme;
        }

        public String getTheme() {
            return theme;
        }

        public void setTheme(String theme) {
            this.theme = theme;
        }
        
        
    }
    
    /**
     * List of available themes
     */
    private static final List<String> THEMES = new ArrayList<>(Arrays.asList(new String[] {
        "default",
        "cerulean",
        "cosmo",
        "cyborg",
        "darkly",
        "flatly",
        "journal",
        "lumen",
        "paper",
        "readable",
        "sandstone",
        "simplex",
        "slate",
        "spacelab",
        "superhero",
        "united",
        "yeti"
    }));
    
    /**
     * The current theme mode
     */
    private Mode mode;
    
    
    /**
     * Create a new Theme UI
     */
    public ThemeBean() {
        
    }
    
    /**
     * Initialize
     */
    @PostConstruct
    private void initialize() {
        
        // Reset the theme mode
        mode = Mode.DAY;

        Mode.DAY.setTheme(UserPreferenceManager.DEFAULT_DAY_THEME);
        Mode.NIGHT.setTheme(UserPreferenceManager.DEFAULT_NIGHT_THEME);
        
    }

    public List<String> getThemes() {
        return THEMES;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }
    
    /**
     * Tell whether we are in day mode or not
     * @return 
     */
    public boolean isDayMode() {
        return mode == Mode.DAY;
    }
    
    /**
     * Set to day (TRUE) or night (FALSE) mode
     * @param dayMode
     */
    public void setDayMode(boolean dayMode) {
        mode = (dayMode) ? Mode.DAY : Mode.NIGHT;
    }
    
    /**
     * Toggle the current mode
     */
    public void toggleMode() {
        if (mode == Mode.DAY) {
            mode = Mode.NIGHT;
        } else {
            mode = Mode.DAY;
        }
    }
    
    /**
     * Get the current theme
     * @return 
     */
    public String getTheme() {
        return mode.getTheme();
    }
    
    /**
     * Get the day theme
     * @return 
     */
    public String getDayTheme() {
        return Mode.DAY.getTheme();
    }
    
    /**
     * Set the day theme
     * @param theme
     */
    public void setDayTheme(String theme) {
        Mode.DAY.setTheme(theme);
    }
    
    /**
     * Get the night theme
     * @return 
     */
    public String getNightTheme() {
        return Mode.NIGHT.getTheme();
    }
    
    /**
     * Set the night theme
     * @param theme
     */
    public void setNightTheme(String theme) {
        Mode.NIGHT.setTheme(theme);
    }
    
    /**
     * Shut down
     */
    @PreDestroy
    private void shutdown() {
        
    }
    
}
