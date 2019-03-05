pragma solidity ^0.4.0;

import "./ApplyData.sol";

//动支申请合约入口，行为合约
contract Apply {
    //apply数据集合地址
    address applysDataAddr;

    // 返回值Event
    event transRetLog(string oper,int256 retCode);

    // 构造函数
  //  function Apply(address dataAddr) {
  //      applysDataAddr = dataAddr;
  //  }

    function newApply(bytes32[] bytesArgs, int[] intArgs, bytes32[] financeArgs) public returns (int code){
        address oldApply = ApplyData(applysDataAddr).getApply(bytesArgs[1]);
        if (0x0 != oldApply) {
            transRetLog("newApply duplicated", 2);
            return 2;
        }

        bytes32[] memory bytesInputs = new bytes32[](40);
        int[] memory intInputs = new int[](12);
        bytes32[] memory financeInputs = new bytes32[](64);
        for (uint i = 0; i < 40; i++) {
            bytesInputs[i] = bytesArgs[i];
        }

        for (i = 0; i < 12; i++) {
            intInputs[i] = intArgs[i];
        }

        for (i = 0; i < 64; i++) {
            financeInputs[i] = financeArgs[i];
        }

        address applyAddr = new FundApply(bytesInputs, intInputs, financeInputs);
        ApplyData(applysDataAddr).addApply(bytesArgs[1], applyAddr);
        transRetLog("newApply", 0);

        return 0;
    }

    // 新增一个投票
    function addVote(bytes32 applyId, bytes32[] bytesArgs, int[] intArgs) public returns (int code) {
        address curApply = ApplyData(applysDataAddr).getApply(applyId);
        if (curApply == 0x0) {
            transRetLog("Vote's fund apply not exist", 9);
            return 9;
        }

        address oldVote = FundApply(curApply).getVote(bytesArgs[0]);
        if (0x0 != oldVote) {
            transRetLog("Vote duplicateed", 2);
            return 2;
        }

        bytes32[] memory bytesInputs = new bytes32[](20);
        int[] memory intInputs = new int[](12);
        for (uint i = 0; i < 20; i++) {
            bytesInputs[i] = bytesArgs[i];
        }

        for (uint j = 0; j < 12; j++) {
            intInputs[j] = intArgs[j];
        }

        address newVote = new Vote(bytesInputs, intArgs);
        FundApply(curApply).addVote(bytesArgs[0], newVote, bytesArgs[1]);
        transRetLog("AddVote succeed", 0);

        return 0;
    }

    // 新增一个动支请求
    function addPayment(bytes32 applyId, bytes32[] bytesArgs, int[] intArgs) public returns (int code) {
        address curApply = ApplyData(applysDataAddr).getApply(applyId);
        if (curApply == 0x0) {
            transRetLog("Payment's fund apply not exist", 9);
            return 9;
        }

        address oldPayment = FundApply(curApply).getPayment(bytesArgs[0]);
        if (0x0 != oldPayment) {
            transRetLog("Payment duplicateed", 2);
            return 2;
        }

        bytes32[] memory bytesInputs = new bytes32[](40);
        int[] memory intInputs = new int[](12);
        for (uint i = 0; i < 40; i++) {
            bytesInputs[i] = bytesArgs[i];
        }

        for (uint j = 0; j < 12; j++) {
            intInputs[j] = intArgs[j];
        }

        address newVote = new Payment(bytesInputs, intArgs);
        FundApply(curApply).addPayment(bytesArgs[0], newVote);
        transRetLog("addPayment succeed", 0);

        return 0;
    }

    // 查询请款申请基本信息
    function queryApplyBaseInfo(bytes32 communityId, bytes32 applyId) constant returns (int , bytes32[] , int[] ,bytes32[] financeDataStr,bytes32[] resultDataStr){
        bytes32[40] memory temp1;
        int[12] memory temp2;
        bytes32[64] memory temp3;
        bytes32[20] memory temp4;

        int[] memory exDataInt;
        bytes32[] memory exDataStr;
        exDataInt = new int[](12);
        exDataStr = new bytes32[](40);
        financeDataStr = new bytes32[](64);
        resultDataStr = new bytes32[](20);

        if (0x0 == ApplyData(applysDataAddr).getApply(applyId)){
            return (9,exDataStr, exDataInt, financeDataStr, resultDataStr);
        }

        FundApply curApply = FundApply(ApplyData(applysDataAddr).getApply(applyId));

        (temp1, temp2, temp3, temp4) = curApply.getAllFields();
        for(uint i=0;i<40;i++) {
            exDataStr[i] = temp1[i];
        }
        for(i=0;i<12;i++) {
            exDataInt[i] = temp2[i];
        }
        for(i=0;i<64;i++) {
            financeDataStr[i] = temp3[i];
        }
        for(i=0;i<20;i++) {
            resultDataStr[i] = temp4[i];
        }

        return (0, exDataStr, exDataInt, financeDataStr, resultDataStr);
    }

    // 查询投票列表，每页最多20条。返回的地址列表
    function queryVoteList(bytes32 applyId, uint256 pageNum) constant returns (int code, address[] list){
        address applyAddr = FundApply(ApplyData(applysDataAddr).getApply(applyId));
        if (0x0 == applyAddr){
            return (9, list);
        }

        FundApply curApply = FundApply(applyAddr);
        uint256 totalNum = curApply.getVoteNum();
        // 如果页数超过范围，返回空列表
        if (totalNum == 0 || (pageNum*20 >= totalNum)){
            return (11, list);
        }

        uint256 beginNum = pageNum * 20;
        uint256 endNum = (pageNum + 1) * 20;
        if (endNum > totalNum){
            endNum = totalNum;
        }
        list = new address[](endNum - beginNum);
        for (uint256 i = beginNum; i < endNum; ++i){
            list[i-beginNum] = curApply.getVoteByIndex(i);
        }

        return (0, list);
    }

    function getVoteByAddr(address voteAddr) public constant returns (bytes32[],int[]){
        bytes32[20] memory temp1;
        int[12] memory temp2;
        int[] memory exDataArrRate;
        bytes32[] memory exDataStrRate;
        exDataArrRate = new int[](12);
        exDataStrRate = new bytes32[](20);
        (temp1, temp2) = Vote(voteAddr).getAllFields();
        for(uint i = 0; i < 20; i++) {
            exDataStrRate[i] = temp1[i];
        }
        for(i = 0;i < 12; i++) {
            exDataArrRate[i] = temp2[i];
        }
        return  (exDataStrRate,exDataArrRate);
    }

    // 查询Payment列表，每页最多20条。返回的地址列表
    function queryPaymentList(bytes32 applyId, uint256 pageNum) constant returns (int code, address[] list){
        address applyAddr = FundApply(ApplyData(applysDataAddr).getApply(applyId));
        if (0x0 == applyAddr){
            return (9, list);
        }

        FundApply curApply = FundApply(applyAddr);
        uint256 totalNum = curApply.getPaymentNum();
        // 如果页数超过范围，返回空列表
        if (totalNum == 0 || (pageNum*20 >= totalNum)){
             return (11, list);
        }

        uint256 beginNum = pageNum * 20;
        uint256 endNum = (pageNum + 1) * 20;
        if (endNum > totalNum){
            endNum = totalNum;
        }
        list = new address[](endNum - beginNum);
        for (uint256 i = beginNum; i < endNum; ++i){
            list[i - beginNum] = curApply.getPaymentByIndex(i);
        }

        return (0, list);
    }

    function getPaymentByAddr(address voteAddr) public constant returns (bytes32[],int[]){
        bytes32[40] memory temp1;
        int[12] memory temp2;
        int[] memory exDataArrRate;
        bytes32[] memory exDataStrRate;
        exDataArrRate = new int[](12);
        exDataStrRate = new bytes32[](40);
        (temp1, temp2) = Payment(voteAddr).getAllFields();
        for(uint i = 0; i < 40; i++) {
            exDataStrRate[i] = temp1[i];
        }
        for(i = 0;i < 12; i++) {
            exDataArrRate[i] = temp2[i];
        }
        return  (exDataStrRate,exDataArrRate);
    }

    // 计算投票结果(内部调用),返回是否通过和3个通过率
    function calcResultInner(bytes32 applyId) private returns (bool, uint[]) {
        // passRate,weightRate,voteRate
        uint[] memory rate = new uint[](3);
        rate[0] = 0;
        rate[1] = 0;
        rate[2] = 0;

        address applyAddr = FundApply(ApplyData(applysDataAddr).getApply(applyId));
        if (0x0 == applyAddr){
            return (false, rate);
        }

        FundApply curApply = FundApply(applyAddr);

        //是否满足投票通过最低比例
        uint resultPassNum = curApply.getResultPassNum();
        uint weightPassNum = curApply.getWeightPassNum();
        uint voteRateNum = curApply.getVoteRateNum();

        //投票结果通过率
        bool resultRatePass = true;
        if (curApply.totalVoterNum() > 0){
             rate[0] = 10000 * resultPassNum / curApply.totalVoterNum();
        }

        if (curApply.passRate() > 0 && curApply.totalVoterNum() > 0){//只有通过比例大于0时才需要计算
            resultRatePass = false;
            if (10000 * resultPassNum >= curApply.passRate() * curApply.totalVoterNum()) {
                resultRatePass = true;
            }
        }

        //比重通过率
        bool weightPass = true;
        if (curApply.totalWeight() > 0){
            rate[1] = 10000 * weightPassNum / curApply.totalWeight();
        }
        if (curApply.weightPassRate() > 0 && curApply.totalWeight() > 0){//只有通过比例大于0和总权重大于0时才需要计算
            weightPass = false;
            if (10000 * weightPassNum >= curApply.weightPassRate() * curApply.totalWeight()) {
                weightPass = true;
            }
        }

        // 投票率
        bool voteNumPass = true;
        if (curApply.totalVoterNum() > 0){
            rate[2] = 10000 * voteRateNum / curApply.totalVoterNum();//计算万分比没有浮点，可能不准
        }
        if (curApply.voteRate() > 0 && curApply.totalVoterNum() > 0) {//只有通过比例大于0时才需要计算
            voteNumPass = false;
            if (10000 * voteRateNum >= curApply.voteRate() * curApply.totalVoterNum()){//这里直接比较值大小，避免精度损失
                voteNumPass = true;
            }
        }

        // 3个都通过，就算通过
        if (weightPass && resultRatePass && voteNumPass){
            return (true, rate);
        }

        return (false, rate);
    }

    // 请求计算投票结果
    function calcRequestResult(bytes32 applyId, bytes32[] bytesArgs) public returns (int code) {
        address curApply = ApplyData(applysDataAddr).getApply(applyId);
        if (curApply == 0x0) {
            transRetLog("calcVoteResult's fund apply not exist", 9);
            return 9;
        }

        FundApply apply = FundApply(curApply);
        // 判断是否已经过了截止时间
//        uint256 curTime = block.timestamp/1000;//转换为秒，原始单位为毫秒
//        uint256 realEndTime = uint(stringToUint(bytes32ToString(apply.endTime()))) + 120;//由于合约时间不准确，延时120S
//        if (curTime < realEndTime){
//            transRetLog("calcVoteResult's not reach endTime", 10);
//            return 10;
//        }

        // 增加结果基础字段
        bytes32[] memory bytesInputs = new bytes32[](20);
        for (uint i = 0; i < 20; i++) {
            bytesInputs[i] = bytesArgs[i];
        }

        // 计算结果
        bool calcRet = false;
        uint[] memory rateArr = new uint[](3);
        (calcRet, rateArr) = calcResultInner(applyId);

        //写入结果, 1表示通过，2表示未通过。1和2的返回值这里要特殊处理
        bytes32 strResult = "0";
        if (calcRet){
            strResult = "1";
            transRetLog("calc passed", 1);
        } else {
            strResult = "2";
            transRetLog("calc not passed", 2);
        }
        bytesInputs[11] = strResult;//第7个字段表示结果，参考SDK接口
        bytesInputs[13] = uintToBytes32(rateArr[0]);// 写入3个通过率
        bytesInputs[14] = uintToBytes32(rateArr[1]);
        bytesInputs[15] = uintToBytes32(rateArr[2]);

        // 写入数据
        FundApply(curApply).addResultInfo(bytesInputs);

        return 0;
    }

    //字符串转整数
    function stringToUint(string s) constant returns (uint result) {
        bytes memory b = bytes(s);
        uint i;
        result = 0;
        for (i = 0; i < b.length; i++) {
            uint c = uint(b[i]);
            if (c >= 48 && c <= 57) {
                result = result * 10 + (c - 48);
            }
        }

        return result;
    }

    // 证书转bytes32
    function uintToBytes32(uint v) constant returns (bytes32) {
        uint maxlength = 100;
        bytes memory reversed = new bytes(maxlength);

        if (0 == v){//如果值为0，需要返回“0”
            reversed[0] = byte(48);
        }

        uint i = 0;
        while (v != 0) {
            uint remainder = v % 10;
            v = v / 10;
            reversed[i++] = byte(48 + remainder);
        }

        bytes memory s = new bytes(i + 1);
        for (uint j = 0; j <= i; j++) {
            s[j] = reversed[i - j];
        }
        string memory str = string(s);

        return stringToBytes32(str);
    }

    function stringToBytes32(string memory source) returns (bytes32 result) {
        bytes memory tempEmptyStringTest = bytes(source);
        if (tempEmptyStringTest.length == 0) {
            return 0x0;
        }

        assembly {
            result := mload(add(source, 32))
        }
    }

    function bytes32ToString(bytes32 x) constant returns (string) {
        bytes memory bytesString = new bytes(32);
        uint charCount = 0;
        for (uint j = 0; j < 32; j++) {
            byte char = byte(bytes32(uint(x) * 2 ** (8 * j)));
            if (char != 0) {
                bytesString[charCount] = char;
                charCount++;
            }
        }
        bytes memory bytesStringTrimmed = new bytes(charCount);
        for (j = 0; j < charCount; j++) {
            bytesStringTrimmed[j] = bytesString[j];
        }
        return string(bytesStringTrimmed);
    }
}