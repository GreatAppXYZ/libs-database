package xyz.greatapp.libs.database.automation_database_driver;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SavepointPgProxyDriverTest
{
    private SavepointPgProxyDriver savepointPgProxyDriver;
    @Mock
    private Driver postgresDriver;
    @Mock
    private Connection connection;
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setUp() throws Exception
    {
        savepointPgProxyDriver = SavepointPgProxyDriver.registerDriver();
        savepointPgProxyDriver.setWrappedDriver(postgresDriver);
        given(postgresDriver.connect(any(), any())).willReturn(connection);
    }

    @Test
    public void shouldNotAcceptNullURL()
    {
        //when
        boolean result = savepointPgProxyDriver.acceptsURL(null);

        //then
        assertFalse(result);
    }

    @Test
    public void shouldNotAcceptEmptyURL()
    {
        //when
        boolean result = savepointPgProxyDriver.acceptsURL("");

        //then
        assertFalse(result);
    }

    @Test
    public void shouldNotAcceptNotValidURL()
    {
        //when
        boolean result = savepointPgProxyDriver.acceptsURL("jdbc:postgresql://localhost:5432/lealpoint");

        //then
        assertFalse(result);
    }

    @Test
    public void shouldAcceptValidURL()
    {
        //when
        SavepointPgProxyDriver driver = new SavepointPgProxyDriver();

        //then
        boolean result = driver.acceptsURL("jdbc:savepointpgproxy://localhost:5432/lealpoint");

        assertTrue(result);
    }

    @Test
    public void shouldReturnNonNullConnection() throws SQLException
    {
        //when
        Connection connect = savepointPgProxyDriver.connect("jdbc:savepointpgproxy://localhost:5432/lealpoint", new Properties());

        //then
        assertNotNull(connect);
    }

    @Test
    public void shouldReturnSavepointProxyConnection() throws SQLException
    {
        //when
        Connection connection = savepointPgProxyDriver.connect("jdbc:savepointpgproxy://localhost:5432/lealpoint", new Properties());

        //then
        assertTrue(connection instanceof SavepointProxyConnection);
    }

    @Test
    public void shouldReturnOriginalConnectionURL() throws SQLException
    {
        //when
        SavepointProxyConnection connection = (SavepointProxyConnection)
                savepointPgProxyDriver.connect("jdbc:savepointpgproxy://localhost:5432/lealpoint", new Properties());

        //then
        assertEquals("jdbc:postgresql://localhost:5432/lealpoint", connection.getConnectionUrl());
    }

    @Test
    public void shouldThrowExceptionWhenUsingOriginalURL() throws SQLException
    {
        //then
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Could not connect to wrapped driver (PostgreSQL JDBC Driver). "
                + "url = jdbc:postgresql://localhost:5432/lealpoint");

        //when
        savepointPgProxyDriver.connect("jdbc:postgresql://localhost:5432/lealpoint", new Properties());
    }

    @Test
    public void shouldReturnSameConnectionForSameURLWhenInTransaction() throws SQLException
    {
        //given
        SavepointProxyConnection connection1 = (SavepointProxyConnection)
                savepointPgProxyDriver.connect("jdbc:savepointpgproxy://localhost:5432/lealpoint", new Properties());
        connection1.beginTransactionForAutomationTest();

        //when
        SavepointProxyConnection connection2 = (SavepointProxyConnection)
                savepointPgProxyDriver.connect("jdbc:savepointpgproxy://localhost:5432/lealpoint", new Properties());

        //then
        assertEquals(connection1, connection2);
    }

    @Test
    public void shouldNotReturnSameConnectionForSameURLWhenNotInTransaction() throws SQLException
    {
        //given
        SavepointProxyConnection connection1 = (SavepointProxyConnection)
                savepointPgProxyDriver.connect("jdbc:savepointpgproxy://localhost:5432/lealpoint", new Properties());

        //when
        SavepointProxyConnection connection2 = (SavepointProxyConnection)
                savepointPgProxyDriver.connect("jdbc:savepointpgproxy://localhost:5432/lealpoint", new Properties());

        //then
        assertNotEquals(connection1, connection2);
    }

    @Test
    public void shouldReturnDifferentConnectionForSameURLWhenInTransactionButWrappedConnectionIsClosed() throws SQLException
    {
        //given
        SavepointProxyConnection connection1 = (SavepointProxyConnection)
                savepointPgProxyDriver.connect("jdbc:savepointpgproxy://localhost:5432/lealpoint", new Properties());
        connection1.beginTransactionForAutomationTest();
        given(connection.isClosed()).willReturn(true);

        //when
        SavepointProxyConnection connection2 = (SavepointProxyConnection)
                savepointPgProxyDriver.connect("jdbc:savepointpgproxy://localhost:5432/lealpoint", new Properties());

        //then
        assertNotEquals(connection1, connection2);
    }

    @Test
    public void shouldReturnDifferentConnectionForSameURLWhenInTransactionButConnectionIsClosed() throws SQLException
    {
        //given
        SavepointProxyConnection connection1 = (SavepointProxyConnection)
                savepointPgProxyDriver.connect("jdbc:savepointpgproxy://localhost:5432/lealpoint", new Properties());
        connection1.beginTransactionForAutomationTest();
        connection1.rollbackTransactionForAutomationTest();
        connection1.close();

        //when
        SavepointProxyConnection connection2 = (SavepointProxyConnection)
                savepointPgProxyDriver.connect("jdbc:savepointpgproxy://localhost:5432/lealpoint", new Properties());

        //then
        assertNotEquals(connection1, connection2);
    }

    @Test
    public void shouldCloseRollBackedTransactionAndReturnNewOne() throws SQLException
    {
        //given
        SavepointProxyConnection connection1 = (SavepointProxyConnection)
                savepointPgProxyDriver.connect("jdbc:savepointpgproxy://localhost:5432/lealpoint", new Properties());
        connection1.beginTransactionForAutomationTest();
        connection1.rollbackTransactionForAutomationTest();
        connection1.setAutoCommit(true);
        given(connection.getAutoCommit()).willReturn(true);
        savepointPgProxyDriver.setProxyConnectionActive(true);

        //when
        SavepointProxyConnection connection2 = (SavepointProxyConnection)
                savepointPgProxyDriver.connect("jdbc:savepointpgproxy://localhost:5432/lealpoint", new Properties());

        //then
        verify(connection).close();
        assertNotEquals(connection1, connection2);
    }
}
