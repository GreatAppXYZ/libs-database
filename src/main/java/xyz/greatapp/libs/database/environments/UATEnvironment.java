package xyz.greatapp.libs.database.environments;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UATEnvironment extends DatabaseEnvironment
{
    @Value("${db_driver:jdbc:postgresql://}")
    private String dbDriver;

    @Value("${db_driver_class:org.postgresql.Driver}")
    private String dbDriverClass;

    @Value("${JDBC_DATABASE_URL:jdbc:postgresql://localhost:5432}")
    private String dbUrl;

    @Value("${JDBC_DATABASE_USERNAME:postgres}")
    private String dbUser;

    @Value("${JDBC_DATABASE_PASSWORD:secret}")
    private String dbPassword;

    @Value("${db_schema:greatappxyz}")
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
        return "uat.";
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
        if (!(obj instanceof UATEnvironment))
        {
            return false;
        }
        UATEnvironment that = (UATEnvironment) obj;
        return getDatabasePath().equals(that.getDatabasePath());
    }

    @Override
    public int hashCode()
    {
        return getDatabasePath().hashCode();
    }
}
