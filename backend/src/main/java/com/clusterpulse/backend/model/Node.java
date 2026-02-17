package com.clusterpulse.backend.model;

public record Node(String nodeId, String nodeName, String region, Status status) {

    public static final Status DEFAULT_STATUS = Status.HEALTHY;
    public static final String DEFAULT_REGION = "us-east-1";

    public static Node create(String nodeId, String nodeName) {
        return new Node(nodeId, nodeName, DEFAULT_REGION, Status.HEALTHY);
    }

    public enum Status {
        HEALTHY,
        UNHEALTHY,
        DEGRADED
    }
}
