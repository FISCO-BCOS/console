// SPDX-License-Identifier: UNLICENSED
pragma solidity >=0.6.10 <=0.8.26;

contract blobBaseFee {
    function getBlobBaseFeeYul() external view returns (uint256 blobBaseFee) {
        assembly {
            blobBaseFee := blobbasefee()
        }
    }

    function getBlobBaseFeeSolidity() external view returns (uint256 blobBaseFee) {
        blobBaseFee = block.blobbasefee;
    }
}