package org.blockchainElo.server.util.custom_exceptions;

public class NetworkIsFull extends Exception {
    public NetworkIsFull(String errorMessage) {
        super(errorMessage);
    }
}
