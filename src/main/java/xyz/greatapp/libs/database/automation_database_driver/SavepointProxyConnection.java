package xyz.greatapp.libs.database.automation_database_driver;

import java.sql.Connection;
import java.sql.SQLException;

public interface SavepointProxyConnection extends Connection
{
    boolean isClosed() throws SQLException;

    boolean isProxyConnectionActive();

    boolean getAutoCommit() throws SQLException;

    void close() throws SQLException;

    void beginTransactionForAutomationTest() throws SQLException;

    void rollbackTransactionForAutomationTest() throws SQLException;

    String getConnectionUrl();

    void setConnectionUrl(String urlForWrappedDriver);
}
