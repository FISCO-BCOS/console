pragma solidity ^0.6.0;
pragma experimental ABIEncoderV2;

import "./Table.sol";

contract TableTest {
    event CreateResult(int256 count);
    event InsertResult(int256 count);
    event UpdateResult(int256 count);
    event RemoveResult(int256 count);

    Table table;
    string constant TABLE_NAME = "t_test";
    constructor () public{
        table = Table(0x1001);
        table.createTable(TABLE_NAME, "id","name,age");
    }
    function select(string memory id) public view returns (string memory,string memory){
        CompareTriple memory compareTriple1 = CompareTriple("id",id,Comparator.EQ);
        CompareTriple[] memory compareFields = new CompareTriple[](1);
        compareFields[0] = compareTriple1;

        Condition memory condition;
        condition.condFields = compareFields;

        Entry[] memory entries = table.select(TABLE_NAME, condition);
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
        int256 result = table.insert(TABLE_NAME,entry1);
        emit InsertResult(result);
        return result;
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
        int256 result = table.update(TABLE_NAME,entry1, condition);
        emit UpdateResult(result);
        return result;
    }

    function remove(string memory id) public returns(int256){
        CompareTriple memory compareTriple1 = CompareTriple("id",id,Comparator.EQ);
        CompareTriple[] memory compareFields = new CompareTriple[](1);
        compareFields[0] = compareTriple1;

        Condition memory condition;
        condition.condFields = compareFields;
        int256 result = table.remove(TABLE_NAME, condition);
        emit RemoveResult(result);
        return result;
    }

    function createTable(string memory tableName,string memory key,string memory fields) public returns(int256){
        int256 result = table.createTable(tableName,key,fields);
        emit CreateResult(result);
        return result;
    }

    function desc(string memory tableName) public returns(string memory, string memory){
        return table.desc(tableName);
    }
}