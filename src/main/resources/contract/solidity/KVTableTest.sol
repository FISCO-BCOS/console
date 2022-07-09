// SPDX-License-Identifier: Apache-2.0
pragma solidity >=0.6.10 <0.8.20;
pragma experimental ABIEncoderV2;

import "./Table.sol";

contract KVTableTest {

    TableManager tm;
    KVTable table;
    string constant tableName = "t_kv_test";
    event SetEvent(int256 count);
    constructor () public{
        tm = TableManager(address(0x1002));

        // create table
        tm.createKVTable(tableName, "id", "item_name");

        // get table address
        address t_address = tm.openTable(tableName);
        table = KVTable(t_address);
    }

    function desc() public view returns(string memory, string memory){
        TableInfo memory tf = tm.desc(tableName);
        return (tf.keyColumn, tf.valueColumns[0]);
    }

    function get(string memory id) public view returns (bool, string memory) {
        bool ok = false;
        string memory value;
        (ok, value) = table.get(id);
        return (ok, value);
    }

    function set(string memory id, string memory item_name)
    public
    returns (int32)
    {
        int32 result = table.set(id,item_name);
        emit SetEvent(result);
        return result;
    }

    function createKVTableTest(string memory _tableName,string memory keyName,string memory fieldName) public returns(int32){
        int32 result = tm.createKVTable(_tableName, keyName, fieldName);
        return result;
    }
}