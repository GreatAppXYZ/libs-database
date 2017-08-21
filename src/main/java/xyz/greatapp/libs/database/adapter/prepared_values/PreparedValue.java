package xyz.greatapp.libs.database.adapter.prepared_values;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class PreparedValue<T>
{
    protected final T value;
    protected final PreparedStatement statement;
    protected final int position;

    PreparedValue(T value, PreparedStatement statement, int position)
    {
        this.value = value;
        this.statement = statement;
        this.position = position;
    }

    public abstract void prepare() throws SQLException;
}
