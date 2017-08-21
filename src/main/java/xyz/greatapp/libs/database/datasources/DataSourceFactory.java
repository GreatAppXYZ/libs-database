package xyz.greatapp.libs.database.datasources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;
import xyz.greatapp.libs.database.util.Computable;
import xyz.greatapp.libs.database.util.Memoizer;
import xyz.greatapp.libs.database.environments.DatabaseEnvironment;

import javax.sql.DataSource;

@Component
public class DataSourceFactory
{
    private final Computable<DatabaseEnvironment, DataSource> dataSources;

    @Autowired
    public DataSourceFactory(DriverManagerDataSourceFactory factory)
    {
        dataSources = new Memoizer<>(env -> getDataSource(factory, env));
    }

    private DataSource getDataSource(DriverManagerDataSourceFactory factory, DatabaseEnvironment environment)
    {
        DriverManagerDataSource dataSource = factory.createDriverManagerDataSource();
        dataSource.setDriverClassName(environment.getDatabaseDriverClass());
        dataSource.setUrl(environment.getDatabasePath());
        dataSource.setUsername(environment.getDatabaseUsername());
        dataSource.setPassword(environment.getDatabasePassword());
        return dataSource;
    }

    public DataSource getDataSource(DatabaseEnvironment environment) throws Exception
    {
        final DataSource dataSource = dataSources.compute(environment);
        if (dataSource == null)
            throw new RuntimeException("DataSource cannot be null!");
        return dataSource;
    }
}
