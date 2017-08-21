package xyz.greatapp.libs.database.adapter.prepared_values;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StringArrayPreparedValue extends PreparedValue<String[]>
{

    StringArrayPreparedValue(String[] obj, PreparedStatement statement, int position)
    {
        super(obj, statement, position);
    }

    @Override
    public void prepare() throws SQLException
    {
        statement.setArray(position, statement.getConnection().createArrayOf("varchar", value));
    }
}
