pragma solidity >=0.6.10 <=0.8.26;

import "./StorageSlot.sol";

contract StorageContract {
    using StorageSlot for *;

    StorageSlot.AddressSlotType private addressSlot = StorageSlot.asAddress(keccak256("address_slot"));
    StorageSlot.BooleanSlotType private booleanSlot = StorageSlot.asBoolean(keccak256("boolean_slot"));
    StorageSlot.Bytes32SlotType private bytes32Slot = StorageSlot.asBytes32(keccak256("bytes32_slot"));
    StorageSlot.Uint256SlotType private uint256Slot = StorageSlot.asUint256(keccak256("uint256_slot"));
    StorageSlot.Int256SlotType private int256Slot = StorageSlot.asInt256(keccak256("int256_slot"));

    function setAddress(address _value) public {
        require(_value != address(0), "Invalid address");
        addressSlot.tstore(_value);
    }

    function getAddress() public view returns (address) {
        return addressSlot.tload();
    }

    function setBytes32(bytes32 _value) public {
        require(_value != bytes32(0), "Invalid bytes32 value");
        bytes32Slot.tstore(_value);
    }

    function getBytes32() public view returns (bytes32) {
        return bytes32Slot.tload();
    }

    function setUint256(uint256 _value) public {
        require(_value <= type(uint256).max, "Invalid uint256 value");
        uint256Slot.tstore(_value);
    }

    function getUint256() public view returns (uint256) {
        return uint256Slot.tload();
    }

    function setInt256(int256 _value) public {
        require(_value >= type(int256).min, "Invalid int256 value");
        require(_value < type(int256).max, "Invalid int256 value");
        int256Slot.tstore(_value);
    }

    function getInt256() public view returns (int256) {
        return int256Slot.tload();
    }

    function storeIntTest(int256 _value) public returns (int256) {
        require(_value >= type(int256).min, "Invalid int256 value");
        require(_value < type(int256).max, "Invalid int256 value");
        int256Slot.tstore(_value);

        return int256Slot.tload();
    }

    function storeUintTest(uint256 _value) public returns (uint256) {
        require(_value <= type(uint256).max, "Invalid uint256 value");
        uint256Slot.tstore(_value);
        return uint256Slot.tload();
    }

    function storeBytes32Test(bytes32 _value) public returns (bytes32) {
        require(_value != bytes32(0), "Invalid bytes32 value");
        bytes32Slot.tstore(_value);
        return bytes32Slot.tload();
    }

    function storeBooleanTest(bool _value) public returns (bool) {
        booleanSlot.tstore(_value);
        return booleanSlot.tload();
    }

    function storeAddressTest(address _value) public returns (address) {
        require(_value != address(0), "Invalid address");
        addressSlot.tstore(_value);
        return addressSlot.tload();
    }
}