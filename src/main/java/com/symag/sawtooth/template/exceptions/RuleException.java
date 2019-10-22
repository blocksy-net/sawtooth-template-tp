package com.symag.sawtooth.template.exceptions;

import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcError;

@JsonRpcError(code = -32035)
public class RuleException extends Exception {
    public RuleException(String message) {
        super(message);
    }
}