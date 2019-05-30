pragma solidity ^0.4.10;
pragma experimental ABIEncoderV2;

import "./Table.sol";

contract Student {

    // event
    event addStudentEvent(int256 ret);
    
    
    constructor() public {
        createTable();
    }

    function createTable() private {
        TableFactory tf = TableFactory(0x1001); 
        tf.createTable("t2_student", "account", "name,age");
    }

    function openTable() private returns(Table) {
        TableFactory tf = TableFactory(0x1001);
        Table table = tf.openTable("t2_student");
        return table;
    }

    function getStudentByAccount(string account) public constant returns(int256,bytes32,int256) {
        Table table = openTable();
        Entries entries = table.select(account, table.newCondition());
        if (0 == uint256(entries.size())) {
            return (-1,"",0);
        } else {
            Entry entry = entries.get(0);
            return (0,entry.getBytes32("name"),entry.getInt("age"));
        }
    }
    
    function isExist(string account) public constant returns(int256) {
        Table table = openTable();
        Entries entries = table.select(account, table.newCondition());
        if (0 == uint256(entries.size())) {
            return 0; 
        } else {
            return -1;
        }
    }
    

    function addStudent(string _account, string _name) public returns(int256){
        int256 ret_code = 0;
        int256 ret= 2;
        ret = isExist(_account);
        
        if(ret == 0) {
            Table table = openTable();
            
            Entry entry = table.newEntry();
            entry.set("account", _account);
            entry.set("name", _name);
            //entry.set("age", parmList[2]);
            
            int count = table.insert(_account, entry);
            
            if (count == 1) {
                ret_code = 0;
            } else {
                ret_code = -2;
            }
        } else {
            ret_code = -1;
        }
        
        emit addStudentEvent(ret_code);
        return ret_code;
    }
    
   
}
