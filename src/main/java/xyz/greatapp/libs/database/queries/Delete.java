package xyz.greatapp.libs.database.queries;

import org.json.JSONObject;
import xyz.greatapp.libs.database.adapter.DataBaseAdapter;
import xyz.greatapp.libs.database.util.DbBuilder;
import xyz.greatapp.libs.service.ServiceResult;
import xyz.greatapp.libs.service.database.requests.fields.ColumnValue;
import xyz.greatapp.libs.service.database.requests.DeleteQueryRQ;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Delete {

    private final Common c = new Common();
    private final DataBaseAdapter databaseAdapter;
    private final String schema;
    private final DeleteQueryRQ query;

    public Delete(DataBaseAdapter databaseAdapter, String schema, DeleteQueryRQ query) {
        this.databaseAdapter = databaseAdapter;
        this.schema = schema;
        this.query = query;
    }

    public ServiceResult execute() throws Exception {
        long updatedRows = databaseAdapter.executeUpdate(new DbBuilder() {
            @Override
            public String sql() throws SQLException {
                return "DELETE FROM " + schema + query.getTable()
                        + c.addWhere(query.getFilters()) + ";";
            }

            @Override
            public ColumnValue[] values() {
                return query.getFilters();
            }

            @Override
            public JSONObject build(ResultSet resultSet) throws Exception {
                return null;
            }
        });
        if (updatedRows == 0) {
            return new ServiceResult(false, "none.rows.were.updated");
        }
        return new ServiceResult(true, "", Long.toString(updatedRows));
    }
}
