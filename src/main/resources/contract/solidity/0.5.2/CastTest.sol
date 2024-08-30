// SPDX-License-Identifier: Apache-2.0
pragma solidity ^0.5.2;

import "./Cast.sol";

contract CastTest {
    Cast constant cast = Cast(address(0x100f));

    function stringToS256(string memory _s) public view returns (int256){
        return cast.stringToS256(_s);
    }

    function stringToS64(string memory _s) public view returns (int64){
        return cast.stringToS64(_s);
    }
    function stringToU256(string memory _s) public view returns (uint256){
        return cast.stringToU256(_s);
    }
    function stringToAddr(string memory _s) public view returns (address){
        return cast.stringToAddr(_s);
    }
    function stringToBytes32(string memory _s) public view returns (bytes32){
        return cast.stringToBytes32(_s);
    }
    function s256ToString(int256 _i) public view returns (string memory){
        return cast.s256ToString(_i);
    }
    function s64ToString(int64 _i) public view returns (string memory){
        return cast.s64ToString(_i);
    }
    function u256ToString(uint256 _u) public view returns (string memory){
        return cast.u256ToString(_u);
    }
    function addrToString(address _a) public view returns (string memory){
        return cast.addrToString(_a);
    }
    function bytes32ToString(bytes32 _b) public view returns (string memory){
        return cast.bytes32ToString(_b);
    }
}
