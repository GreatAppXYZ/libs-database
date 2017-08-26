package xyz.greatapp.libs.database.queries;

import org.json.JSONArray;
import org.json.JSONObject;
import xyz.greatapp.libs.database.adapter.DataBaseAdapter;
import xyz.greatapp.libs.database.util.DbBuilder;
import xyz.greatapp.libs.service.ServiceResult;
import xyz.greatapp.libs.service.database.requests.fields.ColumnValue;
import xyz.greatapp.libs.service.database.requests.SelectQueryRQ;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SelectList {

    private final Common c = new Common();
    private final DataBaseAdapter databaseAdapter;
    private final String schema;
    private final SelectQueryRQ query;

    public SelectList(DataBaseAdapter databaseAdapter, String schema, SelectQueryRQ query) {
        this.databaseAdapter = databaseAdapter;
        this.schema = schema;
        this.query = query;
    }

    public ServiceResult execute() throws Exception {
        JSONArray object = databaseAdapter.selectList(new DbBuilder() {
            @Override
            public String sql() throws SQLException {
                return "SELECT * FROM " +
                        schema +
                        query.getTable() +
                        c.addJoin(query.getJoins(), schema, query.getTable()) +
                        c.addWhere(query.getFilters()) + ";";
            }

            @Override
            public ColumnValue[] values() {
                return query.getFilters();
            }

            @Override
            public JSONObject build(ResultSet resultSet) throws Exception {
                int columnCount = resultSet.getMetaData().getColumnCount();
                JSONObject jsonObject = new JSONObject();
                c.buildObject(resultSet, columnCount, jsonObject);
                return jsonObject;
            }
        });

        return new ServiceResult(true, "", object.toString());
    }
}
