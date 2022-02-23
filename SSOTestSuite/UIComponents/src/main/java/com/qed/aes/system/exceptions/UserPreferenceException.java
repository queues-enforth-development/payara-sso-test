/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qed.aes.system.exceptions;

/**
 *
 * @author jimmc
 */
public class UserPreferenceException extends Exception {

    /**
     * Creates a new instance of <code>ThemeException</code> without detail
     * message.
     */
    public UserPreferenceException() {
        
    }

    /**
     * Constructs an instance of <code>ThemeException</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public UserPreferenceException(String msg) {
        super(msg);
    }

    public UserPreferenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserPreferenceException(Throwable cause) {
        super(cause);
    }
}
