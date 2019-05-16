package io.aboutcode.stage.persistence.orm.jooq;

import io.aboutcode.stage.component.BaseComponent;
import io.aboutcode.stage.dependency.DependencyContext;
import io.aboutcode.stage.dependency.DependencyException;
import io.aboutcode.stage.persistence.Persistence;
import io.aboutcode.stage.persistence.jdbc.JDBCPersistence;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.io.IOException;

/**
 * <p>This component creates a wrapper around an existing {@link JDBCPersistence} instance in a
 * {@link io.aboutcode.stage.component.ComponentContainer}. When resolving this component in other units, it can be used
 * to execute commands on the underlying datasource through Jooq.</p>
 *
 * <p><em>Note</em> that this class is work in progress</p>
 */
public final class JooqPersistence extends BaseComponent implements Persistence<DSLContext> {
    private final SQLDialect targetDialect;
    private final Object persistenceIdentifier;
    private JDBCPersistence jdbcPersistence;

    /**
     * Creates a new instance with the target dialect Jooq should use.
     *
     * @param persistenceIdentifier The identifier for the internal {@link JDBCPersistence} that should be used
     * @param targetDialect         The dialect Jooq should use to create SQL statements
     */
    public JooqPersistence(Object persistenceIdentifier, SQLDialect targetDialect) {
        this.targetDialect = targetDialect;
        this.persistenceIdentifier = persistenceIdentifier;
    }

    /**
     * Creates a new instance with the target dialect Jooq should use.
     *
     * @param targetDialect The dialect Jooq should use to create SQL statements
     */
    public JooqPersistence(SQLDialect targetDialect) {
        this(null, targetDialect);
    }

    /**
     * Creates a new instance with the default dialect.
     *
     * @param persistenceIdentifier The identifier for the internal {@link JDBCPersistence} that should be used
     */
    public JooqPersistence(Object persistenceIdentifier) {
        this(persistenceIdentifier, SQLDialect.DEFAULT);
    }

    /**
     * Creates a new instance with the default dialect and persistence identifier.
     */
    public JooqPersistence() {
        this(null);
    }

    /**
     * Returns the underlying {@link JDBCPersistence} unit
     *
     * @return The underlying jdbc persistence unit
     */
    public JDBCPersistence getJdbcPersistence() {
        return jdbcPersistence;
    }

    @Override
    public <ResultT, ExceptionT extends Exception> ResultT apply(
            PersistenceFunction<DSLContext, ResultT, ExceptionT> function,
            PersistenceHint... persistenceHints) throws IOException, ExceptionT {
        return jdbcPersistence.apply(session -> function.execute(DSL.using(session, targetDialect)));
    }

    @Override
    public <ExceptionT extends Exception> void execute(
            PersistenceAction<DSLContext, ExceptionT> action, PersistenceHint... persistenceHints)
            throws IOException, ExceptionT {
        jdbcPersistence.execute(session -> action.execute(DSL.using(session, targetDialect)));
    }

    @Override
    public void resolve(DependencyContext context) throws DependencyException {
        if (persistenceIdentifier == null) {
            jdbcPersistence = context.retrieveDependency(JDBCPersistence.class);
        } else {
            jdbcPersistence = context.retrieveDependency(persistenceIdentifier, JDBCPersistence.class, true);
        }
    }
}
