/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qed.aes.test.ui;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.annotation.FacesConfig;
import static javax.faces.annotation.FacesConfig.Version.JSF_2_3;
import javax.faces.application.FacesMessage;
import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.security.enterprise.AuthenticationStatus;
import static javax.security.enterprise.AuthenticationStatus.SEND_CONTINUE;
import static javax.security.enterprise.AuthenticationStatus.SEND_FAILURE;
import static javax.security.enterprise.AuthenticationStatus.SUCCESS;
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
    private static final Logger LOGGER = Logger.getLogger("com.qed.test");   

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

                // If we are OK and need to send a redirect...
                if (status.equals(SEND_CONTINUE)) {


                    // Normally do some housekeeping here...

                    // Complete the response and send the redirect
                    facesContext.responseComplete();


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
     * Log the current user out
     * @return 
     */
    public String logout() {

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

        return "/index.xhtml";
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