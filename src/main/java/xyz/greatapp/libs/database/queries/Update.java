package xyz.greatapp.libs.database.queries;

import com.google.common.collect.ObjectArrays;
import org.json.JSONObject;
import xyz.greatapp.libs.database.adapter.DataBaseAdapter;
import xyz.greatapp.libs.database.util.DbBuilder;
import xyz.greatapp.libs.service.ServiceResult;
import xyz.greatapp.libs.service.database.requests.fields.ColumnValue;
import xyz.greatapp.libs.service.database.requests.UpdateQueryRQ;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Update {

    private final Common c = new Common();
    private final DataBaseAdapter databaseAdapter;
    private final String schema;
    private final UpdateQueryRQ query;

    Update(DataBaseAdapter databaseAdapter, String schema, UpdateQueryRQ query) {
        this.databaseAdapter = databaseAdapter;
        this.schema = schema;
        this.query = query;
    }

    public ServiceResult execute() throws Exception {
        long updatedRows = databaseAdapter.executeUpdate(new DbBuilder() {
            @Override
            public String sql() throws SQLException {
                return "UPDATE " + schema + query.getTable()
                        + " SET " + addSets(query.getSets())
                        + c.addWhere(query.getFilters(), schema, query.getTable())
                        + ";";
            }

            @Override
            public ColumnValue[] values() {
                return ObjectArrays.concat(query.getSets(), query.getFilters(), ColumnValue.class);
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

    private String addSets(ColumnValue[] sets) {
        StringBuilder sb = new StringBuilder();
        for (ColumnValue filter : sets) {
            sb.append(filter.getColumn()).append(" = ?, ");
        }
        sb.replace(sb.lastIndexOf(","), sb.lastIndexOf(",") + 1, "");
        return sb.toString();
    }
}
