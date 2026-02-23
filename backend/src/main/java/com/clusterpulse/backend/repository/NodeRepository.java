package com.clusterpulse.backend.repository;

import com.clusterpulse.backend.model.Node;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NodeRepository extends JpaRepository<Node, UUID> {
    Optional<Node> findByNodeId(String nodeId);
}