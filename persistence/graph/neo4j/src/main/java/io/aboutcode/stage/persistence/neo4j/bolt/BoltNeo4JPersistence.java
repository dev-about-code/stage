package io.aboutcode.stage.persistence.neo4j.bolt;

import io.aboutcode.stage.component.BaseComponent;
import io.aboutcode.stage.persistence.Persistence;
import java.io.IOException;
import java.util.stream.Stream;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;

/**
 * An implementation of {@link Persistence} that allows access to a Neo4J graph database.
 */
public class BoltNeo4JPersistence extends BaseComponent implements Persistence<Transaction> {
    private BoltNeo4JDatabaseConfiguration databaseConfiguration;
    private Driver driver;

    /**
     * Creates a new instance with the specified configuration.
     *
     * @param databaseConfiguration The configuration for the underlying database
     */
    public BoltNeo4JPersistence(BoltNeo4JDatabaseConfiguration databaseConfiguration) {
        this.databaseConfiguration = databaseConfiguration;
    }

    private static boolean readOnly(PersistenceHint... persistenceHints) {
        return Stream.of(persistenceHints).anyMatch(hint -> hint == PersistenceHint.ReadOnly);
    }

    private static <T, ExceptionT extends Exception> CatchingTransactionWork<T, ExceptionT> wrap(
            PersistenceFunction<Transaction, T, ExceptionT> operation) {
        return new CatchingTransactionWork<>(operation);
    }

    @Override
    protected void init() {
        driver = databaseConfiguration.apply();
    }

    @Override
    public <ResultT, ExceptionT extends Exception> ResultT apply(
            PersistenceFunction<Transaction, ResultT, ExceptionT> function,
            PersistenceHint... persistenceHints) throws IOException {
        CatchingTransactionWork<ResultT, ExceptionT> wrapper = wrap(function);
        ResultT result;
        try (Session session = driver.session()) {
            if (readOnly(persistenceHints)) {
                result = session.readTransaction(wrapper);
            } else {
                result = session.writeTransaction(wrapper);
            }
        } catch (Exception e) {
            throw new IOException(e);
        }

        if (wrapper.getException() != null) {
            throw wrapper.getException();
        }

        return result;
    }

    @Override
    public <ExceptionT extends Exception> void execute(
            PersistenceAction<Transaction, ExceptionT> action, PersistenceHint... persistenceHints)
            throws IOException, ExceptionT {
        apply((PersistenceFunction<Transaction, Void, ExceptionT>) session -> {
            action.execute(session);
            return null;
        }, persistenceHints);
    }

    private static class CatchingTransactionWork<T, ExceptionT extends Exception> implements
            TransactionWork<T> {
        private PersistenceFunction<Transaction, T, ExceptionT> operation;
        private IOException exception;

        private CatchingTransactionWork(
                PersistenceFunction<Transaction, T, ExceptionT> operation) {
            this.operation = operation;
        }

        private IOException getException() {
            return exception;
        }

        @Override
        public T execute(Transaction transaction) {
            T result = null;
            try {
                result = operation.execute(transaction);
                transaction.success();
            } catch (Exception e) {
                transaction.failure();
                exception = new IOException(e);
            } finally {
                transaction.close();
            }

            return result;
        }
    }
}
