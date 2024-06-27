// SPDX-License-Identifier: UNLICENSED
pragma solidity >=0.6.10 <=0.8.26;

import "./StorageSlot.sol";
import "./ContractA.sol";


contract ContractB {

    function callContractA(address a) public returns (int256){
        ContractA a = ContractA(a);
        int256 result = a.getData();
        return result;
    }

}
