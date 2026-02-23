package com.clusterpulse.backend.config;

import com.clusterpulse.backend.model.Node;
import com.clusterpulse.backend.repository.NodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private final NodeRepository nodeRepository;

    public DataInitializer(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String[]> nodeData = List.of(
            new String[]{"node-1", "Alpha Node"},
            new String[]{"node-2", "Beta Node"},
            new String[]{"node-3", "Gamma Node"},
            new String[]{"node-4", "Delta Node"},
            new String[]{"node-5", "Epsilon Node"}
        );

        for (String[] data : nodeData) {
            if (nodeRepository.findByNodeId(data[0]).isEmpty()) {
                nodeRepository.save(Node.create(data[0], data[1]));
                logger.info("Seeded node: {}", data[0]);
            } else {
                logger.info("Node already exists, skipping: {}", data[0]);
            }
        }
        logger.info("Data initialization complete.");
    }
}