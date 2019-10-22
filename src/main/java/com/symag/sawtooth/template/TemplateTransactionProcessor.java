package com.symag.sawtooth.template;

import sawtooth.sdk.processor.TransactionProcessor;
import java.util.logging.Logger;

public class TemplateTransactionProcessor {
    private final static Logger logger = Logger.getLogger(TemplateTransactionProcessor.class.getName());
    /**
     * the method that runs a Thread with a TransactionProcessor in it.
     */
    public static void main(String[] args) {
        TransactionProcessor transactionProcessor = new TransactionProcessor(args[0]);
        transactionProcessor.addHandler(new TemplateHandler());
        Thread thread = new Thread(transactionProcessor);
        thread.start();
    }
}
