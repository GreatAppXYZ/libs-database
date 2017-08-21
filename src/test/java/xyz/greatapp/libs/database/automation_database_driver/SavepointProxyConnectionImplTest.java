package xyz.greatapp.libs.database.automation_database_driver;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Properties;
import java.util.concurrent.Executor;

import static java.sql.Connection.TRANSACTION_NONE;
import static java.sql.ResultSet.*;
import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.util.Collections.emptyMap;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SavepointProxyConnectionImplTest
{
    private SavepointProxyConnectionImpl savepointProxyConnection;
    @Mock
    private Connection wrappedConnection;
    @Mock
    private SavepointPgProxyDriver driver;
    @Mock
    private Savepoint savepoint;
    @Mock
    private Properties properties;
    @Mock
    private Executor executor;

    @Before
    public void setUp() throws Exception
    {
        savepointProxyConnection = new SavepointProxyConnectionImpl(wrappedConnection, driver);
        given(wrappedConnection.setSavepoint(anyString())).willReturn(savepoint);
    }

    @Test
    public void shouldCallCreateStatementOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.createStatement();

        //then
        verify(wrappedConnection).createStatement();
    }

    @Test
    public void shouldCallPreparedStatementOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.prepareStatement("SELECT * FROM dummy;");

        //then
        verify(wrappedConnection).prepareStatement("SELECT * FROM dummy;");
    }

    @Test
    public void shouldCallPrepareCallOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.prepareCall("SELECT * FROM dummy;");

        //then
        verify(wrappedConnection).prepareCall("SELECT * FROM dummy;");
    }

    @Test
    public void shouldCallNativeSQLOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.nativeSQL("SELECT * FROM dummy;");

        //then
        verify(wrappedConnection).nativeSQL("SELECT * FROM dummy;");
    }

    @Test
    public void shouldCallGetAutoCommitOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.getAutoCommit();

        //then
        verify(wrappedConnection).getAutoCommit();
    }

    @Test
    public void shouldNotCallGetAutoCommitOnWrappedConnectionWhenProxyConnectionIsActive() throws SQLException
    {
        //given
        savepointProxyConnection.beginTransactionForAutomationTest();

        //when
        savepointProxyConnection.getAutoCommit();

        //then
        verify(wrappedConnection, never()).getAutoCommit();
    }

    @Test
    public void shouldCallSetSavepointOnWrappedConnectionWhenProxyConnectionIsActiveAndAutoCommitIsFalse() throws SQLException
    {
        //given
        savepointProxyConnection.beginTransactionForAutomationTest();

        //when
        savepointProxyConnection.setAutoCommit(false);

        //then
        verify(wrappedConnection, times(2)).setSavepoint(anyString());
    }

    @Test
    public void shouldNotCallSetSavepointOnWrappedConnectionWhenProxyConnectionIsActiveAndAutoCommitIsTrue() throws SQLException
    {
        //given
        savepointProxyConnection.beginTransactionForAutomationTest();

        //when
        savepointProxyConnection.setAutoCommit(true);

        //then
        verify(wrappedConnection, times(1)).setSavepoint(anyString());
    }

    @Test
    public void shouldCallReleaseSavepointOnWrappedConnectionWhenProxyConnectionIsActiveAndAutoCommitIsFalse() throws SQLException
    {
        //given
        savepointProxyConnection.beginTransactionForAutomationTest();
        savepointProxyConnection.setAutoCommit(false);

        //when
        savepointProxyConnection.setAutoCommit(false);

        //then
        verify(wrappedConnection, times(2)).releaseSavepoint(any(Savepoint.class));
    }

    @Test
    public void shouldCallSetAutoCommitOnWrappedConnectionWhenConnectionIsNotActiveAndAutoCommitIsTrue() throws SQLException
    {
        //when
        savepointProxyConnection.setAutoCommit(true);

        //then
        verify(wrappedConnection).setAutoCommit(true);
    }

    @Test
    public void shouldCallSetAutoCommitOnWrappedConnectionWhenConnectionIsActiveAndAutoCommitIsTrueInWrappedConnection() throws SQLException
    {
        //given
        given(wrappedConnection.getAutoCommit()).willReturn(true);

        //when
        savepointProxyConnection.setAutoCommit(false);

        //then
        verify(wrappedConnection).setAutoCommit(false);
    }

    @Test
    public void shouldCallCommitOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.commit();

        //then
        verify(wrappedConnection).commit();
    }

    @Test
    public void shouldNotCallCommitOnWrappedConnectionWhenProxyConnectionIsAlive() throws SQLException
    {
        //given
        savepointProxyConnection.beginTransactionForAutomationTest();

        //when
        savepointProxyConnection.commit();

        //then
        verify(wrappedConnection, never()).commit();
    }

    @Test
    public void shouldCallReleaseSavepointOnWrappedConnectionWhenProxyConnectionIsActiveAndCommitIsCalled() throws SQLException
    {
        //given
        savepointProxyConnection.beginTransactionForAutomationTest();

        //when
        savepointProxyConnection.commit();

        //then
        verify(wrappedConnection, times(1)).releaseSavepoint(any(Savepoint.class));
    }

    @Test
    public void shouldCallSetSavepointOnWrappedConnectionWhenProxyConnectionIsActiveAndCommitIsCalled() throws SQLException
    {
        //given
        savepointProxyConnection.beginTransactionForAutomationTest();

        //when
        savepointProxyConnection.commit();

        //then
        verify(wrappedConnection, times(2)).setSavepoint(anyString());
    }

    @Test
    public void shouldCallRollbackOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.rollback();

        //then
        verify(wrappedConnection).rollback();
    }

    @Test
    public void shouldNotCallRollbackOnWrappedConnectionWhenAutoCommitIsTrue() throws SQLException
    {
        //given
        given(wrappedConnection.getAutoCommit()).willReturn(true);

        //when
        savepointProxyConnection.rollback();

        //then
        verify(wrappedConnection, never()).rollback();
    }

    @Test
    public void shouldNotCallRollbackOnWrappedConnectionWhenAutoCommitIsClosed() throws SQLException
    {
        //given
        given(wrappedConnection.isClosed()).willReturn(true);

        //when
        savepointProxyConnection.rollback();

        //then
        verify(wrappedConnection, never()).rollback();
    }

    @Test
    public void shouldNotCallRollbackOnWrappedConnectionWhenProxyConnectionIsAlive() throws SQLException
    {
        //given
        savepointProxyConnection.beginTransactionForAutomationTest();

        //when
        savepointProxyConnection.rollback();

        //then
        verify(wrappedConnection, never()).rollback();
    }

    @Test
    public void shouldCallRollbackLastSavePointOnWrappedConnectionWhenProxyConnectionIsActiveAndRollbackIsCalled() throws SQLException
    {
        //given
        savepointProxyConnection.beginTransactionForAutomationTest();

        //when
        savepointProxyConnection.rollback();

        //then
        verify(wrappedConnection, times(1)).rollback(any(Savepoint.class));
    }

    @Test
    public void shouldCallSetSavepointOnWrappedConnectionWhenProxyConnectionIsActiveAndRollbackIsCalled() throws SQLException
    {
        //given
        savepointProxyConnection.beginTransactionForAutomationTest();

        //when
        savepointProxyConnection.rollback();

        //then
        verify(wrappedConnection, times(2)).setSavepoint(anyString());
    }

    @Test
    public void shouldCallCloseOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.close();

        //then
        verify(wrappedConnection).close();
    }

    @Test
    public void shouldNotCallCloseOnWrappedConnectionWhenItIsClosed() throws SQLException
    {
        //given
        given(wrappedConnection.isClosed()).willReturn(true);

        //when
        savepointProxyConnection.close();

        //then
        verify(wrappedConnection, never()).close();
    }

    @Test
    public void shouldNotCallCloseOnWrappedConnectionWhenItProxyConnectionIsAlive() throws SQLException
    {
        //given
        savepointProxyConnection.beginTransactionForAutomationTest();

        //when
        savepointProxyConnection.close();

        //then
        verify(wrappedConnection, never()).close();
    }

    @Test
    public void shouldCallIsClosedOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.isClosed();

        //then
        verify(wrappedConnection).isClosed();
    }

    @Test
    public void shouldCallGetMetadataOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.getMetaData();

        //then
        verify(wrappedConnection).getMetaData();
    }

    @Test
    public void shouldCallIsReadOnlyOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.isReadOnly();

        //then
        verify(wrappedConnection).isReadOnly();
    }

    @Test
    public void shouldCallSetReadonlyFalseOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.setReadOnly(false);

        //then
        verify(wrappedConnection).setReadOnly(false);
    }

    @Test
    public void shouldCallSetReadonlyTrueOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.setReadOnly(true);

        //then
        verify(wrappedConnection).setReadOnly(true);
    }

    @Test
    public void shouldCallGetCatalogOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.getCatalog();

        //then
        verify(wrappedConnection).getCatalog();
    }

    @Test
    public void shouldCallSetCatalogOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.setCatalog("");

        //then
        verify(wrappedConnection).setCatalog("");
    }

    @Test
    public void shouldCallGetTransactionIsolationOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.getTransactionIsolation();

        //then
        verify(wrappedConnection).getTransactionIsolation();
    }

    @Test
    public void shouldCallSetTransactionIsolationOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.setTransactionIsolation(TRANSACTION_NONE);

        //then
        verify(wrappedConnection).setTransactionIsolation(TRANSACTION_NONE);
    }

    @Test
    public void shouldCallGetWarningsOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.getWarnings();

        //then
        verify(wrappedConnection).getWarnings();
    }

    @Test
    public void shouldCallClearWarningsOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.clearWarnings();

        //then
        verify(wrappedConnection).clearWarnings();
    }

    @Test
    public void shouldCallCreateStatementWithParamsOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.createStatement(TYPE_FORWARD_ONLY, CONCUR_READ_ONLY);

        //then
        verify(wrappedConnection).createStatement(TYPE_FORWARD_ONLY, CONCUR_READ_ONLY);
    }

    @Test
    public void shouldCallPrepareStatementWithParamsOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.prepareStatement("SELECT * FROM dummy", TYPE_FORWARD_ONLY, CONCUR_READ_ONLY);

        //then
        verify(wrappedConnection).prepareStatement("SELECT * FROM dummy", TYPE_FORWARD_ONLY, CONCUR_READ_ONLY);
    }

    @Test
    public void shouldCallPrepareCallWithParamsOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.prepareCall("SELECT * FROM dummy", TYPE_FORWARD_ONLY, CONCUR_READ_ONLY);

        //then
        verify(wrappedConnection).prepareCall("SELECT * FROM dummy", TYPE_FORWARD_ONLY, CONCUR_READ_ONLY);
    }

    @Test
    public void shouldCallGetTypeMapOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.getTypeMap();

        //then
        verify(wrappedConnection).getTypeMap();
    }

    @Test
    public void shouldCallSetTypeMapOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.setTypeMap(emptyMap());

        //then
        verify(wrappedConnection).setTypeMap(emptyMap());
    }

    @Test
    public void shouldCallGetHoldabilityOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.getHoldability();

        //then
        verify(wrappedConnection).getHoldability();
    }

    @Test
    public void shouldCallSetHoldabilityOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.setHoldability(1);

        //then
        verify(wrappedConnection).setHoldability(1);
    }

    @Test
    public void shouldReturnNullWhenCallSetSavePoint() throws SQLException
    {
        //when
        Savepoint savepoint = savepointProxyConnection.setSavepoint();

        //then
        assertNull(savepoint);
    }

    @Test
    public void shouldNotCallSetSavePointOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.setSavepoint();

        //then
        verify(wrappedConnection, never()).setSavepoint();
    }

    @Test
    public void shouldCallSetSavePointWithParametersOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.setSavepoint("");

        //then
        verify(wrappedConnection).setSavepoint("");
    }

    @Test
    public void shouldCallRollbackWithParametersOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.rollback(savepoint);

        //then
        verify(wrappedConnection).rollback(savepoint);
    }

    @Test
    public void shouldCallReleaseSavepointWithParametersOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.releaseSavepoint(savepoint);

        //then
        verify(wrappedConnection).releaseSavepoint(savepoint);
    }

    @Test
    public void shouldCallCreateStatementWithParametersOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.createStatement(TYPE_FORWARD_ONLY, CONCUR_READ_ONLY, HOLD_CURSORS_OVER_COMMIT);

        //then
        verify(wrappedConnection).createStatement(TYPE_FORWARD_ONLY, CONCUR_READ_ONLY, HOLD_CURSORS_OVER_COMMIT);
    }

    @Test
    public void shouldCallPrepareStatementWithParametersOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.prepareStatement("SELECT * FROM dummy;", TYPE_FORWARD_ONLY, CONCUR_READ_ONLY);

        //then
        verify(wrappedConnection).prepareStatement("SELECT * FROM dummy;", TYPE_FORWARD_ONLY, CONCUR_READ_ONLY);
    }

    @Test
    public void shouldCallPrepareCallWithParametersOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.prepareCall("SELECT * FROM dummy;", TYPE_FORWARD_ONLY, CONCUR_READ_ONLY);

        //then
        verify(wrappedConnection).prepareCall("SELECT * FROM dummy;", TYPE_FORWARD_ONLY, CONCUR_READ_ONLY);
    }

    @Test
    public void shouldCallPrepareStatementWithTwoParametersOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.prepareStatement("SELECT * FROM dummy;", RETURN_GENERATED_KEYS);

        //then
        verify(wrappedConnection).prepareStatement("SELECT * FROM dummy;", RETURN_GENERATED_KEYS);
    }

    @Test
    public void shouldCallPrepareStatementWithIntArrayOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.prepareStatement("SELECT * FROM dummy;", new int[]{1});

        //then
        verify(wrappedConnection).prepareStatement("SELECT * FROM dummy;", new int[]{1});
    }

    @Test
    public void shouldCallPrepareStatementWithStringArrayOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.prepareStatement("SELECT * FROM dummy;", new String[]{""});

        //then
        verify(wrappedConnection).prepareStatement("SELECT * FROM dummy;", new String[]{""});
    }

    @Test
    public void shouldCallCreateClobOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.createClob();

        //then
        verify(wrappedConnection).createClob();
    }

    @Test
    public void shouldCallCreateBlobOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.createBlob();

        //then
        verify(wrappedConnection).createBlob();
    }

    @Test
    public void shouldCallCreateNClobOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.createNClob();

        //then
        verify(wrappedConnection).createNClob();
    }

    @Test
    public void shouldCallCreateSQLXMLOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.createSQLXML();

        //then
        verify(wrappedConnection).createSQLXML();
    }

    @Test
    public void shouldCallIsValidOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.isValid(100);

        //then
        verify(wrappedConnection).isValid(100);
    }

    @Test
    public void shouldCallSetClientInfoOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.setClientInfo("key", "value");

        //then
        verify(wrappedConnection).setClientInfo("key", "value");
    }

    @Test
    public void shouldCallGetClientInfoOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.getClientInfo();

        //then
        verify(wrappedConnection).getClientInfo();
    }

    @Test
    public void shouldCallGetClientInfoWithParametersOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.getClientInfo("key");

        //then
        verify(wrappedConnection).getClientInfo("key");
    }

    @Test
    public void shouldCallSetClientInfoWithPropertiesOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.setClientInfo(properties);

        //then
        verify(wrappedConnection).setClientInfo(properties);
    }

    @Test
    public void shouldCallCreateArrayOfOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.createArrayOf("name", new Object[1]);

        //then
        verify(wrappedConnection).createArrayOf("name", new Object[1]);
    }

    @Test
    public void shouldCallCreateStructOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.createStruct("name", new Object[1]);

        //then
        verify(wrappedConnection).createStruct("name", new Object[1]);
    }

    @Test
    public void shouldCallGetSchemaOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.getSchema();

        //then
        verify(wrappedConnection).getSchema();
    }

    @Test
    public void shouldCallSetSchemaOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.setSchema("");

        //then
        verify(wrappedConnection).setSchema("");
    }

    @Test
    public void shouldCallAbortOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.abort(executor);

        //then
        verify(wrappedConnection).abort(executor);
    }

    @Test
    public void shouldCallSetNetworkTimeoutOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.setNetworkTimeout(executor, 100);

        //then
        verify(wrappedConnection).setNetworkTimeout(executor, 100);
    }

    @Test
    public void shouldCallGetNetworkTimeoutOnWrappedConnection() throws SQLException
    {
        //when
        savepointProxyConnection.getNetworkTimeout();

        //then
        verify(wrappedConnection).getNetworkTimeout();
    }

    @Test
    public void unwrapShouldReturnNull() throws SQLException
    {
        //when
        SavepointProxyConnection value = savepointProxyConnection.unwrap(SavepointProxyConnection.class);

        //then
        assertNull(value);
    }

    @Test
    public void isWrappedForShouldReturnNull() throws SQLException
    {
        //when
        boolean value = savepointProxyConnection.isWrapperFor(SavepointProxyConnection.class);

        //then
        assertFalse(value);
    }

    @Test
    public void shouldReturnFalseIfTransactionIsNotOpen()
    {
        //when
        boolean proxyConnectionActive = savepointProxyConnection.isProxyConnectionActive();

        //then
        assertFalse(proxyConnectionActive);
    }

    @Test
    public void shouldReturnTrueIfTransactionIsOpen() throws SQLException
    {
        //given
        savepointProxyConnection.beginTransactionForAutomationTest();

        //when
        boolean proxyConnectionActive = savepointProxyConnection.isProxyConnectionActive();

        //then
        assertTrue(proxyConnectionActive);
    }

    @Test
    public void shouldReturnFalseIfTransactionIsRollbacked() throws SQLException
    {
        //given
        savepointProxyConnection.beginTransactionForAutomationTest();
        savepointProxyConnection.rollbackTransactionForAutomationTest();

        //when
        boolean proxyConnectionActive = savepointProxyConnection.isProxyConnectionActive();

        //then
        assertFalse(proxyConnectionActive);
    }

    @Test
    public void shouldSetAutoCommitToFalseOnWrappedConnectionWhenBeginTransaction() throws SQLException
    {
        //when
        savepointProxyConnection.beginTransactionForAutomationTest();

        //then
        verify(wrappedConnection).setAutoCommit(false);
    }

    @Test
    public void shouldNotSetAutoCommitToFalseTwiceOnWrappedConnectionWhenBeginTransaction() throws SQLException
    {
        //when
        savepointProxyConnection.beginTransactionForAutomationTest();
        savepointProxyConnection.beginTransactionForAutomationTest();

        //then
        verify(wrappedConnection, times(1)).setAutoCommit(false);
    }

    @Test
    public void shouldSetSavepointOnWrappedConnectionWhenBeginTransaction() throws SQLException
    {
        //when
        savepointProxyConnection.beginTransactionForAutomationTest();

        //then
        verify(wrappedConnection).setSavepoint(anyString());
    }

    @Test
    public void shouldNotSetSavePointTwiceOnWrappedConnectionWhenBeginTransaction() throws SQLException
    {
        //when
        savepointProxyConnection.beginTransactionForAutomationTest();
        savepointProxyConnection.beginTransactionForAutomationTest();

        //then
        verify(wrappedConnection, times(1)).setSavepoint(anyString());
    }

    @Test
    public void shouldSetProxyConnectionActiveOnDriverWhenBeginTransaction() throws SQLException
    {
        //when
        savepointProxyConnection.beginTransactionForAutomationTest();

        //then
        verify(driver).setProxyConnectionActive(true);
    }

    @Test
    public void shouldNotSetProxyConnectionTwiceActiveOnDriverWhenBeginTransaction() throws SQLException
    {
        //when
        savepointProxyConnection.beginTransactionForAutomationTest();
        savepointProxyConnection.beginTransactionForAutomationTest();

        //then
        verify(driver, times(1)).setProxyConnectionActive(true);
    }

    @Test
    public void shouldNotRollbackOnWrappedConnectionWhenRollingBackTransactionOnNotActiveProxyConnection() throws SQLException
    {
        //when
        savepointProxyConnection.rollbackTransactionForAutomationTest();

        //then
        verify(wrappedConnection, never()).rollback();
    }

    @Test
    public void shouldRollbackOnWrappedConnectionWhenRollingBackTransaction() throws SQLException
    {
        //given
        savepointProxyConnection.beginTransactionForAutomationTest();

        //when
        savepointProxyConnection.rollbackTransactionForAutomationTest();

        //then
        verify(wrappedConnection).rollback();
    }

    @Test
    public void shouldNotSetAutoCommitOnWrappedConnectionWhenRollingBackTransactionOnNotActiveProxyConnection() throws SQLException
    {
        //when
        savepointProxyConnection.rollbackTransactionForAutomationTest();

        //then
        verify(wrappedConnection, never()).setAutoCommit(true);
    }

    @Test
    public void shouldSetAutoCommitOnWrappedConnectionWhenRollingBackTransaction() throws SQLException
    {
        //given
        savepointProxyConnection.beginTransactionForAutomationTest();

        //when
        savepointProxyConnection.rollbackTransactionForAutomationTest();

        //then
        verify(wrappedConnection).setAutoCommit(true);
    }

    @Test
    public void shouldNotSetProxyConnectionOnDriverWhenRollingBackTransactionOnNotActiveProxyConnection() throws SQLException
    {
        //when
        savepointProxyConnection.rollbackTransactionForAutomationTest();

        //then
        verify(driver, never()).setProxyConnectionActive(false);
    }

    @Test
    public void shouldSetProxyConnectionOnDriverWhenRollingBackTransaction() throws SQLException
    {
        //given
        savepointProxyConnection.beginTransactionForAutomationTest();

        //when
        savepointProxyConnection.rollbackTransactionForAutomationTest();

        //then
        verify(driver).setProxyConnectionActive(false);
    }
}
