//pragma solidity ^0.4.25;
//contract Victim{
//    mapping(address => uint) public userBalance;
//    function withDraw(uint amount){
//
//        if(userBalance[msg.sender] > amount){
//            // msg.sender.transfer(amount);
//            // userBalance[msg.sender] -= amount;
//            msg.sender.call.value(amount)();
//            userBalance[msg.sender] -= amount;
//        }
//    }
//}
pragma solidity ^0.4.25;
contract Basic {
    function calculate(uint value) public returns (bool, uint){
        assert(value >= 0);
    }
}
