/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qed.aes.system.ui.converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

/**
 * A FACES converter for converting text to upper case
 * @author Jim McClure
 */
@FacesConverter("com.qed.aes.system.ui.converters.TextConverter")
public class TextConverter implements javax.faces.convert.Converter {
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return value == null ? null : value.toUpperCase();
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return value == null ? null : ((String) value).toUpperCase();
    }
    
}
