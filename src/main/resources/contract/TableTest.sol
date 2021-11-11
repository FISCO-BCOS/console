pragma solidity ^0.6.0;
pragma experimental ABIEncoderV2;

import "./Table.sol";

contract TableTest {

    Table table;
    constructor () public{
        table = Table(0x1001);
        table.createTable("t_test", "id","name,age");
    }
    function select(string memory id) public view returns (string memory,string memory){
        CompareTriple memory compareTriple1 = CompareTriple("id",id,Comparator.EQ);
        CompareTriple[] memory compareFields = new CompareTriple[](1);
        compareFields[0] = compareTriple1;

        Condition memory condition;
        condition.condFields = compareFields;

        Entry[] memory entries = table.select("t_test", condition);
        string memory name;
        string memory age;
        if(entries.length > 0){
            name = entries[0].fields[0].value;
            age = entries[0].fields[1].value;
        }
        return (name,age);
    }

    function insert(string memory id,string memory name,string memory age) public returns (int256){
        KVField memory kv0 = KVField("id",id);
        KVField memory kv1 = KVField("name",name);
        KVField memory kv2 = KVField("age",age);
        KVField[] memory KVFields = new KVField[](3);
        KVFields[0] = kv0;
        KVFields[1] = kv1;
        KVFields[2] = kv2;
        Entry memory entry1 = Entry(KVFields);
        int256 result1 = table.insert("t_test",entry1);
        return result1;
    }

    function update(string memory id,string memory name,string memory age) public returns (int256){
        KVField memory kv1 = KVField("name",name);
        KVField memory kv2 = KVField("age",age);
        KVField[] memory KVFields = new KVField[](2);
        KVFields[0] = kv1;
        KVFields[1] = kv2;
        Entry memory entry1 = Entry(KVFields);

        CompareTriple memory compareTriple1 = CompareTriple("id",id,Comparator.EQ);
        CompareTriple[] memory compareFields = new CompareTriple[](1);
        compareFields[0] = compareTriple1;

        Condition memory condition;
        condition.condFields = compareFields;
        int256 result1 = table.update("t_test",entry1, condition);

        return result1;
    }

    function remove(string memory id) public returns(int256){
        CompareTriple memory compareTriple1 = CompareTriple("id",id,Comparator.EQ);
        CompareTriple[] memory compareFields = new CompareTriple[](1);
        compareFields[0] = compareTriple1;

        Condition memory condition;
        condition.condFields = compareFields;
        int256 result1 = table.remove("t_test", condition);

        return result1;
    }

    function createTable(string memory tableName,string memory key,string memory fields) public returns(int256){
        int256 result = table.createTable(tableName,key,fields);
        return result;
    }

    function desc(string memory tableName) public returns(string memory, string memory){
        return table.desc(tableName);
    }
}