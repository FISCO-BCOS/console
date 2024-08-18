// SPDX-License-Identifier: Apache-2.0
pragma solidity >=0.6.10 <=0.8.26;

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

contract EchoEvent {

    event Echo1(uint256 indexed u);
    event Echo2(int256 indexed i);
    event Echo3(string indexed s);
    event Echo4(uint256 indexed u, int256 indexed i, string indexed s);

    function echo(uint256 u, int256 i, string memory s) public returns(uint256, int256, string memory) {

        emit Echo1(u);
        emit Echo2(i);
        emit Echo3(s);
        emit Echo4(u, i ,s);

        return (u, i , s);
    }

    event Echo5(bytes32 indexed bsn);
    event Echo6(bytes indexed bs);
    event Echo7(bytes32 indexed bsn, bytes indexed bs);

    function echo(bytes32 bsn, bytes memory bs) public returns(bytes32, bytes memory) {

        emit Echo5(bsn);
        emit Echo6(bs);
        emit Echo7(bsn, bs);

        return (bsn, bs);
    }
}
