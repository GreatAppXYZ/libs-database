package xyz.greatapp.libs.database.queries;

import org.json.JSONObject;
import xyz.greatapp.libs.service.database.requests.fields.ColumnValue;
import xyz.greatapp.libs.service.database.requests.fields.Join;

import java.sql.ResultSet;

class Common {

    String addWhere(ColumnValue[] filters) {
        if (filters == null || filters.length == 0) {
            return "";
        } else {
            String andClause = "WHERE ";
            StringBuilder whereClause = new StringBuilder(" ");
            for (ColumnValue filter : filters) {
                whereClause.append(andClause);
                whereClause.append(filter.getColumn()).append(" = ?");
                andClause = " AND ";
            }
            return whereClause.toString();
        }
    }

    String addJoin(Join[] joins, String schema, String table) {
        if (joins == null || joins.length == 0) {
            return "";
        } else {
            String innerClause = "INNER JOIN ";
            StringBuilder innerStatement = new StringBuilder(" ");
            for (Join join : joins) {
                innerStatement.append(innerClause);
                innerStatement.append(join.getTable()).append(" ON ");
                innerStatement.append(schema).append(table).append(".").append(join.getLeftColumn()).append(" = ");
                innerStatement.append(schema).append(join.getTable()).append(".").append(join.getRightColumn());
            }
            return innerStatement.toString();
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
