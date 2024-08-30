// SPDX-License-Identifier: Apache-2.0
pragma solidity ^0.5.2;

contract BaseEvent {

    //---------------------------------------------------------------------------------------------------------------
    event Transfer(string indexed from_account, string indexed to_account, uint256 indexed amount);
    event TransferAccount(string indexed from_account,string indexed to_account);
    event TransferAmount(uint256 indexed amount);

    function transfer(string memory from_account, string memory to_account, uint256 amount) public  {

        emit Transfer(from_account, to_account, amount);

        emit TransferAccount(from_account, to_account);

        emit TransferAmount(amount);

    }
}
