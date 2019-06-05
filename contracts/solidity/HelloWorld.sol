pragma solidity ^0.4.2;

contract HelloWorld{
    string name;
    bytes32 item;

    function HelloWorld(){
       name = "Hello, World!";
    }

    function get()constant returns(string){
        return name;
    }

    function get1()constant returns(bytes32){
        return item;
    }
    
    function set(string n){
    	name = n;
    }

    function set(string a, bytes32 b){
      name = a;
    	item = b;
    }
}