// SPDX-License-Identifier: UNLICENSED
contract Mcopy {
    function memoryCopy() external pure returns (bytes32 x) {
        assembly {
            mstore(0x20, 0x50)  // Store 0x50 at word 1 in memory
            mcopy(0, 0x20, 0x20)  // Copies 0x50 to word 0 in memory
            x := mload(0)    // Returns 32 bytes "0x50"
        }
    }
}
