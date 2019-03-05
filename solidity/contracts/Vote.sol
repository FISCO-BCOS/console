pragma solidity ^0.4.0;

// 投票基础类
contract Vote {
    bytes32[20] public bytes32Arr;
    int[12] public intArr;

    function  Vote(bytes32[] bytesArrParam, int[] intArrParam) public {
        for (uint i = 0; i < 20; i++) {
            bytes32Arr[i] = bytesArrParam[i];
        }
        for (uint j = 0; j < 12; j++) {
            intArr[j] = intArrParam[j];
        }
    }

     function getAllFields() public constant returns (bytes32[20], int[12]) {
        bytes32[20] memory allBytes;
        for (uint i = 0; i < 20; i++) {
            allBytes[i] = bytes32Arr[i];
        }

        int[12] memory allInts;
        for (uint j = 0; j < 12; j++) {
            allInts[j] = intArr[j];
        }
        return (allBytes, allInts);
    }   

    // 业务相关
    function voteId() public returns(bytes32) {
        return bytes32Arr[0];
    }

    function voteWeight() public returns(uint) {
        return uint(intArr[0]);
    }

    function choice() public returns(int) {
        return intArr[1];
    }
}