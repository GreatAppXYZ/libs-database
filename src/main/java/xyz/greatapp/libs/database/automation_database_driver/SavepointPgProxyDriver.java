package xyz.greatapp.libs.database.automation_database_driver;

import org.apache.commons.collections15.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class SavepointPgProxyDriver extends org.postgresql.Driver
{
    private static final String DRIVER_URL_PROTOCOL = "jdbc:savepointpgproxy:";
    private static final Pattern DRIVER_URL_REGEX_PATTERN = Pattern.compile("^" + DRIVER_URL_PROTOCOL + ".*");
    private static final String WRAPPED_DRIVER_URL_PROTOCOL = "jdbc:postgresql:";
    private static final String WRAPPED_DRIVER_CLASS_NAME = "org.postgresql.Driver";
    private static final Pattern DRIVER_URL_PROTOCOL_PATTERN = Pattern.compile(DRIVER_URL_PROTOCOL, Pattern.LITERAL);
    private static final Pattern URL_CONNECTION_KEY_REGEX_PATTERN = Pattern.compile("\\?.*$");
    private final Map<MultiKey<Object>, Queue<SavepointProxyConnection>> sharedConnectionMap = new ConcurrentHashMap<>();
    private Driver _wrappedDriver;
    private boolean _isProxyConnectionActive = false;

    SavepointPgProxyDriver()
    {
    }

    static
    {
        registerDriver();
    }

    static SavepointPgProxyDriver registerDriver()
    {
        SavepointPgProxyDriver driver = new SavepointPgProxyDriver();
        driver.setWrappedDriver(findWrappedDriver());
        try
        {
            DriverManager.registerDriver(driver);
        }
        catch (SQLException e)
        {
            logAndThrowException(e.getMessage());
        }
        return driver;
    }

    private static void logAndThrowException(String errorMessage)
    {
        throw new RuntimeException(errorMessage);
    }

    private static Driver findWrappedDriver()
    {
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements())
        {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getName().equals(WRAPPED_DRIVER_CLASS_NAME))
            {
                return driver;
            }
        }
        logAndThrowException("Could not find PostgreSQL JDBC Driver");
        return null;
    }

    @Override
    public boolean acceptsURL(String url)
    {
        return StringUtils.isNotBlank(url) && DRIVER_URL_REGEX_PATTERN.matcher(url).matches();
    }

    void setWrappedDriver(Driver driver)
    {
        _wrappedDriver = driver;
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException
    {
        if (StringUtils.isNotBlank(url) && DRIVER_URL_REGEX_PATTERN.matcher(url).matches())
        {
            String urlForWrappedDriver = DRIVER_URL_PROTOCOL_PATTERN.matcher(url).replaceAll(WRAPPED_DRIVER_URL_PROTOCOL);
            return getConnection(url, info, urlForWrappedDriver);
        }
        logAndThrowException("Could not connect to wrapped driver (PostgreSQL JDBC Driver). url = " + url);
        return null;
    }

    private synchronized Connection getConnection(String url, Properties info, String urlForWrappedDriver) throws SQLException
    {
        String urlForConnectionKey = getUrlForConnectionKey(url);
        MultiKey<Object> connectionKey = new MultiKey<>(new Object[] { urlForConnectionKey, info });

        SavepointProxyConnection savepointProxyConnection = null;

        Queue<SavepointProxyConnection> savepointProxyConnectionList = sharedConnectionMap.get(connectionKey);

        if (savepointProxyConnectionList == null)
        {
            savepointProxyConnectionList = new LinkedList<>();
            savepointProxyConnection = createNewConnection(info, urlForWrappedDriver);
            savepointProxyConnectionList.add(savepointProxyConnection);
            sharedConnectionMap.put(connectionKey, savepointProxyConnectionList);
        }
        else
        {
            cleanupConnections(savepointProxyConnectionList);
            for (SavepointProxyConnection savepointProxyConnectionFromList : savepointProxyConnectionList)
            {
                if (savepointProxyConnectionFromList.isProxyConnectionActive())
                {
                    savepointProxyConnection = savepointProxyConnectionFromList;
                    break;
                }
            }
        }

        if (savepointProxyConnection == null || savepointProxyConnection.isClosed())
        {
            savepointProxyConnection = createNewConnection(info, urlForWrappedDriver);
            savepointProxyConnectionList.add(savepointProxyConnection);
            sharedConnectionMap.put(connectionKey, savepointProxyConnectionList);
        }

        return savepointProxyConnection;
    }

    private String getUrlForConnectionKey(String url)
    {
        return URL_CONNECTION_KEY_REGEX_PATTERN.matcher(url).replaceFirst("");
    }

    private SavepointProxyConnection createNewConnection(Properties info, String urlForWrappedDriver) throws SQLException
    {
        Connection wrappedConnection = _wrappedDriver.connect(urlForWrappedDriver, info);
        SavepointProxyConnection connection = new SavepointProxyConnectionImpl(wrappedConnection, this);
        connection.setConnectionUrl(urlForWrappedDriver);
        return connection;
    }

    private void cleanupConnections(Queue<SavepointProxyConnection> savepointProxyConnectionList) throws SQLException
    {
        if (savepointProxyConnectionList != null)
        {
            List<SavepointProxyConnection> closedConnections = new ArrayList<>();

            for (SavepointProxyConnection savepointProxyConnection : savepointProxyConnectionList)
            {
                if (canCloseConnection(savepointProxyConnection))
                {
                    savepointProxyConnection.close();
                }
            }

            for (SavepointProxyConnection savepointProxyConnection : savepointProxyConnectionList)
            {
                if (savepointProxyConnection.isClosed())
                {
                    closedConnections.add(savepointProxyConnection);
                }
            }

            for (SavepointProxyConnection closedConnection : closedConnections)
            {
                savepointProxyConnectionList.remove(closedConnection);
            }
        }
    }

    private boolean canCloseConnection(SavepointProxyConnection savepointProxyConnection) throws SQLException
    {
        return !savepointProxyConnection.isClosed() && isProxyConnectionActive() && !savepointProxyConnection.isProxyConnectionActive() &&
                savepointProxyConnection.getAutoCommit();
    }

    private boolean isProxyConnectionActive()
    {
        return _isProxyConnectionActive;
    }

    void setProxyConnectionActive(boolean isProxyConnectionActive)
    {
        _isProxyConnectionActive = isProxyConnectionActive;
    }
}
