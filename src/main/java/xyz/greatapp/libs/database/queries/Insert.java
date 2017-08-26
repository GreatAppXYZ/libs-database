package xyz.greatapp.libs.database.queries;

import org.json.JSONObject;
import xyz.greatapp.libs.database.adapter.DataBaseAdapter;
import xyz.greatapp.libs.database.util.DbBuilder;
import xyz.greatapp.libs.service.ServiceResult;
import xyz.greatapp.libs.service.database.requests.fields.ColumnValue;
import xyz.greatapp.libs.service.database.requests.InsertQueryRQ;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Insert {

    private final DataBaseAdapter databaseAdapter;
    private final String schema;
    private final InsertQueryRQ query;

    public Insert(DataBaseAdapter databaseAdapter, String schema, InsertQueryRQ query) {
        this.databaseAdapter = databaseAdapter;
        this.schema = schema;
        this.query = query;
    }

    public ServiceResult execute() throws Exception {
        String newId = databaseAdapter.executeInsert(new DbBuilder() {
            @Override
            public String sql() throws SQLException {
                return "INSERT INTO "
                        + schema + query.getTable() + " " +
                        addValuesForInsert(query.getColumnValues(), query.getIdColumnName());
            }

            @Override
            public ColumnValue[] values() {
                return query.getColumnValues();
            }

            @Override
            public JSONObject build(ResultSet resultSet) throws Exception {
                return null;
            }
        });
        return new ServiceResult(true, "", newId);
    }

    private String addValuesForInsert(ColumnValue[] columnValues, String idColumnName) {
        StringBuilder valuesForInsert = new StringBuilder(" (");
        String separator = "";
        for (ColumnValue filter : columnValues) {
            valuesForInsert.append(separator);
            valuesForInsert.append(filter.getColumn());
            separator = ", ";
        }
        valuesForInsert.append(") VALUES (");
        separator = "";
        for (ColumnValue ignored : columnValues) {
            valuesForInsert.append(separator);
            valuesForInsert.append("?");
            separator = ", ";
        }
        valuesForInsert.append(") RETURNING ").append(idColumnName).append(";");
        return valuesForInsert.toString();
    }
}
