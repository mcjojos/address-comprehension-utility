/*
 * Copyright (c) 2016. All Rights Reserved
 */

package com.jojos.home.addresscomprehension;

/**
 * Generic application exception used to indicate an error that stops the tool
 * <p>
 * Created by karanikasg@gmail.com.
 */
public class ApplicationException extends Exception {
    public ApplicationException(String message) {
        super(message);
    }

    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
