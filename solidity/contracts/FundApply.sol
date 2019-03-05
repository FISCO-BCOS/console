pragma solidity ^0.4.0;

import "./Vote.sol";
import "./Payment.sol";

//资金动支申请
contract FundApply {
    //基本信息
    bytes32[40] public bytes32Arr;
    int[12] public intArr;
    //finance字段
    bytes32[64] public financeBytesArr;
    //申请结果字段
    bytes32[20] public resultBytesArr;
    //投票信息
    mapping(bytes32 => address) m_voteMap;
    mapping(bytes32 => uint256) m_voterIndexMap;//voterId->>voteList index，用来去重。voterIndex存的下表是从1开始的。
    address[] private m_voteList;
    // 动支信息
    mapping(bytes32 => address) m_paymentMap;
    address[] private m_paymentList;

    // 投票累计数据，为了能迅速计算出投票结果，每次投票都需要先计算出来
    uint resultPassNum;
    uint weightPassNum;
    uint voteRateNum;

    event newVoteEvent(address addr);
    event voteNumerEvent(uint32 num);

    function FundApply(bytes32[] bytesArrParam, int[] intArrParam, bytes32[] financeAttParam){
        for (uint i = 0; i < 40; i++) {
            bytes32Arr[i] = bytesArrParam[i];
        }
        for (uint j = 0; j < 12; j++) {
            intArr[j] = intArrParam[j];
        }
        for (uint k = 0; k < 64; k++) {
            financeBytesArr[k] = financeAttParam[k];
        }

        resultPassNum = 0;
        weightPassNum = 0;
        voteRateNum = 0;
    }

    function addResultInfo(bytes32[] resultParas){
        for (uint i = 0; i < 20; i++) {
            resultBytesArr[i] = resultParas[i];
        }
    }

    function getResultPassNum() public returns(uint){
        return resultPassNum;
    }

    function getWeightPassNum() public returns(uint){
        return weightPassNum;
    }

    function getVoteRateNum() public returns(uint){
        return voteRateNum;
    }

    function communityId() public returns(bytes32) {
        return bytes32Arr[0];
    }

    function applyId() public returns(bytes32) {
        return bytes32Arr[1];
    }

    // 总投票人数
    function totalVoterNum() public returns(uint) {
        return uint(intArr[0]);
    }

    function passRate() public returns(uint) {
        return uint(intArr[1]);
    }

    function totalWeight() public returns(uint) {
        return uint(intArr[2]);
    }

    function weightPassRate() public returns(uint) {
        return uint(intArr[3]);
    }

    function voteRate() public returns(uint) {
        return uint(intArr[4]);
    }

    function endTime() public returns(bytes32) {
        return bytes32Arr[9];
    }

    // financeList列表怎么处理，后面64这个字段都是，合约里不用读出来，放着就行了

    // 新增一个投票 
    function addVote(bytes32 voteId, address voteAddr, bytes32 voterId) public returns(int) {
        address oldVoteAddr = m_voteMap[voteId];
        if (0x0 != oldVoteAddr) {
            return 0;
        } else {
            m_voteMap[voteId]= voteAddr;

            Vote newVote = Vote(voteAddr);
            uint256 voterIndex = m_voterIndexMap[voterId];
            if (voterIndex == 0){//没有找到对应的用户投票,新增一条
                m_voteList.push(voteAddr);
                m_voterIndexMap[voterId] = m_voteList.length;//如果是List里面的第1个用户，voterId对应的voterIndex是1
            } else {//找到对应的用户投票,覆盖原来的记录
                Vote oldVote = Vote(m_voteList[voterIndex-1]);
                m_voteList[voterIndex-1] = voteAddr;//对应的List下标要减1

                // 回滚老的投票引起的变化
                voteRateNum -= 1;
                //如果老的投票是不通过，对resultPassNum/weightPassNum无影响
                if (oldVote.choice() == 0) {//0表示通过
                    resultPassNum -= 1;
                    weightPassNum = weightPassNum - oldVote.voteWeight();
                }
            }

            // 新的这一票引起的变化
            if (newVote.choice() == 0) {//0表示通过
                resultPassNum += 1;
                weightPassNum = weightPassNum + newVote.voteWeight();
            }
            voteRateNum += 1;
        }
        return 1;
    }

    function getVoteNum() public constant returns(uint256) {
        return m_voteList.length;
    }

    // 获取投票
    function getVote(bytes32 voteId) public returns(address) {
        return m_voteMap[voteId];
    }

    // 获取投票
    function getVoteByIndex(uint256 index) public returns(address) {
        return m_voteList[index];
    }

    // 新增一个动支
    function addPayment(bytes32 paymentId, address paymentAddr) public returns(int) {
        address oldPayment = m_paymentMap[paymentId];
        if (0x0 != oldPayment) {
            return 0;
        } else {
            m_paymentMap[paymentId]= paymentAddr;
            m_paymentList.push(paymentAddr);
        }
        return 1;
    }

    function getPaymentNum() public constant returns(uint256) {
        return m_paymentList.length;
    }

     // 获取动支信息
    function getPayment(bytes32 paymentId) public returns(address) {
        return m_paymentMap[paymentId];
    }

    // 获取动支信息
    function getPaymentByIndex(uint256 index) public returns(address) {
        return m_paymentList[index];
    }

    function getAllFields() public constant returns (bytes32[40], int[12], bytes32[64], bytes32[20]) {
        bytes32[40] memory baseBytes;
        for (uint i = 0; i < 40; i++) {
            baseBytes[i] = bytes32Arr[i];
        }

        int[12] memory allInts;
        for (uint j = 0; j < 12; j++) {
            allInts[j] = intArr[j];
        }

        bytes32[64] memory financeBytes;
        for (i = 0; i < 64; i++) {
            financeBytes[i] = financeBytesArr[i];
        }

        bytes32[20] memory resultBytes;
        for (i = 0; i < 20; i++) {
            resultBytes[i] = resultBytesArr[i];
        }

        return (baseBytes, allInts, financeBytes, resultBytes);
    }
}