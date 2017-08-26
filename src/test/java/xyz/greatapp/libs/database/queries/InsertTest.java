package xyz.greatapp.libs.database.queries;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import xyz.greatapp.libs.database.adapter.DataBaseAdapter;
import xyz.greatapp.libs.database.util.DbBuilder;
import xyz.greatapp.libs.service.database.requests.fields.ColumnValue;
import xyz.greatapp.libs.service.database.requests.InsertQueryRQ;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InsertTest {

    @Mock
    private DataBaseAdapter databaseAdapter;
    private Insert insert;

    @Before
    public void setUp() throws Exception {
        when(databaseAdapter.executeInsert(any())).thenReturn("");
    }

    @Test
    public void shouldConvertRequestOnSelectStatement() throws Exception {
        // given
        InsertQueryRQ query = new InsertQueryRQ("table", new ColumnValue[]{
                new ColumnValue("column1", "value1"),
                new ColumnValue("column2", "value2")
        }, "id");

        insert = new Insert(databaseAdapter, "greatappxyz.", query);

        // whens
        insert.execute();

        // then
        ArgumentCaptor<DbBuilder> dbBuilder = ArgumentCaptor.forClass(DbBuilder.class);
        verify(databaseAdapter).executeInsert(dbBuilder.capture());

        String sql = dbBuilder.getValue().sql();
        assertEquals("INSERT INTO greatappxyz.table  (column1, column2) VALUES (?, ?) RETURNING id;", sql);
    }
}