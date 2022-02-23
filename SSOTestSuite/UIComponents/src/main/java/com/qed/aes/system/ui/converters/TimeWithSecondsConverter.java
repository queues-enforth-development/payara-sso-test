/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qed.aes.system.ui.converters;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

/**
 * A FACES converter for displaying time
 * @author Jim McClure
 */
@FacesConverter("com.qed.aes.system.ui.converters.TimeWithSecondsConverter")
public class TimeWithSecondsConverter implements javax.faces.convert.Converter {
    
    private final SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        // NOP
        return new Object();
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return value == null ? null : formatter.format((Date) value);
    }
    
}
