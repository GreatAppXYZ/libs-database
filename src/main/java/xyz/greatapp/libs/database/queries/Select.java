package xyz.greatapp.libs.database.queries;

import org.json.JSONObject;
import xyz.greatapp.libs.database.adapter.DataBaseAdapter;
import xyz.greatapp.libs.database.util.DbBuilder;
import xyz.greatapp.libs.service.ServiceResult;
import xyz.greatapp.libs.service.requests.database.ColumnValue;
import xyz.greatapp.libs.service.requests.database.SelectQueryRQ;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Select {

    private final DataBaseAdapter databaseAdapter;
    private final String schema;
    private final SelectQueryRQ query;

    public Select(DataBaseAdapter databaseAdapter, String schema, SelectQueryRQ query) {
        this.databaseAdapter = databaseAdapter;
        this.schema = schema;
        this.query = query;
    }

    public ServiceResult execute() throws Exception
    {
        JSONObject object = databaseAdapter.selectObject(new DbBuilder()
        {
            @Override
            public String sql() throws SQLException
            {
                return "SELECT * FROM " + schema + query.getTable() + addWhere(query.getFilters());
            }

            @Override
            public ColumnValue[] values()
            {
                return query.getFilters();
            }

            @Override
            public JSONObject build(ResultSet resultSet) throws Exception
            {
                int columnCount = resultSet.getMetaData().getColumnCount();
                JSONObject jsonObject = new JSONObject();
                buildObject(resultSet, columnCount, jsonObject);
                return jsonObject;
            }
        });

        return new ServiceResult(true, "", object.toString());
    }

    private String addWhere(ColumnValue[] filters)
    {
        if (filters == null || filters.length == 0)
        {
            return ";";
        }
        else
        {
            String andClause = " WHERE ";
            StringBuilder whereClause = new StringBuilder(" ");
            for (ColumnValue filter : filters)
            {
                whereClause.append(andClause);
                whereClause.append(filter.getColumn()).append(" = ? ");
                andClause = " AND ";
            }
            return whereClause + ";";
        }
    }


    private void buildObject(ResultSet resultSet, int columnCount, JSONObject jsonObject) throws Exception
    {
        for (int i = 1; i <= columnCount; i++)
        {
            String columnName = resultSet.getMetaData().getColumnName(i);
            Object object = resultSet.getObject(columnName);
            if (object == null)
                object = "";
            jsonObject.put(columnName.toLowerCase(), object);
        }
    }

}
