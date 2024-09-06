// SPDX-License-Identifier: Apache-2.0
pragma solidity ^0.5.2;

import "./BaseEvent.sol";

contract EchoEvent {

    event Echo(uint256 indexed u);
    event Echo(int256 indexed i);
    event Echo(string indexed s);
    event Echo(uint256 indexed u, int256 indexed i, string indexed s);

    function echo(uint256 u, int256 i, string memory s) public returns(uint256, int256, string memory) {

        emit Echo(u);
        emit Echo(i);
        emit Echo(s);
        emit Echo(u, i ,s);

        return (u, i , s);
    }

    event Echo(bytes32 indexed bsn);
    event Echo(bytes indexed bs);
    event Echo(bytes32 indexed bsn, bytes indexed bs);

    function echo(bytes32 bsn, bytes memory bs) public returns(bytes32, bytes memory) {

        emit Echo(bsn);
        emit Echo(bs);
        emit Echo(bsn, bs);

        return (bsn, bs);
    }
}
