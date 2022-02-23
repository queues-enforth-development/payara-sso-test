/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qed.aes.system.ui;

import com.qed.aes.system.entities.AesUserPreferences;
import com.qed.aes.system.exceptions.UserPreferenceException;
import com.qed.aes.system.identitymanager.IdentityException;
import com.qed.aes.system.managers.UserPreferenceManager;
import com.qed.aes.system.security.AESSecurityException;
import com.qed.aes.system.security.AESSecurityServiceClient;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.annotation.FacesConfig;
import static javax.faces.annotation.FacesConfig.Version.JSF_2_3;
import javax.faces.application.FacesMessage;
import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.NamingException;
import javax.security.enterprise.AuthenticationStatus;
import static javax.security.enterprise.AuthenticationStatus.SEND_CONTINUE;
import static javax.security.enterprise.AuthenticationStatus.SEND_FAILURE;
import javax.security.enterprise.SecurityContext;
import static javax.security.enterprise.authentication.mechanism.http.AuthenticationParameters.withParams;
import javax.security.enterprise.authentication.mechanism.http.CustomFormAuthenticationMechanismDefinition;
import javax.security.enterprise.authentication.mechanism.http.LoginToContinue;
import javax.security.enterprise.credential.Credential;
import javax.security.enterprise.credential.Password;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@FacesConfig(
	// Activates CDI build-in beans
	version = JSF_2_3 
)
@CustomFormAuthenticationMechanismDefinition(
    loginToContinue = @LoginToContinue(
            loginPage="/login.xhtml",
            errorPage="" // DRAFT API - must be set to empty for now
    )
)
@Named
@RequestScoped
public class LoginBean {
  
    /**
     * Our logger
     */
    private static final Logger LOGGER = Logger.getLogger("com.qed.aes.security");   

    /**
     * The SOTERIA security context
     */
    @Inject
    private SecurityContext securityContext;

    /**
     * Faces/JSF context
     */
    @Inject
    private FacesContext facesContext;
    
    /**
     * Our user themes
     */
    @Inject
    private ThemeBean themeBean;
    
    /**
     * Our user preference manager
     */
    @EJB
    private UserPreferenceManager manager;
    
    /**
     * User name
     */
    private String username;
   
    /**
     * Entered password
     */
    private String password;


    /**
     * Create our login bean
     */
    public LoginBean() {

    }
   
   /**
    * Initialize
    */
   @PostConstruct
   private void initialize() {

   }
   
    /**
     * Process a login
     */
    public void login() {
        
        // Check for missing user name or password
        if (isBlank(username)) {
            addError(facesContext, "User name cannot be left blank.");
        } else {
        
            // Check for missing password
            if (isBlank(password)) {
                addError(facesContext, "Password cannot be left blank.");
            } else {

                // Create user credentials from login info
                Credential credential = new UsernamePasswordCredential(username, new Password(password));

                // Try to validate our user
                AuthenticationStatus status = securityContext.authenticate(getRequestFrom(facesContext), getResponseFrom(facesContext), withParams().credential(credential));

                // If we are OK
                if (status.equals(SEND_CONTINUE)) {

                    try {
                        
                        // Register our session with the security system
                        AESSecurityServiceClient securityClient = new AESSecurityServiceClient(); 
                        securityClient.registerSession(username, password);

                        // Restore our user preferenecs
                        restorePreferences(username);

                        // Complete the response
                        facesContext.responseComplete();

                    } catch (NamingException | AESSecurityException e) {
                        addError(facesContext, "Unable to access user session.");
                    }

                } else if (status.equals(SEND_FAILURE)) {

                   // Authentication failed
                   addError(facesContext, "Incorrect user name or password.");

                }
                
            }
            
        }
      
    }
    
    /** 
     * Is this a blank string? 
     * @param s
     * @return 
     */
    public static boolean isBlank(String s) {
        if (s == null)
            return true;
        else
            return (s.trim().equals(""));
    }
        
    /**
     * Restore our user preferences
     */
    private void restorePreferences(String loginId) {
        
        // Reset the theme mode
        themeBean.setMode(ThemeBean.Mode.DAY);

        try {
            
            AesUserPreferences preferences = manager.getUserPreferences(loginId);
            ThemeBean.Mode.DAY.setTheme(preferences.getDayTheme());
            ThemeBean.Mode.NIGHT.setTheme(preferences.getNightTheme());
            
        } catch (IdentityException | UserPreferenceException e) {
            ThemeBean.Mode.DAY.setTheme(UserPreferenceManager.DEFAULT_DAY_THEME);
            ThemeBean.Mode.NIGHT.setTheme(UserPreferenceManager.DEFAULT_NIGHT_THEME);
        }        
        
    }
   
    /**
     * Log the current user out
     */
    public void logout() {

        // Get our request
        HttpServletRequest request = getRequestFrom(facesContext);        
        
        try {
            
            // Try to retrieve our current session
            HttpSession session = request.getSession(false);
            String sessionId = (session != null) ? session.getId() : "";
            
            LOGGER.log(Level.INFO, "Identity: Logging out current request context for user/session: {0}", sessionId );

            // Logout current request
            request.logout();
            
            // Invalidate session so user becomes anonymous.
            if (session == null) {
                LOGGER.log(Level.INFO, "Identity: No session to invalidate for user.");
            } else {
                session.invalidate();
                LOGGER.log(Level.INFO, "Session invalidated for user: {0}", sessionId );
            }
            
        } catch (ServletException e) {
            addError(facesContext, e.getMessage());
        }

    }

    /**
     * Get the request from our context
     * @return 
     */
    private HttpServletRequest getRequestFrom(FacesContext facesContext) {
        return (HttpServletRequest) facesContext.getExternalContext().getRequest();
    }
    
    /**
     * Get the response from our context
     * @return 
     */
    private HttpServletResponse getResponseFrom(FacesContext facesContext) {
        return (HttpServletResponse) facesContext.getExternalContext().getResponse();
    }

    /**
     * Add an error to our faces context
     * @param context
     * @param message 
     */
    private void addError(FacesContext context, String message) {
        LOGGER.log(Level.WARNING, "LoginBean: {0}", message);
        context.addMessage(null, new FacesMessage(SEVERITY_ERROR, message, null));
    }

   public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
}