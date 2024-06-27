// SPDX-License-Identifier: UNLICENSED
pragma solidity >=0.6.10 <=0.8.26;

import "./StorageSlot.sol";
import "./ContractB.sol";

contract ContractA {
    using StorageSlot for *;

    StorageSlot.Int256SlotType private intSlot;
    constructor(int256 value){
        StorageSlot.tstore(intSlot, value);
    }

    function getData() public view returns (int256) {
        return StorageSlot.tload(intSlot);
    }

    function callContractB() public returns (int256){
        ContractB b = new ContractB();
        return b.callContractA(address(this));
    }

}
