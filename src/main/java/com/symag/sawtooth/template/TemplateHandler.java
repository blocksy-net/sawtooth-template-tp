package com.symag.sawtooth.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.arteam.simplejsonrpc.core.domain.ErrorResponse;
import com.github.arteam.simplejsonrpc.server.JsonRpcServer;

import sawtooth.sdk.processor.State;
import sawtooth.sdk.processor.TransactionHandler;
import sawtooth.sdk.processor.Utils;
import sawtooth.sdk.processor.exceptions.InternalError;
import sawtooth.sdk.processor.exceptions.InvalidTransactionException;
import sawtooth.sdk.protobuf.TpProcessRequest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TemplateHandler implements TransactionHandler {

    private final Logger logger = Logger.getLogger(TemplateHandler.class.getName());
    private String templateNameSpace;

    private JsonRpcServer rpcServer;
    private ObjectMapper mapper;

    /**
     * constructor.
     */
    public TemplateHandler() {
        try {
            this.templateNameSpace = Utils.hash512(
                    this.transactionFamilyName().getBytes("UTF-8")).substring(0, 6);
        } catch (UnsupportedEncodingException usee) {
            usee.printStackTrace();
            this.templateNameSpace = "";
        }
        rpcServer = new JsonRpcServer();
        mapper = new ObjectMapper();
    }

    @Override
    public String transactionFamilyName() {
        return "template";
    }

    @Override
    public String getVersion() {
        return "0.2";
    }

    @Override
    public Collection<String> getNameSpaces() {
        ArrayList<String> namespaces = new ArrayList<>();
        namespaces.add(this.templateNameSpace);
        return namespaces;
    }

    @Override
    public void apply(TpProcessRequest transactionRequest, State stateStore)
            throws InvalidTransactionException, InternalError {

        String signerPublicKey;
        String instanceName;
        String rpcMethod;

        if (transactionRequest.getPayload().size() == 0) {
            throw new InvalidTransactionException("JSON-RPC payload is required.");
        }

        String payload = transactionRequest.getPayload().toStringUtf8();

        try {
            BlocksyCall payloadMap = mapper.readValue(payload, BlocksyCall.class);
            instanceName = (String) payloadMap.instance_name;
            rpcMethod = (String) payloadMap.rpc;
            signerPublicKey = header.getSignerPublicKey();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            throw new InvalidTransactionException("Failed to decode Blocksy's payload");
        }
        if (instanceName.isEmpty()) {
            throw new InvalidTransactionException("instanceName is required");
        }
        if (rpcMethod.isEmpty()) {
            throw new InvalidTransactionException("rpcMethod is required");
        }
        TemplateService templateService = new TemplateService(this.templateNameSpace, instanceName, signerPublicKey, stateStore);

        //call the matching method as specified in JSON-RPC document
        String response = rpcServer.handle(rpcMethod, templateService);

        ErrorResponse errorResponse = null;
        try {
            errorResponse = mapper.readValue(response, ErrorResponse.class);
        } catch (IOException e) {
            //in case of exception, the result is not of ErrorResponse type
        }

        if (errorResponse == null) {
            //no errors
            logger.info("rpc response=" + response);
        } else {
            throw new InvalidTransactionException(errorResponse.getError().getMessage());
        }
    }
}