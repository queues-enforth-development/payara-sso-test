/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qed.aes.system.ui.converters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

/**
 * A FACES converter for displaying time
 * @author Jim McClure
 */
@FacesConverter("com.qed.aes.system.ui.converters.DateTimeConverter")
public class DateTimeConverter implements javax.faces.convert.Converter {
    
    /**
     * Our error logger
     */
    private static final Logger LOGGER = Logger.getLogger("com.qed.aes.system.ui.converters");
    
    /**
     * Our date format
     */
    private final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    /**
     * Convert string to object
     * @param context
     * @param component
     * @param value
     * @return 
     */
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Date dateValue = null;
        
        // If we have a value
        if (value != null) {
            try {
                dateValue = formatter.parse(value);
            } catch (ParseException e) {
                LOGGER.log(Level.WARNING, "DateTimeConverter: Unable to parse date: {0}", value);
            }
        }
        
        return dateValue;
    }

    /**
     * Convert object to string
     * @param context
     * @param component
     * @param value
     * @return 
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return value == null ? null : formatter.format((Date) value);
    }
    
}
