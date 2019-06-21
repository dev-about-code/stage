package io.aboutcode.stage.persistence.jdbc;


import com.zaxxer.hikari.HikariConfig;

/**
 * This provides easy access to all JDBC database configurations for known databases.
 */
public interface JDBCDatabaseConfiguration {
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
