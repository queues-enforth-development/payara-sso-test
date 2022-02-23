/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qed.aes.system.ui;

import com.qed.aes.system.identitymanager.Identity;
import com.qed.aes.system.identitymanager.IdentityException;
import com.qed.aes.system.identitymanager.IdentityManager;
import com.qed.aes.system.security.AESSecurityException;
import com.qed.aes.system.security.AESSecurityServiceClient;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.NamingException;
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
     * Our identity manager
     */
    @EJB
    private IdentityManager identityManager;
    
    /**
     * Our current identity
     */
    private Identity identity;
    
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
            identity = identityManager.getIdentity(userName); 
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
     * Redirect to the Web/Partner security bridge link
     */
    public void goToExternalLink() {
        try {
            String link = getTranslatedLink(externalLink);
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            externalContext.redirect(link);
        } catch (IOException e) {
            basicBean.showError(LOGGER, "Unable to redirect to Web/Partner: ", e);
        }
    }
    
    /**
     * Get a translated link
     * @param url
     * @return 
     * @throws java.io.UnsupportedEncodingException 
     */
    public String getTranslatedLink(String url) throws UnsupportedEncodingException {
        String translatedUrl;
        
        // Is this a Web/Partner link?
        if (! isWebPartnerUrl(url)) {
            
            // Just use this
            translatedUrl = url;
            
        } else {
            
            // Translate the Web/Partner part of the link
            String wpUrl = translateUrl(url);
            
            // Encode the URL
            String encodedUrl = URLEncoder.encode(wpUrl, "UTF-8");
            
            // Now format for security bridge
            translatedUrl = getSecurityBridgeLink(encodedUrl);
            
        }
        
        return translatedUrl;
    }

    /**
     * Get Web/Partner link
     * @param wpLink
     * @return 
     */
    private String getSecurityBridgeLink(String wpLink) {
        String securityBridgeLink;
        
        try {
            
            // Make sure we have a link
            if (wpLink == null) {
                throw new AESSecurityException("No Web/Partner link specified.");
            }
            
            // Access our security service
            AESSecurityServiceClient securityService = new AESSecurityServiceClient();

            // Assign a token
            String userId = identity.getLoginId();
            String password = getPassword(userId);
            String token = securityService.assignToken(userId, password);

            // Start with Web/Partner
            StringBuilder baseUrl = new StringBuilder();
            baseUrl.append(webPartnerServerUrlSpecification);
            if (! webPartnerServerUrlSpecification.endsWith("/")) {
                baseUrl.append("/");
            }

            // Use the auto login URL
            StringBuilder aesLoginUrl = new StringBuilder();
            aesLoginUrl.append(baseUrl);
            aesLoginUrl.append(AES_LOGIN_URL);

            // Append our token
            appendParameter(aesLoginUrl, "id", token, 0);
            appendParameter(aesLoginUrl, "page", wpLink, 1);

            securityBridgeLink = aesLoginUrl.toString();

        } catch (AESSecurityException | IdentityException | NamingException e) {
            basicBean.showError(LOGGER, "Unable to create security bridge link: ", e);
            securityBridgeLink = "#";
        }
        
        return securityBridgeLink;
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
     * Get the current user's password
     * @param userId
     * @return 
     * @throws com.qed.aes.system.identitymanager.IdentityException 
     */
    public String getPassword(String userId) throws IdentityException {
        String password;
        
        try {
        
            // Use our security client
            AESSecurityServiceClient securityClient = new AESSecurityServiceClient(); 
            password = securityClient.getPassword(userId);
            
        } catch (AESSecurityException | NamingException e) {
            String message = "Messaging: Unable to retrieve password: ";
            LOGGER.log(Level.WARNING, message, e);
            throw new IdentityException(message, e);
        } 

        return password;
    }
    
    /**
     * Get our current identity
     * @return 
     * @throws com.qed.aes.system.identitymanager.IdentityException
     */
    public Identity getIdentity() throws IdentityException {
        if (initialized)
            return identity;
        else {
            throw new IdentityException("Unable to retrieve identity: ", initializationException);
        }
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

        // Get our request
        HttpServletRequest request = getRequestFrom(facesContext);        
        
        try {
            
            // Check out current identity
            Identity currentIdentity = getIdentity();
            String userId = currentIdentity.getLoginId();
            
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
            
        } catch (IdentityException | ServletException e) {
            basicBean.showError(LOGGER, "Unable to process logout: ", e);
        }

        // Redirect to our initial page for this application
        return basicBean.redirect(initialPage);        
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
