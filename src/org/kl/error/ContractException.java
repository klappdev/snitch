package org.kl.error;

public class ContractException extends Exception {
    private String message;

    public ContractException(String message) {
        super(message);

        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
