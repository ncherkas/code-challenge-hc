package com.ncherkas.codechallenge.hc.k8s.orchestrator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.ncherkas.codechallenge.hc.k8s.orchestrator.deployments.DeploymentEntity;
import com.ncherkas.codechallenge.hc.k8s.orchestrator.deployments.DeploymentsApiMapStore;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.hazelcast.repository.config.EnableHazelcastRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Java config for the Spring IoC.
 */
@Configuration
@EnableHazelcastRepositories
public class K8sOrchestratorConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(K8sOrchestratorConfig.class);

    public static final String DEPLOYMENTS_MAP_NAME = "k8s_deployments";

    @Bean
    public KubernetesClient kubernetesClient() {
        return new DefaultKubernetesClient(); // TODO: any additional configs?
    }

    @Bean
    public Watch deploymentEventsWatch(KubernetesClient KubernetesClient, HazelcastInstance hazelcastInstance) {
        // This is done in order to keep the in-memory state consistent with API,
        // especially when we create new deployment and it needs some time to roll-out
        LOGGER.info("Starting the deployments watcher...");
        IMap<String, DeploymentEntity> deploymentsMap = hazelcastInstance.getMap(DEPLOYMENTS_MAP_NAME);
        return KubernetesClient.apps().deployments()
                .watch(new DeploymentsWatcher(deploymentsMap));
    }

    @Bean
    public YAMLMapper yamlMapper() {
        return new YAMLMapper();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public Config hazelcastConfig(@Value("${k8s.orchestrator.api.hz.mancenter.enabled}") boolean enableMancenter,
                                  @Value("${k8s.orchestrator.api.hz.mancenter.url}") String mancenterUrl,
                                  DeploymentsApiMapStore deploymentsApiMapStore) {

        Config config = new Config();

        config.getManagementCenterConfig()
                .setEnabled(enableMancenter)
                .setUrl(mancenterUrl);

        MapConfig mapConfig = config.getMapConfig(DEPLOYMENTS_MAP_NAME);
        MapStoreConfig mapStoreConfig = new MapStoreConfig()
                .setEnabled(true)
                .setImplementation(deploymentsApiMapStore);
        mapConfig.setMapStoreConfig(mapStoreConfig);

        return config;
    }

    @Bean
    public HazelcastInstance hazelcastInstance(Config config) {
        return Hazelcast.newHazelcastInstance(config);
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public static class DeploymentsWatcher implements Watcher<Deployment> {

        private final IMap<String, DeploymentEntity> deploymentsMap;

        public DeploymentsWatcher(IMap<String, DeploymentEntity> deploymentsMap) {
            this.deploymentsMap = checkNotNull(deploymentsMap);
        }

        @Override
        public void eventReceived(Watcher.Action action, Deployment deployment) {
            DeploymentEntity deploymentEntity = DeploymentEntity.of(deployment);
            String key = deploymentEntity.getId();

            if (action == Action.MODIFIED && deploymentsMap.localKeySet().contains(key)) {
                // We're evicting and refreshing the value stored by the Hazelcast
                deploymentsMap.loadAll(Collections.singleton(key), true);
                LOGGER.info("event {} for: {}", action.name(), deployment);
                try {
                    // Let's sleep for a while since there can be multiple events fired one-by-one
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while sleeping", e);
                }
            }
        }

        @Override
        public void onClose(KubernetesClientException cause) {
            LOGGER.info("Watcher close due to {}", cause);
        }
    }
}
