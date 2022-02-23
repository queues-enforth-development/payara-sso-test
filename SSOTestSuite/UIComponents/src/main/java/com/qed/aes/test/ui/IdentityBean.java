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
import javax.annotation.Resource;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.security.enterprise.SecurityContext;
import javax.servlet.http.HttpServletRequest;

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
    private static final Logger LOGGER = Logger.getLogger("com.qed.aes.system.ui");
    
    /**
     * Our login URL
     */
    private static final String AES_LOGIN_URL = "partner/common/aes_login.jsp";
    
    /**
     * Web/Partner protocol string
     */
    private static final String WEB_PARTNER_PROTOCOL = "webpartner://";
    
    /**
     * The URL to our Web/Partner server
     * This will be injected by the container from JNDI
     */
    @Resource(mappedName = "resource/webPartnerServerUrl")
    private String webPartnerServerUrlSpecification;
    
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
     * A Web/Partner link (if present)
     */
    private String externalLink;
        
    
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
            String userName = principal.getName();
            
            // Retrieve permissions by user
            initialized = true;
            
        } catch (Throwable t) {
            initializationException = t;
            LOGGER.log(Level.WARNING, "Unable to identify current user: ", t);
        }

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
     * Translate a URL that has webpartner:// to the proper server
     * @param origURL
     * @return 
     */
    public String translateUrl(String origURL) {
        String newURL;
        
        // See if we've specified the server
        if (isWebPartnerUrl(origURL)) {
            
            // Replace this with the name of the Web/Partner server
            newURL = webPartnerServerUrlSpecification + "/" + origURL.substring(WEB_PARTNER_PROTOCOL.length());
            
        } else {
            
            // Use URL as-is
            newURL = origURL;
            
        }
                
        return newURL;
    }
    
    /**
     * See if a URL is for the Web/Partner system
     * @param url
     * @return 
     */
    public boolean isWebPartnerUrl(String url) {
        boolean webPartner = url.startsWith(WEB_PARTNER_PROTOCOL);
        return webPartner;
    }
    
    /**
     * Append a parameter to a URL
     */
    private void appendParameter(StringBuilder url, String parameterName, String parameterValue, int parameterCount) {
        url.append(parameterCount == 0 ? "?" : "&");
        url.append(parameterName);
        url.append("=");
        url.append(parameterValue);
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

    public String getExternalLink() {
        return externalLink;
    }

    public void setExternalLink(String externalLink) {
        this.externalLink = externalLink;
    }
    
}
