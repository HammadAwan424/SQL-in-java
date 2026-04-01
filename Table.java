import java.security.Timestamp;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class Table {
    String nextLine = "\n"; // for readability
    String tableName;
    String activeColumnForChaining;
    List<Column> columnsList = new ArrayList<>();

    public Table(String tableName) {
        this.tableName = tableName;
    }

    public Table has(String columnName) {
        activeColumnForChaining = columnName;
        return this;
    }

    public Table asString(int size) {
        columnsList.add(new Column(
            activeColumnForChaining,
            ParameterizedColumnType.VARCHAR,
            new SizeParameter(size)
        ));
        return this;
    }

    public Table asDecimal(int precision, int scale) {
        columnsList.add(new Column(
            activeColumnForChaining,
            ParameterizedColumnType.DECIMAL,
            new PrecisionScaleParameter(precision, scale)
        ));
        return this;
    }

    public Table asInt() {
        columnsList.add(new Column(
            activeColumnForChaining, 
            NonParameterizedColumnType.INTEGER
        ));
        return this;
    }

    public String toSQL() {
        String columnString = "";
        for (Column C: columnsList) {
            columnString = columnString.concat("    " + C.toSQL() + nextLine); // Username VARCHAR(20)
        }
        return tableName + "(" + nextLine + columnString + ")";
    }
}


class Column {
    String name;
    ColumnType columnType;
    ColumnTypeParameters parameters;

    Column(String name, NonParameterizedColumnType columnType) {
        this.name = name;
        this.columnType = columnType;
    }

    Column(String name, ParameterizedColumnType columnType, ColumnTypeParameters parameters) {
        this.name = name;
        this.columnType = columnType;
        this.parameters = parameters;
    }

    public String toSQL() {
        if (this.columnType instanceof ParameterizedColumnType) 
            return this.name + " " + this.columnType.getSQLType() + parameters.toSQL();
        return this.name + " " + this.columnType.getSQLType();
    }
}



interface ColumnType {
    String getSQLType();
    Class<?> getJavaType();
}

// Ignoring BINARY, BLOB and different sized INTEGERs
// DONE: improve it so the instances don't contain unnecessary varaibles
// By creating two enum types, Parametereiz and NonParametereized, and using
// composition, the ones which need params are passed a Parameter object instead
// of all tyeps in a single ColumnType and storing all possible parameters
// Changes: 1) enum ColumnType -> interface ColumnType
// enum ColumnType -> 1) ParamtereizedColumnType & NonParameterizedColumnType
enum ParameterizedColumnType implements ColumnType {
    VARCHAR(String.class, "VARCHAR"),
    DECIMAL(Double.class, "DECIMAL");

    
    Class<?> type;
    String SQLType;

    ParameterizedColumnType(Class<?> type, String SQLType) {
        this.type = type;
        this.SQLType = SQLType;
    }

    public String getSQLType() {
        return this.SQLType;
    }

    public Class<?> getJavaType() {
        return this.type;
    }
}

enum NonParameterizedColumnType implements ColumnType {
    CHAR(String.class, "CHAR"),
    
    INTEGER(Integer.class, "INTEGER"),

    // TODO: learn what java.sql.* provides and their difference from java.util.*
    DATE(Date.class, "DATE"),
    TIME(Time.class, "TIME"),
    // TODO: implement this somehow
    // DATETIME(Date),

    TIMESTAMP(Timestamp.class, "TIMESTAMP"),

    BOOLEAN(Boolean.class, "BOOLEAN");


    Class<?> type;
    String SQLType;

    NonParameterizedColumnType(Class<?> type, String SQLType) {
        this.type = type;
        this.SQLType = SQLType;
    }

    public String getSQLType() {
        return this.SQLType;
    }

    public Class<?> getJavaType() {
        return this.type;
    }
}



// ColumnTypeParameters composed inside ColumnType to represent
// the parameters of the ColumnType
interface ColumnTypeParameters {
    String toSQL();
}

class SizeParameter implements ColumnTypeParameters {
    int size;
    SizeParameter(int size) { this.size = size; }
    public String toSQL() { return "(" + size + ")"; }
}

class PrecisionScaleParameter implements ColumnTypeParameters {
    int precision, scale;
    PrecisionScaleParameter(int precision, int scale) {
        this.precision = precision;
        this.scale = scale;
    }
    public String toSQL() { return "(" + precision + ", " + scale + ")"; }
}
