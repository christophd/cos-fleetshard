package org.bf2.cos.fleetshard.api.connector;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.sundr.builder.annotations.Buildable;

@Buildable(builderPackage = "io.fabric8.kubernetes.api.builder")
public abstract class ConnectorStatus {
    private List<Condition> conditions;

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    /**
     * Defines a condition related to the Connector status
     */
    //@Buildable(builderPackage = "io.fabric8.kubernetes.api.builder")
    public static class Condition {
        public enum Type {
            Installing,
            Ready,
            Deleted,
            Error;
        }

        private String type;
        private String reason;
        private String message;
        private String status;
        private String lastTransitionTime;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @JsonInclude(value = JsonInclude.Include.NON_NULL)
        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        @JsonInclude(value = JsonInclude.Include.NON_NULL)
        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getLastTransitionTime() {
            return lastTransitionTime;
        }

        public void setLastTransitionTime(String lastTransitionTime) {
            this.lastTransitionTime = lastTransitionTime;
        }
    }

}
