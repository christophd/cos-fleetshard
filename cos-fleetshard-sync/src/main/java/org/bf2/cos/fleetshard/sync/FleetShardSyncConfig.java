package org.bf2.cos.fleetshard.sync;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

import org.bf2.cos.fleetshard.api.ManagedConnector;
import org.bf2.cos.fleetshard.api.ManagedConnectorCluster;
import org.bf2.cos.fleetshard.api.ManagedConnectorOperator;
import org.bf2.cos.fleetshard.support.DurationConverter;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithConverter;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "cos")
public interface FleetShardSyncConfig {
    /**
     * Configuration options for the {@link ManagedConnectorCluster}
     *
     * @return {@link Cluster}
     */
    Cluster cluster();

    /**
     * Configuration options for operators.
     *
     * @return {@link Operators}
     */
    Operators operators();

    /**
     * Configuration options for connectors.
     *
     * @return {@link Connectors}
     */
    Connectors connectors();

    /**
     * Configuration options for resources.
     *
     * @return {@link Resources}
     */
    Resources resources();

    /**
     * Metrics configuration options.
     *
     * @return {@link Connectors}
     */
    Metrics metrics();

    /**
     * Configuration options for the fleet manager.
     *
     * @return {@link Connectors}
     */
    Manager manager();

    /**
     * Configuration options for the tenancyr.
     *
     * @return {@link Tenancy}
     */
    Tenancy tenancy();

    interface Cluster {
        /**
         * The ID assigned to the operator.
         * </p>
         * This value is used by the operator to create a {@link ManagedConnectorCluster} CR upon startup and it is added to any
         * {@link ManagedConnector} that is created by this synchronizer instance.
         *
         * @return the cluster id.
         */
        String id();

        Status status();

        interface Status {
            /**
             * Define how often the synchronizer should update the status of the {@link ManagedConnectorCluster} to the fleet
             * manager.
             *
             * @return the sync interval.
             */
            @WithDefault("60s")
            String syncInterval();
        }
    }

    interface Operators {
        /**
         * The namespace where {@link ManagedConnectorOperator} are placed.
         *
         * @return the namespace.
         */
        String namespace();
    }

    interface Tenancy {
        /**
         * If tenancy is supported
         *
         * @return the namespace.
         */
        @WithDefault("true")
        boolean enabled();
    }

    interface Resources {
        /**
         * Determine how often the synchronizer should poll the Control Plane for resources
         *
         * @return the poll interval
         */
        @WithDefault("15s")
        @WithConverter(DurationConverter.class)
        Duration pollInterval();

        /**
         * Determine how often the synchronizer should re-sync resources with the Control Plane.
         *
         * @return the re-sync interval
         */
        @WithDefault("60s")
        @WithConverter(DurationConverter.class)
        Duration resyncInterval();
    }

    interface Connectors {

        /**
         * Determine if the synchronizer should watch {@link ManagedConnector} and report the related status to the Control
         * Plane in realtime.
         *
         * @return true if the synchronizer should watch {@link ManagedConnector}
         */
        @WithDefault("true")
        boolean watch();

        Status status();

        /**
         * An optional map of additional labels to be added to the generated {@link ManagedConnector}.
         *
         * @return the additional labels
         */
        Map<String, String> labels();

        /**
         * An optional map of additional annotations to be added to the generated {@link ManagedConnector}.
         *
         * @return the additional annotations
         */
        Map<String, String> annotations();

        interface Status {
            /**
             * Determine how often the synchronizer should sync the status of all the deployed {@link ManagedConnector} to the
             * Control Plane.
             *
             * @return the re-sync interval
             */
            @WithDefault("60s")
            @WithConverter(DurationConverter.class)
            Duration resyncInterval();

            /**
             * Determine how often the synchronizer should update the status of deployed {@link ManagedConnector} to the
             * Control Plane.
             *
             * @return the timeout.
             */
            @WithDefault("1s")
            @WithConverter(DurationConverter.class)
            Duration updateInterval();
        }
    }

    interface Metrics {
        /**
         * The base name for metrics created by the operator.
         *
         * @return the base name.
         */
        @WithDefault("cos.fleetshard.sync")
        String baseName();
    }

    interface Manager {
        /**
         * The {@link URI} of the Control Plane.
         *
         * @return the control plane {@link URI}.
         */
        URI uri();

        /**
         * The connect timeout;
         *
         * @return the timeout.
         */
        @WithDefault("5s")
        @WithConverter(DurationConverter.class)
        Duration connectTimeout();

        /**
         * The read timeout;
         *
         * @return the timeout.
         */
        @WithDefault("10s")
        @WithConverter(DurationConverter.class)
        Duration readTimeout();
    }
}
