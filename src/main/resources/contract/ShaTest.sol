// SPDX-License-Identifier: Apache-2.0
pragma solidity>=0.4.24 <0.6.11;

pragma experimental ABIEncoderV2;

import "./Crypto.sol";

contract ShaTest{
    bytes _data = "Hello, ShaTest";
    Crypto crypto;

    constructor() public {
        crypto = Crypto(0x100a);
    }

    function getSha256(bytes memory _memory) public returns(bytes32 result)
    {
        return sha256(_memory);
    }

    function getKeccak256(bytes memory _memory) public returns(bytes32 result)
    {
        return keccak256(_memory);
    }

    function calculateSM3(bytes memory _memory) public returns(bytes32 result)
    {
        return crypto.sm3(_memory);
    }

    function calculateKeccak256(bytes memory _memory) public returns(bytes32 result)
    {
        return crypto.keccak256Hash(_memory);
    }

    function getData() public view returns(bytes memory)
    {
        return _data;
    }
}
