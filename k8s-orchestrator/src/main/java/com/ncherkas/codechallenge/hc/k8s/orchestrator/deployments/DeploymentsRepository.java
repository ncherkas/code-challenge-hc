package com.ncherkas.codechallenge.hc.k8s.orchestrator.deployments;

import org.springframework.data.hazelcast.repository.HazelcastRepository;

public interface DeploymentsRepository extends HazelcastRepository<DeploymentEntity, String> {
}
