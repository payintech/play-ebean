package play.db.ebean.orm;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Given a classloader, load the models configuration.
 * <p>
 * This is used by the ebean sbt plugin to get the same
 * models configuration that will be loaded by the app.
 * </p>
 *
 * @since 15.04.30
 */
public class ModelsConfigLoader implements Function<ClassLoader, Map<String, List<String>>> {

    @Override
    public Map<String, List<String>> apply(final ClassLoader classLoader) {
        final Map<String, List<String>> datasourceModels = new HashMap<>();
        final Config config = ConfigFactory.load(classLoader);

        if (config.hasPathOrNull("ebean.servers")) {
            final Config ebeanServersConfig = config.getConfig("ebean.servers");
            ebeanServersConfig.root().keySet().forEach(serverName -> {
                final Config ebeanServerConfig = ebeanServersConfig.getConfig(serverName);
                if (ebeanServerConfig.hasPath("enhancement")) {
                    datasourceModels.put(
                        serverName,
                        ebeanServerConfig.getStringList("enhancement")
                    );
                }
            });
        }

        return datasourceModels;
    }
}
