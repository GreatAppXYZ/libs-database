package xyz.greatapp.libs.database.util;

import org.json.JSONObject;
import xyz.greatapp.libs.service.requests.database.ColumnValue;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class DbBuilder
{
    public abstract String sql() throws SQLException;

    public abstract ColumnValue[] values();

    public abstract JSONObject build(ResultSet resultSet) throws Exception;
}
