/*
 * Copyright 2020, EnMasse authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.enmasse.controller;

import io.enmasse.address.model.AddressSpace;
import io.enmasse.admin.model.v1.InfraConfig;
import io.enmasse.admin.model.v1.StandardInfraConfig;
import io.enmasse.config.AnnotationKeys;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.policy.PodDisruptionBudget;
import io.fabric8.kubernetes.api.model.policy.PodDisruptionBudgetBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PodDistruptionBudgetController implements Controller {
    private static final Logger log = LoggerFactory.getLogger(PodDistruptionBudgetController.class);

    private final NamespacedKubernetesClient client;
    private final String namespace;

    public PodDistruptionBudgetController(NamespacedKubernetesClient client, String namespace) {
        this.client = client;
        this.namespace = namespace;
    }

    public AddressSpace reconcileActive(AddressSpace addressSpace) throws Exception {
        InfraConfig infraConfig = InfraConfigs.parseCurrentInfraConfig(addressSpace);

        if (infraConfig instanceof StandardInfraConfig) {
            RouterSet routerSet = RouterSet.create(namespace, addressSpace, client);
            reconcileRouterPodDisruptionBudget(addressSpace, routerSet, (StandardInfraConfig) infraConfig);
            reconcileBrokerPodDisruptionBudget(addressSpace, (StandardInfraConfig) infraConfig);
        }
        return addressSpace;
    }

    private static boolean needsUpdate(IntOrString existing, Integer updated) {
        return updated != null && (existing == null || !updated.equals(existing.getIntVal()));
    }


    private void reconcileRouterPodDisruptionBudget(AddressSpace addressSpace, RouterSet routerSet, StandardInfraConfig infraConfig) {
        if (infraConfig.getSpec() != null && infraConfig.getSpec().getRouter() != null && (infraConfig.getSpec().getRouter().getMinAvailable() != null || infraConfig.getSpec().getRouter().getMaxUnavailable() != null)) {
            String name = String.format("enmasse.%s.%s.qdrouterd", addressSpace.getMetadata().getNamespace(), addressSpace.getMetadata().getName());
            try {
                boolean changed = false;
                PodDisruptionBudget podDisruptionBudget = client.inNamespace(namespace).policy().podDisruptionBudget().withName(name).get();
                if (podDisruptionBudget == null) {
                    podDisruptionBudget = new PodDisruptionBudgetBuilder()
                            .editOrNewMetadata()
                            .withName(name)
                            .addToLabels("app", "enmasse")
                            .endMetadata()
                            .editOrNewSpec()
                            .endSpec()
                            .build();
                    changed = true;
                }

                Integer minAvailable = infraConfig.getSpec().getRouter().getMinAvailable();
                if (needsUpdate(podDisruptionBudget.getSpec().getMinAvailable(), minAvailable)) {
                    podDisruptionBudget.getSpec().setMinAvailable(new IntOrString(minAvailable));
                    changed = true;
                }

                Integer maxUnavailable = infraConfig.getSpec().getRouter().getMaxUnavailable();
                if (needsUpdate(podDisruptionBudget.getSpec().getMaxUnavailable(), maxUnavailable)) {
                    podDisruptionBudget.getSpec().setMaxUnavailable(new IntOrString(maxUnavailable));
                    changed = true;
                }

                if (!routerSet.getStatefulSet().getSpec().getSelector().equals(podDisruptionBudget.getSpec().getSelector())) {
                    podDisruptionBudget.getSpec().setSelector(routerSet.getStatefulSet().getSpec().getSelector());
                    changed = true;
                }

                if (changed) {
                    client.inNamespace(namespace).policy().podDisruptionBudget().createOrReplace(podDisruptionBudget);
                }
            } catch (KubernetesClientException e) {
                log.warn("Error creating pod distruption budget for router", e);
            }
        }
    }

    private void reconcileBrokerPodDisruptionBudget(AddressSpace addressSpace, StandardInfraConfig infraConfig) {
        if (infraConfig.getSpec() != null && infraConfig.getSpec().getBroker() != null &&
                (infraConfig.getSpec().getBroker().getMinAvailable() != null || infraConfig.getSpec().getBroker().getMaxUnavailable() != null)) {

            String name = String.format("enmasse.%s.%s.broker", addressSpace.getMetadata().getNamespace(), addressSpace.getMetadata().getName());
            String infraUuid = addressSpace.getAnnotation(AnnotationKeys.INFRA_UUID);
            try {
                boolean changed = false;
                PodDisruptionBudget podDisruptionBudget = client.inNamespace(namespace).policy().podDisruptionBudget().withName(name).get();
                if (podDisruptionBudget == null) {
                    podDisruptionBudget = new PodDisruptionBudgetBuilder()
                            .editOrNewMetadata()
                            .withName(name)
                            .addToLabels("app", "enmasse")
                            .endMetadata()
                            .editOrNewSpec()
                            .withNewSelector()
                            .addToMatchLabels("app", "enmasse")
                            .addToMatchLabels("role", "broker")
                            .addToMatchLabels("infraType", "standard")
                            .addToMatchLabels("infraUuid", infraUuid)
                            .endSelector()
                            .endSpec()
                            .build();
                    changed = true;
                }

                Integer minAvailable = infraConfig.getSpec().getRouter().getMinAvailable();
                if (needsUpdate(podDisruptionBudget.getSpec().getMinAvailable(), minAvailable)) {
                    podDisruptionBudget.getSpec().setMinAvailable(new IntOrString(minAvailable));
                    changed = true;
                }

                Integer maxUnavailable = infraConfig.getSpec().getRouter().getMaxUnavailable();
                if (needsUpdate(podDisruptionBudget.getSpec().getMaxUnavailable(), maxUnavailable)) {
                    podDisruptionBudget.getSpec().setMaxUnavailable(new IntOrString(maxUnavailable));
                    changed = true;
                }

                if (changed) {
                    client.inNamespace(namespace).policy().podDisruptionBudget().createOrReplace(podDisruptionBudget);
                }
            } catch (KubernetesClientException e) {
                log.warn("Error creating pod distruption budget for broker", e);
            }
        }
    }
}