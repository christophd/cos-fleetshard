package org.bf2.cos.fleetshard.sync.client;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.bf2.cos.fleetshard.sync.FleetShardSyncConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.observability.v1.Observability;
import com.redhat.observability.v1.ObservabilitySpec;
import com.redhat.observability.v1.observabilityspec.ConfigurationSelector;
import com.redhat.observability.v1.observabilityspec.SelfContained;
import com.redhat.observability.v1.observabilityspec.Storage;
import com.redhat.observability.v1.observabilityspec.selfcontained.*;
import com.redhat.observability.v1.observabilityspec.storage.Prometheus;
import com.redhat.observability.v1.observabilityspec.storage.prometheus.VolumeClaimTemplate;
import com.redhat.observability.v1.observabilityspec.storage.prometheus.volumeclaimtemplate.Spec;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.openshift.api.model.operatorhub.v1alpha1.Subscription;
import io.fabric8.openshift.api.model.operatorhub.v1alpha1.SubscriptionBuilder;
import io.fabric8.openshift.api.model.operatorhub.v1alpha1.SubscriptionSpecBuilder;

@ApplicationScoped
public class FleetShardObservabilityClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(FleetShardObservabilityClient.class);

    @Inject
    KubernetesClient kubernetesClient;
    @Inject
    FleetShardSyncConfig config;

    private String observabilityNamespace = "";

    @PostConstruct
    public void init() {
        observabilityNamespace = config.observability().namespace();
    }

    public void setupObservability() {
        if (!config.observability().enabled()) {
            LOGGER.warn("Observability is not enabled.");
            return;
        }

        var observabilityCRD = kubernetesClient.resources(CustomResourceDefinition.class)
            .withName("observabilities.observability.redhat.com")
            .get();
        if (observabilityCRD == null) {
            LOGGER.error("Observability CustomResourceDefinition is not present. Observability will not be enabled.");
            return;
        }

        if (observabilityNamespace.isBlank()) {
            LOGGER.error("Observability namespace is not set!");
            return;
        }

        if (config.observability().subscription().enabled()) {
            createSubscriptionResource();
        }
        copyResourcesToObservabilityNamespace();
        createObservabilityResource();
    }

    private void createSubscriptionResource() {
        LOGGER.info("Creating Subscription resource");
        FleetShardSyncConfig.Observability.Subscription subscriptionConfig = config.observability().subscription();

        Subscription subscription = new SubscriptionBuilder()
            .withMetadata(
                new ObjectMetaBuilder()
                    .withName(subscriptionConfig.name())
                    .withNamespace(observabilityNamespace)
                    .build())
            .withSpec(
                new SubscriptionSpecBuilder()
                    .withName(subscriptionConfig.name())
                    .withChannel(subscriptionConfig.channel())
                    .withInstallPlanApproval(subscriptionConfig.installPlanApproval())
                    .withSource(subscriptionConfig.source())
                    .withSourceNamespace(subscriptionConfig.sourceNamespace())
                    .withStartingCSV(subscriptionConfig.startingCsv())
                    .build())
            .build();

        kubernetesClient.resources(Subscription.class)
            .inNamespace(subscription.getMetadata().getNamespace())
            .withName(subscription.getMetadata().getName())
            .createOrReplace(subscription);
        LOGGER.info("Subscription resource created");
    }

    private void copyResourcesToObservabilityNamespace() {
        LOGGER.info("Copying resources to observability namespace");

        config.observability().configMapsToCopy().ifPresent(resources -> resources.forEach(this::copyConfigMap));
        config.observability().secretsToCopy().ifPresent(resources -> resources.forEach(this::copySecret));

        LOGGER.info("Observability resources copied to the target namespace: {}", observabilityNamespace);
    }

    private void copyConfigMap(String resourceName) {
        ConfigMap resource = kubernetesClient.resources(ConfigMap.class)
            .inNamespace(config.namespace())
            .withName(resourceName)
            .get();

        if (resource == null) {
            String msg = String.format("Observability ConfigMap not found to be copied: %s/%s", config.namespace(),
                resourceName);
            throw new IllegalArgumentException(msg);
        }

        resource.getMetadata().setNamespace(observabilityNamespace);
        kubernetesClient.resources(ConfigMap.class)
            .inNamespace(resource.getMetadata().getNamespace())
            .withName(resourceName)
            .createOrReplace(resource);
    }

    private void copySecret(String resourceName) {
        Secret resource = kubernetesClient.resources(Secret.class)
            .inNamespace(config.namespace())
            .withName(resourceName)
            .get();

        if (resource == null) {
            String msg = String.format("Observability Secret not found to be copied: %s/%s", config.namespace(), resourceName);
            throw new IllegalArgumentException(msg);
        }

        resource.getMetadata().setNamespace(observabilityNamespace);
        kubernetesClient.resources(Secret.class)
            .inNamespace(resource.getMetadata().getNamespace())
            .withName(resourceName)
            .createOrReplace(resource);
    }

    private void createObservabilityResource() {
        LOGGER.info("Creating Observability resource");
        final var observability = new Observability();

        final var meta = new ObjectMetaBuilder()
            .withName(config.observability().resourceName())
            .withNamespace(observabilityNamespace)
            .withFinalizers(config.observability().finalizer())
            .build();
        observability.setMetadata(meta);

        final var spec = new ObservabilitySpec();
        spec.setClusterId(config.cluster().id());

        final var configurationSelector = new ConfigurationSelector();
        configurationSelector.setMatchLabels(Map.of("configures", config.observability().configuresMatchLabel()));
        spec.setConfigurationSelector(configurationSelector);

        spec.setResyncPeriod(config.observability().resyncPeriod());
        spec.setRetention(config.observability().retention());

        final var selfContained = new SelfContained();
        selfContained.setDisablePagerDuty(false);

        Map<String, String> rhocAppLabel = Map.of("app", "rhoc");

        final var grafanaDashboardLS = new GrafanaDashboardLabelSelector();
        grafanaDashboardLS.setMatchLabels(rhocAppLabel);
        selfContained.setGrafanaDashboardLabelSelector(grafanaDashboardLS);

        final var podMonitorLS = new PodMonitorLabelSelector();
        podMonitorLS.setMatchLabels(rhocAppLabel);
        selfContained.setPodMonitorLabelSelector(podMonitorLS);

        final var ruleLS = new RuleLabelSelector();
        ruleLS.setMatchLabels(rhocAppLabel);
        selfContained.setRuleLabelSelector(ruleLS);

        final var serviceMonitorLS = new ServiceMonitorLabelSelector();
        serviceMonitorLS.setMatchLabels(rhocAppLabel);
        selfContained.setServiceMonitorLabelSelector(serviceMonitorLS);

        final var probeSelector = new ProbeSelector();
        probeSelector.setMatchLabels(rhocAppLabel);
        selfContained.setProbeSelector(probeSelector);

        spec.setSelfContained(selfContained);

        final var storage = new Storage();
        final var prometheus = new Prometheus();
        final var volumeClaimTemplate = new VolumeClaimTemplate();
        final var volumeClaimTemplateSpec = new Spec();
        final var resources = new com.redhat.observability.v1.observabilityspec.storage.prometheus.volumeclaimtemplate.spec.Resources();
        resources.setRequests(Map.of("storage", new IntOrString(config.observability().storageRequest())));
        volumeClaimTemplateSpec.setResources(resources);
        volumeClaimTemplate.setSpec(volumeClaimTemplateSpec);
        prometheus.setVolumeClaimTemplate(volumeClaimTemplate);
        storage.setPrometheus(prometheus);

        spec.setStorage(storage);

        observability.setSpec(spec);

        kubernetesClient.resources(Observability.class)
            .inNamespace(observability.getMetadata().getNamespace())
            .withName(observability.getMetadata().getName())
            .createOrReplace(observability);
        LOGGER.info("Observability resource created");
    }

}
