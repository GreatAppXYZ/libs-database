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
import xyz.greatapp.libs.service.database.requests.DeleteQueryRQ;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeleteTest {

    @Mock
    private DataBaseAdapter databaseAdapter;
    private Delete delete;

    @Before
    public void setUp() throws Exception {
        when(databaseAdapter.executeUpdate(any())).thenReturn(1);
    }

    @Test
    public void shouldConvertRequestOnSelectStatement() throws Exception {
        // given
        DeleteQueryRQ query = new DeleteQueryRQ("table", new ColumnValue[]{
                new ColumnValue("column1", "value1"),
                new ColumnValue("column2", "value2")});
        delete = new Delete(databaseAdapter, "greatappxyz.", query);

        // when
        delete.execute();

        // then
        ArgumentCaptor<DbBuilder> dbBuilder = ArgumentCaptor.forClass(DbBuilder.class);
        verify(databaseAdapter).executeUpdate(dbBuilder.capture());

        String sql = dbBuilder.getValue().sql();
        assertEquals("DELETE FROM greatappxyz.table " +
                "WHERE greatappxyz.table.column1 = ? " +
                "AND greatappxyz.table.column2 = ?;", sql);
    }
}