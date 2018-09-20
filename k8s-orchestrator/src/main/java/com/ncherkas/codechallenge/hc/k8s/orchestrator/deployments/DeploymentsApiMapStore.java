package com.ncherkas.codechallenge.hc.k8s.orchestrator.deployments;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.base.Functions;
import com.hazelcast.core.MapStore;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toMap;

/**
 * Hazelcast Map Store backed by the K8S API. All read/write operations invoked on {@code DeploymentsRepository} end up
 * here and then get forwarded to the K8S Java client.
 */
@Component
public class DeploymentsApiMapStore implements MapStore<String, DeploymentEntity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentsApiMapStore.class);

    private final KubernetesClient kubernetesClient;
    private final YAMLMapper yamlMapper;

    @Autowired
    public DeploymentsApiMapStore(KubernetesClient kubernetesClient, YAMLMapper yamlMapper) {
        this.kubernetesClient = checkNotNull(kubernetesClient);
        this.yamlMapper = checkNotNull(yamlMapper);
    }

    @Override
    public void store(String key, DeploymentEntity deploymentEntity) {
        checkNotNull(deploymentEntity);
        LOGGER.debug("Storing deployment entity by the key '{}'", key);
        try {
            Deployment deployment = deploymentEntity.getDeployment();
            LOGGER.debug("Deployment: " + deployment);

            String yaml = yamlMapper.writeValueAsString(deployment);
            LOGGER.debug("YAML: " + yaml);

            // Creating the {@code Deployment} instance out of YAML template and passing it to the K8S API
            kubernetesClient.apps().deployments()
                    .load(new ByteArrayInputStream(yaml.getBytes()))
                    .create();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize into YAML: " + deploymentEntity, e);
        }
    }

    @Override
    public void storeAll(Map<String, DeploymentEntity> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAll(Collection<String> keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DeploymentEntity load(String key) {
        LOGGER.debug("Loading deployment entity by the key '{}'", key);
        return getAllEntities().get(key);
    }

    @Override
    public Map<String, DeploymentEntity> loadAll(Collection<String> keys) {
        LOGGER.debug("Loading deployment entity by the keys '{}'", keys);
        Map<String, DeploymentEntity> deployments = getAllEntities();
        return deployments.entrySet().stream()
                .filter(e -> keys.contains(e.getKey()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Iterable<String> loadAllKeys() {
        // Hazelcast Map Store works in the way the 1st we get all keys and
        // then loads the data in parallel using {@code loadAll}
        LOGGER.debug("Loading keys for all deployment entities");
        return getAllEntities().keySet();
    }

    private Map<String, DeploymentEntity> getAllEntities() {
        return kubernetesClient.apps().deployments().list().getItems().stream()
                .map(DeploymentEntity::of)
                .collect(toMap(DeploymentEntity::getId, Functions.identity()));
    }
}
