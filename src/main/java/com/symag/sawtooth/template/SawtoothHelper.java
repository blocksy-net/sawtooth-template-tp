package com.symag.sawtooth.template;

import com.google.protobuf.ByteString;
import com.symag.sawtooth.template.exceptions.ReadWriteStateException;
import sawtooth.sdk.processor.State;
import sawtooth.sdk.processor.Utils;
import sawtooth.sdk.processor.exceptions.InternalError;
import sawtooth.sdk.processor.exceptions.InvalidTransactionException;

import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;

public class SawtoothHelper {
    /**
     * The implementation that should return a unique address as per the payload
     * that is requested. The implementers have to take care of adding the necessary
     * logic for uniqueness and durability.
     *
     * Note : For sender this evaluation for a payload need to be calculated the
     * same way for input and output transaction address
     *
     * @param id
     * @return unique state address
     */
    public static String getUniqueAddress(String transactionFamilyNameSpace, String instanceName, String entity, String id) {
        String hashedName = Utils.hash512(instanceName.concat("_").concat(entity).concat("_").concat(id).getBytes(StandardCharsets.UTF_8));
        return transactionFamilyNameSpace + hashedName.substring(hashedName.length() - 64);
    }

    /**
     * Helper function to encode the State that will be stored at the address of the name.
     *
     * The implementation doesn't do any encoding and add the data with java Serialized bytes.
     */
    public static Map.Entry<String, ByteString> encodeState(String address, String data) {

        return new AbstractMap.SimpleEntry<String, ByteString>(address, ByteString.copyFrom(data.getBytes()));
    }
    /**
     * Helper function to decode State retrieved from the address of the name.
     */
    public static String decodeState(Map<String, ByteString> addressValues) {
        String decodedValue = "";

        if (addressValues.size() > 0) {
            decodedValue = addressValues.values().iterator().next().toString(StandardCharsets.UTF_8);
        }
        return decodedValue;
    }

    /**
     * Helper method to set a state value
     * @param addressValues
     * @throws ReadWriteStateException
     */
    public static void setState(State state, Collection<Map.Entry<String, ByteString>> addressValues) throws ReadWriteStateException {
        try {
            Collection<String> addresses = state.setState(addressValues);
            if (addresses.isEmpty()) {
                throw new ReadWriteStateException("Failed to set state, data size is zero.");
            }
        } catch (InternalError | InvalidTransactionException e) {
            throw new ReadWriteStateException("Failed to set state.");
        }
    }

    /**
     * Helper method to get a state value
     * @param addresses
     * @return
     * @throws ReadWriteStateException
     */
    public static Map<String, ByteString> getState(State state, Collection<String> addresses) throws ReadWriteStateException {
        try {
            return state.getState(addresses);
        } catch (InternalError | InvalidTransactionException e) {
            throw new ReadWriteStateException("Failed to get state.");
        }
    }

}
