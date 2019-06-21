package io.aboutcode.stage.persistence.neo4j.bolt;


import org.neo4j.driver.v1.Driver;

/**
 * <p>Implementations of this allow configurtion of BOLT Neo4J database connections.</p>
 */
public interface BoltNeo4JDatabaseConfiguration {
    /**
     * Creates the database {@link Driver} with the corresponding parameters for the underlying
     * Neo4J database.
     *
     * @return The resulting configuration
     */
    Driver apply();
}
