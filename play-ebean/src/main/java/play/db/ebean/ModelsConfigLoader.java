package play.db.ebean;

import play.Configuration;
import play.Environment;
import play.Mode;

import java.io.File;
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
        // Using TEST mode is the only way to load configuration without failing if application.conf doesn't exist
        final Environment env = new Environment(new File("."), classLoader, Mode.TEST);
        final Configuration config = Configuration.load(env);
        return EbeanParsedConfig.parseFromConfig(config).getDatasourceModels();
    }
}
