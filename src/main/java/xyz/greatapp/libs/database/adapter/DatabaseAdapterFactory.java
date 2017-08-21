package xyz.greatapp.libs.database.adapter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.greatapp.libs.database.datasources.DataSourceFactory;
import xyz.greatapp.libs.database.environments.*;
import xyz.greatapp.libs.service.Environment;

@Component
public class DatabaseAdapterFactory
{
    private final DataSourceFactory dataSourceFactory;
    private final DevEnvironment devEnvironment;
    private final UATEnvironment uatEnvironment;
    private final ProdEnvironment prodEnvironment;
    private final AutomationTestEnvironment automationTestEnvironment;
    private final IntegrationTestEnvironment integrationTestEnvironment;

    @Autowired
    public DatabaseAdapterFactory(DataSourceFactory dataSourceFactory, DevEnvironment devEnvironment, UATEnvironment uatEnvironment, ProdEnvironment prodEnvironment, AutomationTestEnvironment automationTestEnvironment, IntegrationTestEnvironment integrationTestEnvironment)
    {
        this.dataSourceFactory = dataSourceFactory;
        this.devEnvironment = devEnvironment;
        this.uatEnvironment = uatEnvironment;
        this.prodEnvironment = prodEnvironment;
        this.automationTestEnvironment = automationTestEnvironment;
        this.integrationTestEnvironment = integrationTestEnvironment;
    }

    public DataBaseAdapter getDatabaseAdapter(Environment environment) throws Exception
    {
        DatabaseEnvironment databaseEnvironment = getDatabaseEnvironment(environment);
        return new DataBaseAdapter(dataSourceFactory.getDataSource(databaseEnvironment), new PreparedStatementMapper());
    }

    private DatabaseEnvironment getDatabaseEnvironment(Environment environment)
    {
        switch (environment)
        {
            case DEV:
                return devEnvironment;
            case UAT:
                return uatEnvironment;
            case PROD:
                return prodEnvironment;
            case AUTOMATION_TEST:
                return automationTestEnvironment;
            case INTEGRATION_TEST:
                return integrationTestEnvironment;
            default:
                return devEnvironment;
        }
    }
}
