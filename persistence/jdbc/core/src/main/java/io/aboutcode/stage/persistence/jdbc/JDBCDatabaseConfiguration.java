package io.aboutcode.stage.persistence.jdbc;


import com.zaxxer.hikari.HikariConfig;

/**
 * This provides easy access to all JDBC database configurations for known databases.
 */
public interface JDBCDatabaseConfiguration {

    /**
     * Creates a new {@link JDBCDatabaseConfiguration} for MySQL with the specified parameters.
     *
     * @param dataSourceClass The type of datasource to use as driver
     * @param serverName      The name of the server to connect to
     * @param databaseName    The name of the database to connect to
     * @param username        The user to connect with
     * @param password        The password of the database user
     * @param port            The port on which to connect
     *
     * @return An instance of {@link JDBCDatabaseConfiguration}
     */
    static JDBCDatabaseConfiguration MySQL(Class dataSourceClass, String serverName,
                                           String databaseName, String username, String password,
                                           Integer port) {
        return targetConfiguration -> {
            targetConfiguration.setDataSourceClassName(dataSourceClass.getName());
            targetConfiguration.addDataSourceProperty("serverName", serverName);
            if (port != null) {
                targetConfiguration.addDataSourceProperty("port", port);
            }
            targetConfiguration.addDataSourceProperty("databaseName", databaseName);
            targetConfiguration.addDataSourceProperty("user", username);
            targetConfiguration.addDataSourceProperty("password", password);
            return targetConfiguration;
        };
    }

    /**
     * Creates the configuration for the connection pool with the corresponding parameters for the
     * underlying JDBC database.
     *
     * @param targetConfiguration The base configuration
     *
     * @return The resulting configuration
     */
    HikariConfig apply(HikariConfig targetConfiguration);
}
