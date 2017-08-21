package xyz.greatapp.libs.database.adapter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import xyz.greatapp.libs.database.adapter.DataBaseAdapter;
import xyz.greatapp.libs.database.automation_database_driver.SavepointProxyConnection;
import xyz.greatapp.libs.database.util.DbBuilder;
import xyz.greatapp.libs.service.requests.database.ColumnValue;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DataBaseAdapterTest {
    private static final String ANY_SQL = "SELECT * FROM dummy;";
    private static final ColumnValue[] VALUES = new ColumnValue[]{};

    private DataBaseAdapter databaseAdapter;

    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private ResultSet resultSet;
    @Mock
    private DbBuilder builder;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private JSONObject jsonObject;
    @Mock
    private JSONObject newJSONObject;
    @Mock
    private PreparedStatementMapper preparedStatementMapper;
    @Mock
    private SavepointProxyConnection savepointProxyConnection;
    @Mock
    private DbBuilder dbBuilder;

    @Before
    public void setUp() throws Exception {
        databaseAdapter = new TestableDataBaseAdapter();
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(any())).willReturn(preparedStatement);
        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(builder.sql()).willReturn(ANY_SQL);
        given(builder.values()).willReturn(VALUES);
        given(connection.prepareStatement(ANY_SQL)).willReturn(preparedStatement);
        given(builder.build(resultSet)).willReturn(jsonObject);
    }

    @Test
    public void shouldGetConnectionFromDataSource() throws SQLException {
        //when
        databaseAdapter.getConnection();

        //then
        verify(dataSource).getConnection();
    }

    @Test
    public void shouldGetConnectionTwiceFromDataSource() throws SQLException {
        //given
        databaseAdapter.getConnection();

        //when
        databaseAdapter.getConnection();

        //then
        verify(dataSource, times(1)).getConnection();
    }

    @Test
    public void executeInsertShouldCreateStatementOnConnection() throws Exception {
        //when
        databaseAdapter.executeInsert(dbBuilder);

        //then
        verify(connection).prepareStatement(null);
    }

    @Test
    public void executeInsertShouldExecuteOnStatement() throws Exception {
        //when
        databaseAdapter.executeInsert(dbBuilder);

        //then
        verify(preparedStatement).executeQuery();
    }

    @Test
    public void executeInsertShouldGetKeyOnResultSet() throws Exception {
        //when
        databaseAdapter.executeInsert(dbBuilder);

        //then
        verify(resultSet).next();
        verify(resultSet).getString(1);
    }

    @Test
    public void executeInsertShouldCloseConnection() throws Exception {
        //when
        databaseAdapter.executeInsert(dbBuilder);

        //then
        verify(connection).close();
    }

    @Test
    public void executeUpdateShouldCreateStatementOnConnection() throws Exception {
        //when
        databaseAdapter.executeUpdate(dbBuilder);

        //then
        verify(connection).prepareStatement(null);
    }

    @Test
    public void executeUpdateShouldExecuteUpdateOnStatement() throws Exception {
        //when
        databaseAdapter.executeUpdate(dbBuilder);

        //then
        verify(preparedStatement).executeUpdate();
    }

    @Test
    public void executeUpdateShouldCloseConnection() throws Exception {
        //when
        databaseAdapter.executeUpdate(dbBuilder);

        //then
        verify(connection).close();
    }

    @Test
    public void selectListShouldCallPrepareStatementOnConnection() throws Exception {
        //given
        given(builder.sql()).willReturn("SELECT * FROM dummy;");

        //when
        databaseAdapter.selectList(builder);

        //then
        verify(connection).prepareStatement("SELECT * FROM dummy;");
        verify(preparedStatementMapper).map(preparedStatement, VALUES);
    }

    @Test
    public void selectListShouldExecuteQueryOnPreparedStatement() throws Exception {
        //given
        given(builder.sql()).willReturn("SELECT * FROM dummy;");

        //when
        databaseAdapter.selectList(builder);

        //then
        verify(connection).prepareStatement("SELECT * FROM dummy;");
        verify(preparedStatementMapper).map(preparedStatement, VALUES);
    }

    @Test
    public void selectList_whenResultSetHasNext_shouldPutBuiltResultInANewJSONArray() throws Exception {
        given(resultSet.next()).willReturn(true, true, false);

        JSONArray resultJSONArray = databaseAdapter.selectList(builder);

        assertThat(resultJSONArray.length(), is(2));
        assertThat(resultJSONArray.get(0), is(jsonObject));
        assertThat(resultJSONArray.get(1), is(jsonObject));
        verify(preparedStatementMapper).map(preparedStatement, VALUES);
    }

    @Test
    public void selectObject_whenExistsRecords_returnsBuiltResultSet() throws Exception {
        given(resultSet.next()).willReturn(true);

        JSONObject result = databaseAdapter.selectObject(builder);

        assertThat(result, is(jsonObject));
        verify(preparedStatementMapper).map(preparedStatement, VALUES);
    }

    @Test
    public void selectObject_whenDoNotExistRecords_returnANewJsonObject() throws Exception {
        given(resultSet.next()).willReturn(false);

        JSONObject result = databaseAdapter.selectObject(builder);

        assertThat(result, equalTo(newJSONObject));
        verify(preparedStatementMapper).map(preparedStatement, VALUES);
    }

    @Test
    public void beginTransaction_disableAutoCommitAndMarkAsInTransactionState() throws Exception {
        databaseAdapter.beginTransaction();

        verify(connection).setAutoCommit(false);
        assertThat(databaseAdapter.isInTransaction(), is(true));
    }

    @Test
    public void rollbackTransaction_whenExistsConnection_preparesConnectionForRollbackAndReleasesConnection() throws Exception {
        // used to create the connection
        databaseAdapter.getConnection();

        databaseAdapter.rollbackTransaction();

        verify(connection).rollback();
        verify(connection).setAutoCommit(true);
        assertThat(databaseAdapter.isInTransaction(), is(false));
        verify(connection).close();
    }

    @Test
    public void rollbackTransaction_whenDoesNotExistConnection_doesNothing() throws Exception {
        databaseAdapter.rollbackTransaction();

        verifyZeroInteractions(connection);
    }

    @Test
    public void beginTransactionForFunctionalTest_usesSavepointProxyConnectionAsConnection() throws Exception {
        databaseAdapter.beginTransactionForFunctionalTest();

        verify(savepointProxyConnection).beginTransactionForAutomationTest();

        assertThat(databaseAdapter.currentConnection(), is(savepointProxyConnection));
    }

    @Test
    public void rollbackTransactionForFunctionalTest_usesSavepointProxyConnectionAsConnectionAndSetNullCurrentConnection() throws Exception {
        databaseAdapter.rollbackTransactionForFunctionalTest();

        verify(savepointProxyConnection).rollbackTransactionForAutomationTest();

        assertNull(databaseAdapter.currentConnection());
    }

    private class TestableDataBaseAdapter extends DataBaseAdapter {
        TestableDataBaseAdapter() {
            super(dataSource, preparedStatementMapper);
        }

        @Override
        JSONObject newJSONObject() {
            return newJSONObject;
        }

        @Override
        SavepointProxyConnection getConnectionForFunctionalTests() throws SQLException {
            return savepointProxyConnection;
        }
    }
}
