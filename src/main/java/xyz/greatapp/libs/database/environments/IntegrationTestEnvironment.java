package xyz.greatapp.libs.database.environments;

import org.springframework.stereotype.Component;

@Component
public class IntegrationTestEnvironment extends DatabaseEnvironment
{
    @Override
    public String getDatabasePath()
    {
        return "jdbc:h2:mem:myDb;DB_CLOSE_DELAY=-1";
    }

    @Override
    public String getDatabaseDriverClass()
    {
        return "org.h2.Driver";
    }

    @Override
    public String getDatabaseUsername()
    {
        return "sa";
    }

    @Override
    public String getDatabasePassword()
    {
        return "sa";
    }

    @Override
    public String getSchema()
    {
        return "public";
    }

    @Override
    public String getURIPrefix()
    {
        return null;
    }

    @Override
    public String getSearchPathSettingQuery()
    {
        return "SET SCHEMA_SEARCH_PATH public";
    }
}
