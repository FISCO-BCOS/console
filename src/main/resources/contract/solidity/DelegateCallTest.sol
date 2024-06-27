pragma solidity >=0.6.10 <=0.8.26;

contract DelegateCallDest {
    int public value = 0;
    address public sender;
    address public myAddress;

    constructor() public {
        myAddress = address(this);
    }

    function add() public returns(bytes memory) {
        sender = msg.sender;
        value += 2;
        return "2";
    }
}

contract DelegateCallTest {
    int public value = 0;
    address public sender;
    address public myAddress;
    uint myCodeSize;
    bytes32 myCodeHash;
    address public delegateDest;


    constructor() public {
        myAddress = address(this);
        delegateDest = address(new DelegateCallDest());
    }

    function add() public returns(bytes memory) {
        sender = msg.sender;
        value += 1;
        return "1";
    }

    function testFailed() public returns(bytes memory){
        address addr = address(0x1001);
        return dCall(addr, "add()");
    }

    function testSuccess() public returns(bytes memory){
        return dCall(delegateDest, "add()");
    }

    function dCall(address addr, string memory func) private returns(bytes memory) {
        bool success;
        bytes memory ret;
        (success, ret) = addr.delegatecall(abi.encodeWithSignature(func));
        require(success, "delegatecall failed");

        return ret;
    }

    function codesizeAt(address addr) public returns(uint){
        uint len;
        assembly { len := extcodesize(addr) }
        return len;
    }

    function codehashAt(address addr) public returns(bytes32){
        bytes32 codeHash;
        assembly { codeHash := extcodehash(addr) }
        return codeHash;
    }

}