package io.aboutcode.stage.persistence.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.aboutcode.stage.component.BaseComponent;
import io.aboutcode.stage.persistence.Persistence;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * An implementation of {@link Persistence} that allows direct JDBC access (as opposed to access via
 * a ORM framework, for example). The implementation uses a smart connection pooling methodology.
 */
public class JDBCPersistence extends BaseComponent implements Persistence<Connection> {

   private HikariDataSource connectionPool;
   private JDBCDatabaseConfiguration JDBCDatabaseConfiguration;

   /**
    * Creates a new instance with the specified database configuration
    *
    * @param JDBCDatabaseConfiguration The configuration to create this instance with
    */
   public JDBCPersistence(JDBCDatabaseConfiguration JDBCDatabaseConfiguration) {
      this.JDBCDatabaseConfiguration = JDBCDatabaseConfiguration;
   }

   @Override
   protected void init() {
      connectionPool = new HikariDataSource(JDBCDatabaseConfiguration.apply(new HikariConfig()));
      connectionPool.setInitializationFailFast(true);
   }

   @Override
   public <ResultT, ExceptionT extends Exception> ResultT apply(
       PersistenceFunction<Connection, ResultT, ExceptionT> function,
       PersistenceHint... persistenceHints) throws IOException, ExceptionT {
      ResultT result;
      try (Connection connection = connectionPool.getConnection()) {
         result = function.execute(connection);
      } catch (SQLException e) {
         throw new IOException(e);
      }

      return result;
   }

   @Override
   public <ExceptionT extends Exception> void execute(
       PersistenceAction<Connection, ExceptionT> action, PersistenceHint... persistenceHints)
       throws IOException, ExceptionT {
      try (Connection connection = connectionPool.getConnection()) {
         action.execute(connection);
      } catch (SQLException e) {
         throw new IOException(e);
      }
   }
}
