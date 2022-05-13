// SPDX-License-Identifier: Apache-2.0
pragma solidity >=0.6.10 <0.8.20;
pragma experimental ABIEncoderV2;

import "./Table.sol";

contract TableTest {
    event CreateResult(int256 count);
    event InsertResult(int256 count);
    event UpdateResult(int256 count);
    event RemoveResult(int256 count);

    TableManager constant tm =  TableManager(address(0x1002));
    Table table;
    string constant TABLE_NAME = "t_test";
    constructor () public{
        // create table
        string[] memory columnNames = new string[](2);
        columnNames[0] = "name";
        columnNames[1] = "age";
        TableInfo memory tf = TableInfo("id", columnNames);
        int32 result = tm.createTable(TABLE_NAME, tf);
        require(result == 0, "create table failed");

        address t_address = tm.openTable(TABLE_NAME);
        table = Table(t_address);
    }

    function select(string memory id) public view returns (string memory,string memory)
    {
        Entry memory entry = table.select(id);

        string memory name;
        string memory age;
        if(entry.fields.length==2){
            name = entry.fields[0];
            age = entry.fields[1];
        }
        return (name,age);
    }

    function insert(string memory id,string memory name,string memory age) public returns (int32){
        string[] memory columns = new string[](2);
        columns[0] = name;
        columns[1] = age;
        Entry memory entry = Entry(id, columns);
        int32 result = table.insert(entry);
        emit InsertResult(result);
        return result;
    }

    function update(string memory id, string memory name, string memory age) public returns (int32){
        UpdateField[] memory updateFields = new UpdateField[](2);
        updateFields[0] = UpdateField(0, name);
        updateFields[1] = UpdateField(1, age);

        int32 result = table.update(id, updateFields);
        emit UpdateResult(result);
        return result;
    }

    function remove(string memory id) public returns(int32){
        int32 result = table.remove(id);
        emit RemoveResult(result);
        return result;
    }

    function createTable(string memory tableName,string memory key,string[] memory fields) public returns(int256){
        TableInfo memory tf = TableInfo(key, fields);
        int32 result = tm.createTable(tableName,tf);
        emit CreateResult(result);
        return result;
    }

    function desc() public view returns(string memory, string[] memory){
        TableInfo memory ti = table.desc();
        return (ti.keyColumn,ti.valueColumns);
    }
}