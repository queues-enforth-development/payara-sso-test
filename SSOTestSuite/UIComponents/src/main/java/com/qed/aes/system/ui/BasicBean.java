/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qed.aes.system.ui;

import com.qed.aes.system.security.AESSecurityServiceClient;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.enterprise.context.Dependent;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;

/**
 * Basic UI services
 * @author jimmc
 */
@Named
@Dependent
public class BasicBean implements java.io.Serializable {
    
    
    /**
     * Create our UI
     */
    public BasicBean() {
        
    }
    
    /**
     * Show a message
     * @param message
     */
    public void showMessage(String message) {
        // Queue the message for display
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(message, null));        
    }       
    
    /**
     * Log and display an error message
     * @param logger
     * @param message
     * @param t 
     */
    public void showError(Logger logger, String message, Throwable t) {
        logger.log(Level.WARNING, message, t);
        String detailMessage = t.getMessage();
        if (detailMessage != null) {
            if (! message.endsWith(": ")) {
                message = message + ": ";
            }
        }

        // Queue the message for display
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, detailMessage));
    }       
    
    /**
     * Transition to new page via redirect
     * @param url
     * @return 
     */
    public String redirect(String url) {
        String nextPage = null;
        
        if (url != null) {
            // Do we already have parameters?
            if (url.contains("?")) {
                nextPage = url + "&faces-redirect=true";
            } else {
                nextPage = url + "?faces-redirect=true";
            }
        }

        return nextPage;
    }
    
    /**
     * Redirect with parameters
     * @param url
     * @param parameters
     * @return 
     */
    @Deprecated
    public String redirect(String url, String parameters) {
        return (url == null) ? null : url + "?faces-redirect=true" + "&" + parameters;
    }
    
    /**
     * Redirect to a page with parameters given as an array
     * 
     * Example:
     *     return redirect("page", new String[][] {
     *                  { "agencyName", agencyName },
     *                  { "incidentNumber", incidentNumber },
     *                  { "reportNumber", reportNumber }
     *     });
     * @param viewPath
     * @param parameters
     * @return 
     */
    public String redirect(String viewPath, String[][] parameters) {
        StringBuilder b = new StringBuilder();
        
        // Concatenate the parameters and values
        boolean first = true;
        for (String[] row : parameters) {
            String parameterName = row[0];
            String value = row[1];
            
            // Get the value 
            String fragment = getUrlFragment(parameterName, value);
            if (! fragment.equals("")) {
                if (! first) {
                    b.append("&");
                }
                b.append(fragment);
                
                first = false;
            }
        }
        
        return redirect(viewPath, b.toString());
    }
        
    /**
     * Compose a URL fragment given name and value
     * @param name
     * @param value
     * @return 
     */
    private String getUrlFragment(String name, String value) {
        StringBuilder b = new StringBuilder();
        if (value != null) {
            b.append(name);
            b.append("=");
            b.append(value);
        }

        return b.toString();        
    }
    
    /**
     * Redirect with parameters from the request parameter map
     * @param view
     * @param logger
     */
    public void redirectAJAX(String view, Logger logger) {
        StringBuilder redirectBuilder = new StringBuilder();
        
        try {
            
            // Set up the destination
            redirectBuilder.append(view);
            if (! view.endsWith(".xhtml")) {
                redirectBuilder.append(".xhtml");
            }

            redirectBuilder.append("?faces-redirect=true");
            ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
            Map<String, String> parameters = context.getRequestParameterMap();
            for (String parameterName : parameters.keySet()) {
                String parameterValue = parameters.get(parameterName);
                if ((! parameterName.contains("javax"))  &&  (! parameterName.contains("Form"))) {
                    redirectBuilder.append("&");
                    redirectBuilder.append(parameterName);
                    redirectBuilder.append("=");
                    redirectBuilder.append(parameterValue);
                }
            }

            // Do the redirect
            context.redirect(redirectBuilder.toString());
            
        } catch (IOException e) {
            showError(logger, "Unable to redirect to page: " + redirectBuilder.toString(), e);
        }
    }
    
}
