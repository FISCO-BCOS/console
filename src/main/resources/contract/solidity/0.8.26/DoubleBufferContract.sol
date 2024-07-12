contract DoubleBufferContract {
    uint[] bufferA;
    uint[] bufferB;

    modifier nonreentrant(bytes32 key) {
        assembly {
            if tload(key) {revert(0, 0)}
            tstore(key, 1)
        }
        _;
        assembly {
            tstore(key, 0)
        }
    }

    bytes32 constant A_LOCK = keccak256("a");
    bytes32 constant B_LOCK = keccak256("b");

    function pushA() nonreentrant(A_LOCK) public payable {
        bufferA.push(msg.value);
    }

    function popA() nonreentrant(A_LOCK) public {
        require(bufferA.length > 0);

        (bool success,) = msg.sender.call{value: bufferA[bufferA.length - 1]}("");
        require(success);
        bufferA.pop();
    }

    function pushB() nonreentrant(B_LOCK) public payable {
        bufferB.push(msg.value);
    }

    function popB() nonreentrant(B_LOCK) public {
        require(bufferB.length > 0);

        (bool success,) = msg.sender.call{value: bufferB[bufferB.length - 1]}("");
        require(success);
        bufferB.pop();
    }

    function getBufferA() public view returns (uint256[] memory) {
        return bufferA;
    }

    function getBufferB() public view returns (uint256[] memory) {
        return bufferB;
    }
}