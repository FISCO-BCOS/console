pragma solidity ^0.4.0;

// 动支信息
contract Payment {
    bytes32[40] public bytes32Arr;
    int[12] public intArr;

    function  Payment(bytes32[] bytesArrParam, int[] intArrParam) public {
        for (uint i = 0; i < 40; i++) {
            bytes32Arr[i] = bytesArrParam[i];
        }
        for (uint j = 0; j < 12; j++) {
            intArr[j] = intArrParam[j];
        }
    }

     function getAllFields() public constant returns (bytes32[40], int[12]) {
        bytes32[40] memory allBytes;
        for (uint i = 0; i < 40; i++) {
            allBytes[i] = bytes32Arr[i];
        }

        int[12] memory allInts;
        for (uint j = 0; j < 12; j++) {
            allInts[j] = intArr[j];
        }
        return (allBytes, allInts);
    }   

    
    function paymentId() public returns(bytes32) {
        return bytes32Arr[0];
    }

    //finance字段
    function requestAmount() public returns(int) {
        return intArr[0];
    }
}