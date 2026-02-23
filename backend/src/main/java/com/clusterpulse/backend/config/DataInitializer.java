package com.clusterpulse.backend.config;
import com.clusterpulse.backend.model.Node;
import com.clusterpulse.backend.repository.NodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final NodeRepository nodeRepository;

    public DataInitializer(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        long nodeCount = nodeRepository.count();
        if (nodeCount > 0) {
            logger.info("Nodes already exist in the database. Skipping seeding.");
            return;
        }

        List<Node> nodes = List.of(
                Node.builder()
                        .id(UUID.randomUUID())
                        .nodeId("node-1")
                        .nodeName("Alpha Node")
                        .region("us-east-1")
                        .status(Node.Status.HEALTHY)
                        .createdDate(Instant.now())
                        .build(),
                Node.builder()
                        .id(UUID.randomUUID())
                        .nodeId("node-2")
                        .nodeName("Beta Node")
                        .region("us-east-1")
                        .status(Node.Status.HEALTHY)
                        .createdDate(Instant.now())
                        .build(),
                Node.builder()
                        .id(UUID.randomUUID())
                        .nodeId("node-3")
                        .nodeName("Gamma Node")
                        .region("us-east-1")
                        .status(Node.Status.HEALTHY)
                        .createdDate(Instant.now())
                        .build(),
                Node.builder()
                        .id(UUID.randomUUID())
                        .nodeId("node-4")
                        .nodeName("Delta Node")
                        .region("us-east-1")
                        .status(Node.Status.HEALTHY)
                        .createdDate(Instant.now())
                        .build(),
                Node.builder()
                        .id(UUID.randomUUID())
                        .nodeId("node-5")
                        .nodeName("Epsilon Node")
                        .region("us-east-1")
                        .status(Node.Status.HEALTHY)
                        .createdDate(Instant.now())
                        .build()
        );
        nodeRepository.saveAll(nodes);
        logger.info("Seeded 5 initial nodes to the database: {}", nodes.stream().map(Node::getNodeId).toList());
    }
}