package xyz.greatapp.libs.database.adapter;

import org.json.JSONArray;
import org.json.JSONObject;
import xyz.greatapp.libs.database.automation_database_driver.SavepointProxyConnection;
import xyz.greatapp.libs.database.util.DbBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DataBaseAdapter
{
    private final transient DataSource dataSource;
    private transient Connection connection = null;
    private boolean _isInTransaction;
    private final transient PreparedStatementMapper preparedStatementMapper;

    DataBaseAdapter(DataSource dataSource, PreparedStatementMapper preparedStatementMapper)
    {
        this.dataSource = dataSource;
        this.preparedStatementMapper = preparedStatementMapper;
    }

    /**
     * Creates a new connection to a postgres database
     *
     * @return The established connection.
     */
    Connection getConnection() throws SQLException
    {
        if (currentConnection() == null)
        {
            connection = dataSource.getConnection();
        }
        return currentConnection();
    }

    /**
     * Executes an insert statement.
     *
     * @param builder Helper object that contains placeholders and build method
     * @return The generated key of the id column returned from the query execution.
     * @throws Exception A SQL or mapping Exception
     */
    public String executeInsert(DbBuilder builder) throws Exception
    {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(builder.sql()))
        {
            preparedStatementMapper.map(preparedStatement, builder.values());
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getString(1);
        }
        finally
        {
            releaseConnectionIfPossible();
        }
    }

    /**
     * Executes and update statement.
     *
     * @param builder Helper object that contains placeholders and build method
     * @return The number of rows returned from the query.
     * @throws Exception A SQL or mapping Exception
     */
    public int executeUpdate(DbBuilder builder) throws Exception
    {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(builder.sql()))
        {
            preparedStatementMapper.map(preparedStatement, builder.values());
            return preparedStatement.executeUpdate();
        }
        finally
        {
            releaseConnectionIfPossible();
        }
    }

    /**
     * Executes a Select statement in the database and returns multiple rows.
     *
     * @param builder Helper object that contains placeholders and build method
     * @return The list of type T built from the select statement execution
     * @throws Exception A SQL or mapping Exception
     */
    public synchronized JSONArray selectList(DbBuilder builder) throws Exception
    {
        try (PreparedStatement statement = getConnection().prepareStatement(builder.sql()))
        {
            preparedStatementMapper.map(statement, builder.values());

            try (ResultSet resultSet = statement.executeQuery())
            {
                JSONArray jsonArray = new JSONArray();
                while (resultSet.next())
                {
                    jsonArray.put(builder.build(resultSet));
                }
                return jsonArray;
            }
        }
        finally
        {
            releaseConnectionIfPossible();
        }
    }

    /**
     * Executes a Select statement in the database and returns only one object
     *
     * @param builder Helper object that contains placeholders and build method
     * @return The type T built from the select statement execution
     * @throws Exception A SQL or mapping Exception
     */
    public synchronized JSONObject selectObject(DbBuilder builder) throws Exception
    {
        try (PreparedStatement statement = getConnection().prepareStatement(builder.sql()))
        {
            preparedStatementMapper.map(statement, builder.values());

            try (ResultSet resultSet = statement.executeQuery())
            {
                return resultSet.next()
                        ? builder.build(resultSet)
                        : newJSONObject();
            }
        }
        finally
        {
            releaseConnectionIfPossible();
        }
    }

    JSONObject newJSONObject()
    {
        return new JSONObject();
    }

    synchronized void beginTransaction() throws Exception
    {
        getConnection().setAutoCommit(false);
        _isInTransaction = true;
    }

    synchronized void rollbackTransaction() throws Exception
    {
        if (existsConnection())
        {
            try
            {
                getConnection().rollback();
                getConnection().setAutoCommit(true);
                _isInTransaction = false;
            }
            finally
            {
                releaseConnectionIfPossible();
            }
        }
    }

    boolean isInTransaction()
    {
        return _isInTransaction;
    }

    private synchronized void releaseConnectionIfPossible() throws SQLException
    {
        if (!isInTransaction() && existsConnection())
        {
            releaseConnection();
        }
    }

    private void releaseConnection() throws SQLException
    {
        currentConnection().close();
        connection = null;
    }

    @Override
    protected void finalize() throws Throwable
    {
        if (existsConnection())
        {
            releaseConnection();
        }
        super.finalize();
    }

    private boolean existsConnection()
    {
        return currentConnection() != null;
    }

    Connection currentConnection()
    {
        return connection;
    }

    public synchronized void beginTransactionForFunctionalTest() throws SQLException
    {
        SavepointProxyConnection connection = getConnectionForFunctionalTests();
        connection.beginTransactionForAutomationTest();
        this.connection = connection;
    }

    public synchronized void rollbackTransactionForFunctionalTest() throws SQLException
    {
        SavepointProxyConnection connection = getConnectionForFunctionalTests();
        connection.rollbackTransactionForAutomationTest();
        this.connection = null;
    }

    SavepointProxyConnection getConnectionForFunctionalTests() throws SQLException
    {
        return (SavepointProxyConnection) getConnection();
    }
}
