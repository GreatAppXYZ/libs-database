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
import xyz.greatapp.libs.service.database.requests.UpdateQueryRQ;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpdateTest {

    @Mock
    private DataBaseAdapter databaseAdapter;

    @Before
    public void setUp() throws Exception {
        when(databaseAdapter.executeUpdate(any())).thenReturn(1);
    }

    @Test
    public void shouldConvertRequestOnSelectStatement() throws Exception {
        // given
        UpdateQueryRQ query = new UpdateQueryRQ("table", new ColumnValue[]{
                new ColumnValue("column1", "value1"),
                new ColumnValue("column2", "value2")
        }, new ColumnValue[]{
                new ColumnValue("column3", "value3"),
                new ColumnValue("column4", "value4")});

        Update update = new Update(databaseAdapter, "greatappxyz.", query);

        // when
        update.execute();

        // then
        ArgumentCaptor<DbBuilder> dbBuilder = ArgumentCaptor.forClass(DbBuilder.class);
        verify(databaseAdapter).executeUpdate(dbBuilder.capture());

        String sql = dbBuilder.getValue().sql();
        assertEquals("UPDATE greatappxyz.table " +
                "SET column1 = ?, column2 = ?  " +
                "WHERE greatappxyz.table.column3 = ? " +
                "AND greatappxyz.table.column4 = ?;", sql);
    }
}