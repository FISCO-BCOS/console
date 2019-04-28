package console.common;

public class TableInfo {

    private String key;
    private String valueFields;

    public TableInfo(String key, String valueFields) {
        super();
        this.key = key;
        this.valueFields = valueFields;
    }

    public String getKey() {
        return key;
    }

    public String getValueFields() {
        return valueFields;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValueFields(String valueFields) {
        this.valueFields = valueFields;
    }
}
