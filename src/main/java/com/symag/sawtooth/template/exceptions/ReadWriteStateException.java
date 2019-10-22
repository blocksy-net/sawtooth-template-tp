package com.symag.sawtooth.template.exceptions;

import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcError;

@JsonRpcError(code = -32034)
public class ReadWriteStateException extends Exception {
    public ReadWriteStateException(String message) {
        super(message);
    }
}