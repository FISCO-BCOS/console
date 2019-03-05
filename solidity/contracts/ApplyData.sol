pragma solidity ^0.4.0;

import "./FundApply.sol";

//资金动支申请列表
contract ApplyData {
    event transRetLog(string oper,int256 status);
    mapping(bytes32 => address) applyMap;
    address[] applyList;

    function addApply(bytes32 applyId, address applyAddr) public returns(int) {  
        address oldApply = applyMap[applyId];
        if( 0x0 != oldApply ) {
            return 0;
        } else {
            applyMap[applyId]= applyAddr;
            applyList.push(applyAddr);
        }
        return 1;
    }

    function getApply(bytes32 applyId) returns(address) {
        return applyMap[applyId];
    }
    
    function getApplyListLength() returns(uint) {
        return applyList.length;
    }

    function getApplyByIndex(uint index) returns (address) {
        return applyList[index];
    }
}