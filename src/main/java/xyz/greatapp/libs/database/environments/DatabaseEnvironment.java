package xyz.greatapp.libs.database.environments;

public abstract class DatabaseEnvironment
{
    public abstract String getDatabasePath();

    public abstract String getDatabaseDriverClass();

    public abstract String getDatabaseUsername();

    public abstract String getDatabasePassword();

    public abstract String getSchema();

    public abstract String getURIPrefix();

    public abstract String getSearchPathSettingQuery();
}
