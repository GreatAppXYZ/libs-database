package xyz.greatapp.libs.database.queries;

import org.json.JSONObject;
import xyz.greatapp.libs.service.database.requests.fields.ColumnValue;

import java.sql.ResultSet;

public class Common {

    String addWhere(ColumnValue[] filters) {
        if (filters == null || filters.length == 0) {
            return ";";
        } else {
            String andClause = " WHERE ";
            StringBuilder whereClause = new StringBuilder(" ");
            for (ColumnValue filter : filters) {
                whereClause.append(andClause);
                whereClause.append(filter.getColumn()).append(" = ? ");
                andClause = " AND ";
            }
            return whereClause + ";";
        }
    }

    void buildObject(ResultSet resultSet, int columnCount, JSONObject jsonObject) throws Exception {
        for (int i = 1; i <= columnCount; i++) {
            String columnName = resultSet.getMetaData().getColumnName(i);
            Object object = resultSet.getObject(columnName);
            if (object == null)
                object = "";
            jsonObject.put(columnName.toLowerCase(), object);
        }
    }
}
