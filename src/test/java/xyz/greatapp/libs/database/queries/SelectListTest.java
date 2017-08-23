package xyz.greatapp.libs.database.queries;

import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import xyz.greatapp.libs.database.adapter.DataBaseAdapter;
import xyz.greatapp.libs.database.util.DbBuilder;
import xyz.greatapp.libs.service.requests.database.ColumnValue;
import xyz.greatapp.libs.service.requests.database.SelectQueryRQ;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SelectListTest {

    @Mock
    private DataBaseAdapter databaseAdapter;
    private SelectList select;

    @Before
    public void setUp() throws Exception {
        when(databaseAdapter.selectList(any())).thenReturn(new JSONArray());
    }

    @Test
    public void shouldConvertRequestOnSelectStatement() throws Exception {
        // given
        SelectQueryRQ query = new SelectQueryRQ("table", new ColumnValue[]{
                new ColumnValue("column1", "value1"),
                new ColumnValue("column2", "value2")
        });

        select = new SelectList(databaseAdapter, "greatappxyz.", query);

        // when
        select.execute();

        // then
        ArgumentCaptor<DbBuilder> dbBuilder = ArgumentCaptor.forClass(DbBuilder.class);
        verify(databaseAdapter).selectList(dbBuilder.capture());

        String sql = dbBuilder.getValue().sql();
        assertEquals("SELECT * FROM greatappxyz.table  WHERE column1 = ?  AND column2 = ? ;", sql);
    }
}