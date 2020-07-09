pragma solidity>=0.4.24 <0.6.11;

contract ShaTest{
    bytes _data = "Hello, ShaTest";
    
    function getSha256(bytes memory _memory) public returns(bytes32 result)
    {
        return sha256(_memory);
    }

    function getKeccak256(bytes memory _memory) public returns(bytes32 result)
    {
        return keccak256(_memory);
    }

    function getData() public view returns(bytes memory)
    {
        return _data;
    }
}
