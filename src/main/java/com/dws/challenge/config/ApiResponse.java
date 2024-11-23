package com.dws.challenge.config;

import java.util.UUID;

/*
*   Adding this class to maintain common API Response Structure,
*   For now added this for transfer method or task specific requirement.
*   This code should be place in Global Code so that is can be reused across all of microservices
*/

public class ApiResponse {
    private final String requestId;
    private final String message;

    public ApiResponse(String message) {
        this.requestId = UUID.randomUUID().toString();
        this.message = message;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getMessage() {
        return message;
    }
}