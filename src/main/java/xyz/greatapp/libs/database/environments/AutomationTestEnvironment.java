package xyz.greatapp.libs.database.environments;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AutomationTestEnvironment extends DatabaseEnvironment
{
    @Value("jdbc:savepointpgproxy://")
    private String dbDriver;

    @Value("xyz.greatapp.libs.database.automation_database_driver.SavepointPgProxyDriver")
    private String dbDriverClass;

    @Value("jdbc:savepointpgproxy://localhost:5432")
    private String dbUrl;

    @Value("postgres")
    private String dbUser;

    @Value("root")
    private String dbPassword;

    @Value("greatappxyz_test")
    private String schema;

    @Override
    public String getDatabasePath()
    {
        return dbUrl;
    }

    public String getDatabaseDriverClass()
    {
        return dbDriverClass;
    }

    public String getDatabaseUsername()
    {
        return dbUser;
    }

    public String getDatabasePassword()
    {
        return dbPassword;
    }

    @Override
    public String getSchema()
    {
        return schema;
    }

    @Override public String getURIPrefix()
    {
        return "test.";
    }

    @Override public String getSearchPathSettingQuery()
    {
        return "ALTER ROLE "
                + getDatabaseUsername()
                + " SET search_path = "
                + getSchema() + ";";
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof AutomationTestEnvironment))
        {
            return false;
        }
        AutomationTestEnvironment that = (AutomationTestEnvironment) obj;
        return getDatabasePath().equals(that.getDatabasePath());
    }

    @Override
    public int hashCode()
    {
        return getDatabasePath().hashCode();
    }
}
