package com.clusterpulse.backend.model;

import java.util.Deque;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Holds the sliding window of metrics and its dedicated lock for a single node.
 */
public final class NodeWindow {

    private final Deque<NodeMetrics> deque;
    private final ReentrantLock lock = new ReentrantLock();

    public NodeWindow(Deque<NodeMetrics> deque) {
        this.deque = deque;
    }

    public Deque<NodeMetrics> getDeque() {
        return deque;
    }

    public ReentrantLock getLock() {
        return lock;
    }
}
