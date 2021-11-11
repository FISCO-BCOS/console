pragma solidity ^0.6.0;
pragma experimental ABIEncoderV2;

    struct KVField {
        string key;
        string value;
    }

    struct Entry {
        KVField[] fields;
    }

    enum Comparator {EQ, NE, GT, GE, LT, LE}
    struct CompareTriple {
        string lvalue;
        string rvalue;
        Comparator cmp;
    }

    struct Condition {
        CompareTriple[] condFields;
    }
contract Table {
    function createTable(string memory tableName, string memory key, string memory valueFields) public returns (int256){}
    function select(string memory tableName, Condition memory condition) public view returns (Entry[] memory){}
    function insert(string memory tableName, Entry memory entry) public returns (int256){}
    function update(string memory tableName, Entry memory entry, Condition memory condition) public returns (int256){}
    function remove(string memory tableName, Condition memory condition) public returns (int256){}
    function desc(string memory tableName) public returns(string memory,string memory){}
}
contract KVTable {
    function createTable(string memory tableName, string memory key, string memory valueFields) public returns (int256){}
    function get(string memory tableName, string memory key) public view returns (bool, Entry memory entry){}
    function set(string memory tableName,string memory key, Entry memory entry) public returns (int256){}
}
