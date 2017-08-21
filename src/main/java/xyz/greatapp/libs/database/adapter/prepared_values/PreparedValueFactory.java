package xyz.greatapp.libs.database.adapter.prepared_values;

import xyz.greatapp.libs.service.requests.database.ColumnValue;

import java.sql.PreparedStatement;

public class PreparedValueFactory
{
    public PreparedValue createPreparedValueFor(ColumnValue obj, PreparedStatement statement, int position) {

        if (obj.getValue() == null)
            return new NullPreparedValue(statement, position);

        if (obj.getValue() instanceof String)
            return new StringPreparedValue((String)obj.getValue(), statement, position);

        if (obj.getValue() instanceof Integer)
            return new IntegerPreparedValue((Integer)obj.getValue(), statement, position);

        if (obj.getValue() instanceof Boolean)
            return new BooleanPreparedValue((Boolean)obj.getValue(), statement, position);

        if (obj.getValue() instanceof Double)
            return new DoublePreparedValue((Double) obj.getValue(), statement, position);

        if (obj.getValue() instanceof Long)
            return new LongPreparedValue((Long) obj.getValue(), statement, position);

        if (obj.getValue() instanceof String[])
            return new StringArrayPreparedValue((String[]) obj.getValue(), statement, position);

        throw new RuntimeException("Unsupported SQL type for object : " + obj.getValue());
    }
}
