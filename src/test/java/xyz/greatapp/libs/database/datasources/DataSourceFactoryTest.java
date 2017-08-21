package xyz.greatapp.libs.database.datasources;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import xyz.greatapp.libs.database.environments.ProdEnvironment;
import xyz.greatapp.libs.database.environments.UATEnvironment;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.junit.Assert.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DataSourceFactoryTest
{
    private DataSourceFactory dataSourceFactory;
    @Mock
    private ProdEnvironment prodEnvironment;
    @Mock
    private UATEnvironment uatEnvironment;
    @Mock
    private DriverManagerDataSourceFactory driverManagerDataSourceFactory;
    @Mock
    private DriverManagerDataSource driverManagerDataSource;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;

    @Before
    public void setUp() throws Exception
    {
        dataSourceFactory = new DataSourceFactory(driverManagerDataSourceFactory);
        given(driverManagerDataSourceFactory.createDriverManagerDataSource()).willReturn(driverManagerDataSource);
    }

    @Test
    public void shouldReturnNewDataSource() throws Exception
    {
        // when
        DataSource dataSource = dataSourceFactory.getDataSource(prodEnvironment);

        // then
        assertNotNull(dataSource);
    }

    @Test
    public void shouldCreateOnlyOneDataSourceForSameEnvironment() throws Exception
    {

        // when
        dataSourceFactory.getDataSource(prodEnvironment);
        dataSourceFactory.getDataSource(prodEnvironment);

        // then
        verify(driverManagerDataSourceFactory, times(1)).createDriverManagerDataSource();
    }

    @Test
    public void shouldCreateOneDataSourceForEveryDifferentEnvironment() throws Exception
    {

        // when
        dataSourceFactory.getDataSource(prodEnvironment);
        dataSourceFactory.getDataSource(prodEnvironment);
        dataSourceFactory.getDataSource(uatEnvironment);

        // then
        verify(driverManagerDataSourceFactory, times(2)).createDriverManagerDataSource();
    }
}
