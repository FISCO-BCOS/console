pragma solidity ^0.8.0;

import "./DoubleBufferContract.sol";

contract TestDoubleBufferContract {
    DoubleBufferContract public doubleBufferContract;

    constructor() {
        doubleBufferContract = new DoubleBufferContract();
    }

    function testReentrancyA() public {
        doubleBufferContract.pushA{value: 1 ether}();
        // 尝试再次调用pushA函数,应该被阻止
        doubleBufferContract.pushA{value: 1 ether}();
    }

    function testReentrancyB() public {
        doubleBufferContract.pushB{value: 1 ether}();
        // 尝试再次调用pushB函数,应该被阻止
        doubleBufferContract.pushB{value: 1 ether}();
    }

    receive() external payable {
        doubleBufferContract.pushA{value: 1 ether}();
    }
}