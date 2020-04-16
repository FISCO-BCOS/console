pragma solidity ^0.4.24;

contract ShaTest{
    bytes _data = "Hello, ShaTest";
    function ShaTest(){}
    function getSha256(bytes _memory) returns(bytes32 result)
    {   
        return sha256(_memory);
    }   
    
    function getSha3(bytes _memory) returns(bytes32 result)
    {   
        return sha3(_memory);
    }
    
    function getKeccak256(bytes _memory)returns(bytes32 result)
    {
        return keccak256(_memory);
    }

    function getData() constant returns(bytes)
    {
        return _data;
    }
}
