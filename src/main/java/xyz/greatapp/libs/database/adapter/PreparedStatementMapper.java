package xyz.greatapp.libs.database.adapter;

import xyz.greatapp.libs.database.adapter.prepared_values.PreparedValueFactory;
import xyz.greatapp.libs.service.database.requests.fields.ColumnValue;

import java.sql.PreparedStatement;
import java.sql.SQLException;

class PreparedStatementMapper {
    void map(PreparedStatement statement, ColumnValue[] values) throws SQLException
    {
        if (values != null)
        {
            int position = 1;
            for (ColumnValue value : values) {
                new PreparedValueFactory().createPreparedValueFor(value, statement, position++)
                        .prepare();

            }
        }
    }
}
