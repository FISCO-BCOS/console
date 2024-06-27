// SPDX-License-Identifier: UNLICENSED
pragma solidity >=0.6.10 <=0.8.26;

import "./StorageSlot.sol";
import "./ContractA.sol";
import "./ContractB.sol";

contract MainContract {

    function checkAndVerifyIntValue(int256 value) public returns (bool) {
        ContractA a = new ContractA(value);
        int256 result = a.callContractB();
        require(result == value, "store value not equal tload result");
        return true;
    }
}