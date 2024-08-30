// SPDX-License-Identifier: Apache-2.0
pragma solidity ^0.5.2;


pragma experimental ABIEncoderV2;

contract Cast {
    function stringToS256(string memory) public view returns (int256);

    function stringToS64(string memory) public view returns (int64);

    function stringToU256(string memory) public view returns (uint256);

    function stringToAddr(string memory) public view returns (address);

    function stringToBytes32(string memory) public view returns (bytes32);
    
    function s256ToString(int256) public view returns (string memory);
    function s64ToString(int64) public view returns (string memory);
    function u256ToString(uint256) public view returns (string memory);
    function addrToString(address) public view returns (string memory);
    function bytes32ToString(bytes32) public view returns (string memory);
}