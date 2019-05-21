import "./Table.sol";
pragma experimental ABIEncoderV2;

contract TableTest {
    event selectResult(string name, int item_id, string item_name);
    event insertResult(int count);
    event updateResult(int count);
    event removeResult(int count);
    
    //create table
    function create() public returns(int) {
        TableFactory tf = TableFactory(0x1001); //The fixed address is 0x1001 for TableFactory
        int count = tf.createTable("t_test", "name", "item_id,item_name");
        // createResult(count);
        return count;
    }

    //select records
    function select(string name) public constant returns(string[], int[], string[]){
        TableFactory tf = TableFactory(0x1001);
        Table table = tf.openTable("t_test");
        
        Condition condition = table.newCondition();
        //condition.EQ("name", name);
        
        Entries entries = table.select(name, condition);
        string[] memory user_name_bytes_list = new string[](uint256(entries.size()));
        int[] memory item_id_list = new int[](uint256(entries.size()));
        string[] memory item_name_bytes_list = new string[](uint256(entries.size()));
        
        for(int i=0; i<entries.size(); ++i) {
            Entry entry = entries.get(i);
            
            user_name_bytes_list[uint256(i)] = entry.getString("name");
            item_id_list[uint256(i)] = entry.getInt("item_id");
            item_name_bytes_list[uint256(i)] = entry.getString("item_name");
            selectResult(user_name_bytes_list[uint256(i)], item_id_list[uint256(i)], item_name_bytes_list[uint256(i)]);
        }
 
        return (user_name_bytes_list, item_id_list, item_name_bytes_list);
    }
    //insert records
    function insert(string name, int item_id, string item_name) public returns(int) {
        TableFactory tf = TableFactory(0x1001);
        Table table = tf.openTable("t_test");
        
        Entry entry = table.newEntry();
        entry.set("name", name);
        entry.set("item_id", item_id);
        entry.set("item_name", item_name);
        
        int count = table.insert(name, entry);
        insertResult(count);
        
        return count;
    }
    //update records
    function update(string name, int item_id, string item_name) public returns(int) {
        TableFactory tf = TableFactory(0x1001);
        Table table = tf.openTable("t_test");
        
        Entry entry = table.newEntry();
        entry.set("item_name", item_name);
        
        Condition condition = table.newCondition();
        condition.EQ("name", name);
        condition.EQ("item_id", item_id);
        
        int count = table.update(name, entry, condition);
        updateResult(count);
        
        return count;
    }
    //remove records
    function remove(string name, int item_id) public returns(int){
        TableFactory tf = TableFactory(0x1001);
        Table table = tf.openTable("t_test");
        
        Condition condition = table.newCondition();
        condition.EQ("name", name);
        condition.EQ("item_id", item_id);
        
        int count = table.remove(name, condition);
        removeResult(count);
        
        return count;
    }
}