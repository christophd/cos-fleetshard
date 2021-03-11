package org.bf2.cos.fleetshard.api.connector.camel;

import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;
import io.sundr.builder.annotations.Buildable;
import org.bf2.cos.fleetshard.api.connector.Connector;

@Buildable(builderPackage = "io.fabric8.kubernetes.api.builder")
@Version("v1alpha1")
@Group("cos.bf2.org")
public class CamelConnector extends Connector<CamelConnectorSpec, CamelConnectorStatus> {
}
