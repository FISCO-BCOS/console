package console.precompiled.model;

import java.util.List;
import org.fisco.bcos.sdk.v3.contract.precompiled.crud.common.Common;

public class Table {

    private String tableName;
    private String keyFieldName;
    private List<String> valueFields;
    private String optional = "";
    private Common.TableKeyOrder keyOrder = Common.TableKeyOrder.Lexicographic;

    public Table() {}

    public String getTableName() {
        return tableName;
    }

    public String getKeyFieldName() {
        return keyFieldName;
    }

    public void setKeyFieldName(String keyFieldName) {
        this.keyFieldName = keyFieldName;
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

    public void setValueFields(List<String> valueFields) {
        this.valueFields = valueFields;
    }

    public void setOptional(String optional) {
        this.optional = optional;
    }

    public Common.TableKeyOrder getKeyOrder() {
        return keyOrder;
    }

    public void setKeyOrder(Common.TableKeyOrder keyOrder) {
        this.keyOrder = keyOrder;
    }
}
