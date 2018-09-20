package com.ncherkas.codechallenge.hc.k8s.orchestrator.deployments;

import com.google.common.base.Strings;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.ncherkas.codechallenge.hc.k8s.orchestrator.K8sOrchestratorConfig.DEPLOYMENTS_MAP_NAME;
import static java.time.ZoneOffset.UTC;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

/**
 * Deployment entity. Main purpose is to embed the actual {@code io.fabric8.kubernetes.api.model.apps.Deployment} and
 * allow to work with the Spring Data Hazelcast.
 */
@KeySpace(DEPLOYMENTS_MAP_NAME)
public class DeploymentEntity implements Comparable<DeploymentEntity>, Serializable {

    private static final String DEFAULT_NAMESPACE = "default";

    @Id
    private String id;
    private Deployment deployment;
    private Instant createAt;

    public DeploymentEntity() {
    }

    private DeploymentEntity(String id, Deployment deployment, Instant createAt) {
        this.id = id;
        this.deployment = deployment;
        this.createAt = createAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Deployment getDeployment() {
        return deployment;
    }

    public void setDeployment(Deployment deployment) {
        this.deployment = deployment;
    }

    public Instant getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Instant createAt) {
        this.createAt = createAt;
    }

    @Override
    public int compareTo(DeploymentEntity other) {
        return this.createAt.compareTo(other.getCreateAt());
    }

    public static DeploymentEntity of(io.fabric8.kubernetes.api.model.apps.Deployment deployment) {
        checkNotNull(deployment);

        ObjectMeta metadata = deployment.getMetadata();
        if (Strings.isNullOrEmpty(metadata.getNamespace())) {
            // For the kubectl, the namespace is not mandatory though for the Java client it is
            metadata.setNamespace(DEFAULT_NAMESPACE);
        }

        String id = metadata.getNamespace() + "_" + metadata.getName();
        LocalDateTime creationDateTime = metadata.getCreationTimestamp() != null
                ? LocalDateTime.parse(metadata.getCreationTimestamp(), ISO_DATE_TIME)
                : LocalDateTime.now();

        return new DeploymentEntity(id, deployment, creationDateTime.toInstant(UTC));
    }
}
