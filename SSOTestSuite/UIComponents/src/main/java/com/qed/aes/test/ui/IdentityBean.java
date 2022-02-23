/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qed.aes.test.ui;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.security.enterprise.SecurityContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * UI to manage identity of current user
 * @author jimmc
 */
@Named
@SessionScoped
public class IdentityBean implements java.io.Serializable {
    
    /**
     * Our error logger
     */
    private static final Logger LOGGER = Logger.getLogger("com.qed.test");
    
    /**
     * The SOTERIA security context
     */
    @Inject
    private SecurityContext securityContext;
    
    /**
     * The current FACES context
     */
    @Inject
    private FacesContext facesContext;

    /**
     * Basic UI services
     */
    @Inject
    private BasicBean basicBean;
    
    /**
     * Permissions cache
     */
    private Map<String, Boolean> permissionsCache;
        
    /**
     * Were we successfully able to initialize
     */
    private boolean initialized = false;
    
    /**
     * Any exception that we encountered when initializing
     */
    private Throwable initializationException = null;
    
    /**
     * Remember our user name
     */
    private String userName;
            
    
    /**
     * Create a new Identity UI
     */
    public IdentityBean() {
        
    }
    
    /**
     * Initialize our identity from the security system
     */
    @PostConstruct
    private void initialize() {
        
        try {
            
            LOGGER.log(Level.INFO, "Identity: Session being initialized...");

            // Initialize the cache
            permissionsCache = new HashMap<>();

            // Retrieve the user name
            Principal principal = securityContext.getCallerPrincipal();
            userName = principal.getName();
            
            // Retrieve permissions by user
            initialized = true;
            
        } catch (Throwable t) {
            initializationException = t;
            LOGGER.log(Level.WARNING, "Unable to identify current user: ", t);
        }

    }
    
/**
     * Default version of logout for applications that have not been updated.This will at least display something, even if just an error.
     * @return
     */
    public String logout() {
        return logout("/index");
    }
    
    /**
     * Begin the process of logging this user out
     * @param initialPage
     * @return 
     */
    public String logout(String initialPage) {   
        
        try {

            // Get our request
            HttpServletRequest request = getRequestFrom(facesContext);        

            // Check out current identity
            String userId = userName;

            // Try to retrieve our current session
            HttpSession session = request.getSession(false);
            String sessionId = (session != null) ? session.getId() : "";

            LOGGER.log(Level.INFO, "Identity: Logging out current request context for user/session: {0}/{1}", new Object[] { userId, sessionId } );

            // Logout current request
            request.logout();

            // Invalidate session so user becomes anonymous.
            if (session == null) {
                LOGGER.log(Level.INFO, "Identity: No session to invalidate for user: {0}", userId);
            } else {
                session.invalidate();
                LOGGER.log(Level.INFO, "Session invalidated for user: {0}/{1}", new Object[] { userId, sessionId } );
            }
            
        } catch (ServletException e) {
            basicBean.showError(LOGGER, "Unable to process logout: ", e);
        }
            
        // Redirect to our initial page for this application
        return basicBean.redirect(initialPage);        
    }
    
    /**
     * Safely join two paths without repeating the slashes
     * @param path1
     * @param path2
     * @return 
     */
    public String joinPaths(String path1, String path2) {
        StringBuilder newPath = new StringBuilder();
        
        if (path1.endsWith("/")) {
            if (path2.startsWith("/")) {
                newPath.append(path1);
                newPath.append(path2.substring(1));
            } else {
                newPath.append(path1);
                newPath.append(path2);
            }
        } else {
            if (path2.startsWith("/")) {
                newPath.append(path1);
                newPath.append(path2);
            } else {
                newPath.append(path1);
                newPath.append("/");
                newPath.append(path2);
            }
        }
        
        return newPath.toString();
    }
    
    /**
     * Is a particular function permitted for a JSF screen?
     * @param applicationName
     * @param function
     * @return 
     */
    public boolean isPermitted(String applicationName, String function) {
        boolean permitted;
                
        // Format our function
        String applicationFunction = makeApplicationFunction(applicationName, function);
        
        // Check cache first
        if (permissionsCache.containsKey(applicationFunction)) {
            // If found, use it
            permitted = permissionsCache.get(applicationFunction);
        } else {

            // Check the current request
            HttpServletRequest request = getRequestFrom(facesContext);            
            
            // See if this user has this permission
            permitted = request.isUserInRole(applicationFunction);
            
            // Save what we decided
            permissionsCache.put(applicationFunction, permitted);
            
        }
        
        return permitted;
    }
    
    /**
     * Make a combined application + role string
     */
    private String makeApplicationFunction(String applicationName, String functionName) {
        return applicationName + ":" + functionName;
    }
    
    /**
     * Get the request from our context
     * @return 
     */
    private HttpServletRequest getRequestFrom(FacesContext facesContext) {
        return (HttpServletRequest) facesContext.getExternalContext().getRequest();
    }
    
    /**
     * Shut down our session
     */
    @PreDestroy
    private void shutdown() {
        LOGGER.log(Level.INFO, "Identity: Session being shut down...");
    }
   
    /**
     * Format the string for insert/update in an SQL statement
     * @param s
     * @return 
     */
    public static String sqlFormat(String s) {
        String result;
        
        if (s == null  ||  s.equals("")) 
            result = "null";
        else
            result = quote(s);
        
        return result;
    }  
    
    /** 
     * Surround string with quotes; also drops leading/trailing blanks and substitutes quotes. 
     * @param s
     * @return 
     */
    public static String quote(String s) {
        return ("\"" + cleanUp(s) + "\"");
    }
    
    /** 
     * Private helper function to clean up a string; trims and translates quotes 
     */
    private static String cleanUp(String s) {
        return (s.trim().replace('"', '\''));
    }

    public String getUserName() {
        return userName;
    }
    
}
