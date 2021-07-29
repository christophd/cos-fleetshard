package org.bf2.cos.fleetshard.api;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.model.annotation.PrinterColumn;
import io.sundr.builder.annotations.Buildable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Buildable(builderPackage = "io.fabric8.kubernetes.api.builder")
public class DeploymentSpec {

    @PrinterColumn
    private String connectorTypeId;
    private Long connectorResourceVersion;
    private Long deploymentResourceVersion;
    private String desiredState;
    private String secret;
    private String secretChecksum;

    @JsonProperty
    public String getConnectorTypeId() {
        return connectorTypeId;
    }

    @JsonProperty
    public void setConnectorTypeId(String connectorTypeId) {
        this.connectorTypeId = connectorTypeId;
    }

    @JsonProperty
    public Long getConnectorResourceVersion() {
        return connectorResourceVersion;
    }

    @JsonProperty
    public void setConnectorResourceVersion(Long connectorResourceVersion) {
        this.connectorResourceVersion = connectorResourceVersion;
    }

    @JsonProperty
    public Long getDeploymentResourceVersion() {
        return deploymentResourceVersion;
    }

    @JsonProperty
    public void setDeploymentResourceVersion(Long deploymentResourceVersion) {
        this.deploymentResourceVersion = deploymentResourceVersion;
    }

    @JsonProperty
    public String getDesiredState() {
        return desiredState;
    }

    @JsonProperty
    public void setDesiredState(String desiredState) {
        this.desiredState = desiredState;
    }

    @JsonIgnore
    public boolean hasDesiredStateOf(String... desiredStates) {
        Objects.requireNonNull(desiredStates, "desiredState should not be null");

        for (String desiredState : desiredStates) {
            if (Objects.equals(this.desiredState, desiredState)) {
                return true;
            }
        }
        return false;
    }

    @JsonProperty
    public String getSecret() {
        return secret;
    }

    @JsonProperty
    public void setSecret(String secret) {
        this.secret = secret;
    }

    @JsonProperty
    public String getSecretChecksum() {
        return secretChecksum;
    }

    @JsonProperty
    public void setSecretChecksum(String secretChecksum) {
        this.secretChecksum = secretChecksum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeploymentSpec)) {
            return false;
        }
        DeploymentSpec spec = (DeploymentSpec) o;
        return Objects.equals(getConnectorTypeId(), spec.getConnectorTypeId())
            && Objects.equals(getConnectorResourceVersion(), spec.getConnectorResourceVersion())
            && Objects.equals(getDeploymentResourceVersion(), spec.getDeploymentResourceVersion())
            && Objects.equals(getDesiredState(), spec.getDesiredState())
            && Objects.equals(getSecret(), spec.getSecret())
            && Objects.equals(getSecretChecksum(), spec.getSecretChecksum());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            getConnectorTypeId(),
            getConnectorResourceVersion(),
            getDeploymentResourceVersion(),
            getDesiredState(),
            getSecret(),
            getSecretChecksum());
    }

    @Override
    public String toString() {
        return "DeploymentSpec{" +
            "connectorTypeId=" + connectorTypeId +
            ", connectorResourceVersion=" + connectorResourceVersion +
            ", deploymentResourceVersion=" + deploymentResourceVersion +
            ", desiredState='" + desiredState +
            ", secret='" + secret +
            ", secretChecksum='" + secretChecksum +
            '}';
    }
}
