package com.symag.sawtooth.template;

import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcMethod;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcParam;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcService;
import com.google.protobuf.ByteString;
import com.symag.sawtooth.template.exceptions.InstanceAlreadyInitializedException;
import com.symag.sawtooth.template.exceptions.ReadWriteStateException;
import com.symag.sawtooth.template.exceptions.TransactionOwnerException;
import sawtooth.sdk.processor.State;
import sawtooth.sdk.processor.exceptions.InvalidTransactionException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

@JsonRpcService
class TemplateService {
    private String transactionFamilyNameSpace;
    private String signerPublicKey;
    private String instanceName;
    private State state;

    private final Logger logger = Logger.getLogger(TemplateService.class.getName());

    private static final String PREFIX_ADR_INSTANCE = "instance";

    TemplateService(String transactionFamilyNameSpace, String instanceName, String signerPublicKey, State state) throws InvalidTransactionException {
        this.transactionFamilyNameSpace = transactionFamilyNameSpace;
        this.instanceName = instanceName;
        this.signerPublicKey = signerPublicKey;
        this.state = state;
    }

    /**
     * Init a new contract instance
     * @param ownerPublicKey
     * @throws TransactionOwnerException
     * @throws InstanceAlreadyInitializedException
     * @throws ReadWriteStateException
     */
    @JsonRpcMethod
    public void init(@JsonRpcParam("ownerPublicKey") String ownerPublicKey) throws TransactionOwnerException, InstanceAlreadyInitializedException, ReadWriteStateException {
        logger.info("called init !");
        if (!this.signerPublicKey.equals(ownerPublicKey)) {
            throw new TransactionOwnerException();
        }

        //Check if instance is already initialized ?
        String ownerPublicKeyAddress = SawtoothHelper.getUniqueAddress(transactionFamilyNameSpace, instanceName, PREFIX_ADR_INSTANCE, "ownerPublicKey");
        Map<String, ByteString> result = null;
        result = SawtoothHelper.getState(state, Collections.singletonList(ownerPublicKeyAddress));
        if (! result.get(ownerPublicKeyAddress).isEmpty() ) {
            throw new InstanceAlreadyInitializedException("Instance " + instanceName + " is already initialized.");
        }
        Collection<Map.Entry<String, ByteString>> addressValues = Arrays.asList(
                SawtoothHelper.encodeState(SawtoothHelper.getUniqueAddress(transactionFamilyNameSpace, instanceName, PREFIX_ADR_INSTANCE, "ownerPublicKey"), ownerPublicKey)
        );

        SawtoothHelper.setState(state, addressValues);
    }



}
