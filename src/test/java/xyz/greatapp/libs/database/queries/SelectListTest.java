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
import xyz.greatapp.libs.service.database.requests.fields.ColumnValue;
import xyz.greatapp.libs.service.database.requests.SelectQueryRQ;
import xyz.greatapp.libs.service.database.requests.fields.Join;

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
        assertEquals("SELECT * FROM greatappxyz.table WHERE greatappxyz.table.column1 = ? AND " +
                "greatappxyz.table.column2 = ?;", sql);
    }

    @Test
    public void shouldConvertRequestOnSelectStatementWithJoin() throws Exception {
        // given
        Join[] joins = {
                new Join("tableB", "columnA", "columnB")
        };
        SelectQueryRQ query = new SelectQueryRQ("tableA", new ColumnValue[0], joins);

        select = new SelectList(databaseAdapter, "greatappxyz.", query);

        // when
        select.execute();

        // then
        ArgumentCaptor<DbBuilder> dbBuilder = ArgumentCaptor.forClass(DbBuilder.class);
        verify(databaseAdapter).selectList(dbBuilder.capture());

        String sql = dbBuilder.getValue().sql();
        assertEquals("SELECT * FROM greatappxyz.tableA INNER JOIN tableB ON " +
                "greatappxyz.tableA.columnA = greatappxyz.tableB.columnB;", sql);
    }

    @Test
    public void shouldConvertRequestOnSelectStatementWithJoinAndWhere() throws Exception {
        // given
        ColumnValue[] filters = new ColumnValue[] {
          new ColumnValue("columnA", "23")
        };
        Join[] joins = {
                new Join("tableB", "columnA", "columnB")
        };
        SelectQueryRQ query = new SelectQueryRQ("tableA", filters, joins);

        select = new SelectList(databaseAdapter, "greatappxyz.", query);

        // when
        select.execute();

        // then
        ArgumentCaptor<DbBuilder> dbBuilder = ArgumentCaptor.forClass(DbBuilder.class);
        verify(databaseAdapter).selectList(dbBuilder.capture());

        String sql = dbBuilder.getValue().sql();
        assertEquals("SELECT * FROM greatappxyz.tableA INNER JOIN tableB ON " +
                "greatappxyz.tableA.columnA = greatappxyz.tableB.columnB " +
                "WHERE greatappxyz.tableA.columnA = ?;", sql);
    }

    @Test
    public void shouldConvertRequestOnSelectStatementWithJoinAndWhereFromRightTable() throws Exception {
        // given
        ColumnValue[] filters = new ColumnValue[] {
                new ColumnValue("columnB", "23", "tableB")};
        Join[] joins = {
                new Join("tableB", "columnA", "columnB")
        };
        SelectQueryRQ query = new SelectQueryRQ("tableA", filters, joins);

        select = new SelectList(databaseAdapter, "greatappxyz.", query);

        // when
        select.execute();

        // then
        ArgumentCaptor<DbBuilder> dbBuilder = ArgumentCaptor.forClass(DbBuilder.class);
        verify(databaseAdapter).selectList(dbBuilder.capture());

        String sql = dbBuilder.getValue().sql();
        assertEquals("SELECT * FROM greatappxyz.tableA INNER JOIN tableB ON " +
                "greatappxyz.tableA.columnA = greatappxyz.tableB.columnB " +
                "WHERE greatappxyz.tableB.columnB = ?;", sql);
    }
}