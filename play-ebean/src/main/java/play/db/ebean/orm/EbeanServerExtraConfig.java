package play.db.ebean.orm;

import com.typesafe.config.Config;
import io.ebean.config.DatabaseConfig;

/**
 * Allow third party app to configure Ebean server.
 *
 * @author Thibault Meyer
 * @since 18.12.20
 */
public interface EbeanServerExtraConfig {

    /**
     * Apply extra configuration to the Ebean server before it was started.
     *
     * @param ebeanServerConfig Handle to the Ebean server configuration
     * @param playConfig        Handle to Play configuration
     */
    void applyExtraConfiguration(final DatabaseConfig ebeanServerConfig, final Config playConfig);
}
