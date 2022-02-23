/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qed.aes.system.entities;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * User preferences for Aes Applications (ACM)
 * @author Jim McClure
 */
@Entity
@Table(name = "aesuserpreferences")
@NamedQueries({
    @NamedQuery(name = "AesUserPreferences.findAll", query = "SELECT c FROM AesUserPreferences c"),
})
public class AesUserPreferences implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 15)
    @Column(name = "userid")
    private String userId;
    
    /**
     * The name of our day theme
     * @return 
     */
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "daytheme")
    private String dayTheme;

    /**
     * The name of our day theme
     * @return 
     */
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "nighttheme")
    private String nightTheme;

    /**
     * Implementation
     */
    
    public AesUserPreferences() {
        
    }

    public AesUserPreferences(String userId) {
        this.userId = userId;
    }
    
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (userId != null ? userId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the agencyName fields are not set
        if (!(object instanceof AesUserPreferences)) {
            return false;
        }
        AesUserPreferences other = (AesUserPreferences) object;
        if ((this.userId == null && other.userId != null) || (this.userId != null && !this.userId.equals(other.userId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.qed.aes.system.entities.AesUserPreferences[ id=" + userId + " ]";
    }
    
}
