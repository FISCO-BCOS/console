// SPDX-License-Identifier: MIT

contract BlobHashExample {
    bytes public largeData = "This is a very large data blob that needs to be hashed efficiently.";

    function hashData() public view returns (bytes32) {
        bytes32 result;
        assembly {
            result := blobhash(add(sload(largeData.slot), largeData.offset))
        }
        return result;
    }
}