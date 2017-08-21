package xyz.greatapp.libs.database.automation_database_driver;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executor;

public class SavepointProxyConnectionImpl implements SavepointProxyConnection
{
    private final Connection wrappedConnection;
    private final SavepointPgProxyDriver driver;
    private boolean isProxyConnectionActive = false;
    private String connectionUrl;
    private Savepoint _lastSavepoint;

    SavepointProxyConnectionImpl(Connection wrappedConnection, SavepointPgProxyDriver savepointPgProxyDriver)
    {
        this.wrappedConnection = wrappedConnection;
        driver = savepointPgProxyDriver;
    }

    @Override
    public Statement createStatement() throws SQLException
    {
        return wrappedConnection.createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException
    {
        return wrappedConnection.prepareStatement(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException
    {
        return wrappedConnection.prepareCall(sql);
    }

    @Override
    public String nativeSQL(String sql) throws SQLException
    {
        return wrappedConnection.nativeSQL(sql);
    }

    @Override
    public synchronized boolean getAutoCommit() throws SQLException
    {
        return !isProxyConnectionActive() && wrappedConnection.getAutoCommit();
    }

    @Override
    public synchronized void setAutoCommit(boolean autoCommit) throws SQLException
    {
        if (isProxyConnectionActive())
        {
            if (!autoCommit)
            {
                releaseLastSavepoint();
                setNewSavepoint();
            }
        }
        else
        {
            if (!isClosed())
            {
                if (getAutoCommit() != autoCommit)
                {
                    wrappedConnection.setAutoCommit(autoCommit);
                }
            }
        }
    }

    @Override
    public synchronized void commit() throws SQLException
    {
        if (isProxyConnectionActive())
        {
            releaseLastSavepoint();
            setNewSavepoint();
        }
        else
        {
            wrappedConnection.commit();
        }
    }

    @Override
    public synchronized void rollback() throws SQLException
    {
        if (isProxyConnectionActive())
        {
            rollbackLastSavepoint();
            setNewSavepoint();
        }
        else
        {
            if (!isClosed())
            {
                if (!getAutoCommit())
                {
                    wrappedConnection.rollback();
                }
            }
        }
    }

    @Override
    public synchronized void close() throws SQLException
    {
        if (!isClosed() && !isProxyConnectionActive())
        {
            wrappedConnection.close();
        }
    }

    @Override
    public synchronized boolean isClosed() throws SQLException
    {
        return wrappedConnection.isClosed();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException
    {
        return wrappedConnection.getMetaData();
    }

    @Override
    public boolean isReadOnly() throws SQLException
    {
        return wrappedConnection.isReadOnly();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException
    {
        wrappedConnection.setReadOnly(readOnly);
    }

    @Override
    public String getCatalog() throws SQLException
    {
        return wrappedConnection.getCatalog();
    }

    @Override
    public void setCatalog(String catalog) throws SQLException
    {
        wrappedConnection.setCatalog(catalog);
    }

    @Override
    public int getTransactionIsolation() throws SQLException
    {
        return wrappedConnection.getTransactionIsolation();
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException
    {
        wrappedConnection.setTransactionIsolation(level);
    }

    @Override
    public SQLWarning getWarnings() throws SQLException
    {
        return wrappedConnection.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException
    {
        wrappedConnection.clearWarnings();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException
    {
        return wrappedConnection.createStatement(resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
    {
        return wrappedConnection.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
    {
        return wrappedConnection.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public Map<String, Class< ? >> getTypeMap() throws SQLException
    {
        return wrappedConnection.getTypeMap();
    }

    @Override
    public void setTypeMap(Map<String, Class< ? >> map) throws SQLException
    {
        wrappedConnection.setTypeMap(map);
    }

    @Override
    public int getHoldability() throws SQLException
    {
        return wrappedConnection.getHoldability();
    }

    @Override
    public void setHoldability(int holdability) throws SQLException
    {
        wrappedConnection.setHoldability(holdability);
    }

    @Override
    public Savepoint setSavepoint() throws SQLException
    {
        return null;
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException
    {
        return wrappedConnection.setSavepoint(name);
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException
    {
        wrappedConnection.rollback(savepoint);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException
    {
        wrappedConnection.releaseSavepoint(savepoint);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
    {
        return wrappedConnection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
    {
        return wrappedConnection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
    {
        return wrappedConnection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException
    {
        return wrappedConnection.prepareStatement(sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException
    {
        return wrappedConnection.prepareStatement(sql, columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException
    {
        return wrappedConnection.prepareStatement(sql, columnNames);
    }

    @Override
    public Clob createClob() throws SQLException
    {
        return wrappedConnection.createClob();
    }

    @Override
    public Blob createBlob() throws SQLException
    {
        return wrappedConnection.createBlob();
    }

    @Override
    public NClob createNClob() throws SQLException
    {
        return wrappedConnection.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException
    {
        return wrappedConnection.createSQLXML();
    }

    @Override
    public boolean isValid(int timeout) throws SQLException
    {
        return wrappedConnection.isValid(timeout);
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException
    {
        wrappedConnection.setClientInfo(name, value);
    }

    @Override
    public String getClientInfo(String name) throws SQLException
    {
        return wrappedConnection.getClientInfo(name);
    }

    @Override
    public Properties getClientInfo() throws SQLException
    {
        return wrappedConnection.getClientInfo();
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException
    {
        wrappedConnection.setClientInfo(properties);
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException
    {
        return wrappedConnection.createArrayOf(typeName, elements);
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException
    {
        return wrappedConnection.createStruct(typeName, attributes);
    }

    @Override
    public String getSchema() throws SQLException
    {
        return wrappedConnection.getSchema();
    }

    @Override
    public void setSchema(String schema) throws SQLException
    {
        wrappedConnection.setSchema(schema);
    }

    @Override
    public void abort(Executor executor) throws SQLException
    {
        wrappedConnection.abort(executor);
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException
    {
        wrappedConnection.setNetworkTimeout(executor, milliseconds);
    }

    @Override
    public int getNetworkTimeout() throws SQLException
    {
        return wrappedConnection.getNetworkTimeout();
    }

    @Override
    public <T> T unwrap(Class<T> tClass) throws SQLException
    {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class< ? > aClass) throws SQLException
    {
        return false;
    }

    @Override
    public synchronized boolean isProxyConnectionActive()
    {
        return isProxyConnectionActive;
    }

    // Attribute should not be modified directly but through public methods beginTransactionForAutomationTest or rollbackTransactionForAutomationTest
    private synchronized void setProxyConnectionActive(boolean isProxyConnectionActive)
    {
        this.isProxyConnectionActive = isProxyConnectionActive;
        driver.setProxyConnectionActive(isProxyConnectionActive);
    }

    private Savepoint getLastSavepoint()
    {
        return _lastSavepoint;
    }

    private void setLastSavepoint(Savepoint lastSavepoint)
    {
        _lastSavepoint = lastSavepoint;
    }

    private synchronized void setNewSavepoint() throws SQLException
    {
        String savepointName = UUID.randomUUID().toString();
        Savepoint savepoint = wrappedConnection.setSavepoint(savepointName);
        setLastSavepoint(savepoint);
    }

    @Override
    public synchronized void beginTransactionForAutomationTest() throws SQLException
    {
        if (!isProxyConnectionActive())
        {
            wrappedConnection.setAutoCommit(false);
            setNewSavepoint();
            setProxyConnectionActive(true);
        }
    }

    @Override
    public synchronized void rollbackTransactionForAutomationTest() throws SQLException
    {
        if (isProxyConnectionActive())
        {
            wrappedConnection.rollback();
            wrappedConnection.setAutoCommit(true);
            setProxyConnectionActive(false);
        }
    }

    private synchronized void releaseLastSavepoint() throws SQLException
    {
        Savepoint lastSavepoint = getLastSavepoint();
        if (lastSavepoint != null)
        {
            wrappedConnection.releaseSavepoint(lastSavepoint);
            setLastSavepoint(null);
        }
    }

    private synchronized void rollbackLastSavepoint() throws SQLException
    {
        Savepoint lastSavepoint = getLastSavepoint();
        if (lastSavepoint != null)
        {
            wrappedConnection.rollback(lastSavepoint);
            setLastSavepoint(null);
        }
    }

    @Override
    public String getConnectionUrl()
    {
        return connectionUrl;
    }

    @Override
    public void setConnectionUrl(String connectionUrl)
    {
        this.connectionUrl = connectionUrl;
    }
}
