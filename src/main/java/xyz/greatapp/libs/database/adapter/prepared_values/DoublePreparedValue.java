package xyz.greatapp.libs.database.adapter.prepared_values;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DoublePreparedValue extends PreparedValue<Double>
{

    DoublePreparedValue(Double obj, PreparedStatement statement, int position)
    {
        super(obj, statement, position);
    }

    @Override
    public void prepare() throws SQLException
    {
        statement.setDouble(position, value);
    }
}
