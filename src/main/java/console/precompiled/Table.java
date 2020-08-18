package console.precompiled;

import java.util.List;

public class Table {

    private String tableName;
    private String key;
    private List<String> valueFields;
    private String optional = "";

    public Table() {}

    public String getTableName() {
        return tableName;
    }

    public String getKey() {
        return key;
    }

    public List<String> getValueFields() {
        return valueFields;
    }

    public String getOptional() {
        return optional;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValueFields(List<String> valueFields) {
        this.valueFields = valueFields;
    }

    public void setOptional(String optional) {
        this.optional = optional;
    }
}
