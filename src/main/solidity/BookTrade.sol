// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

contract BookTrade {

    struct Trade {
        address buyer;
        address seller;
        string bookTitle;
        uint256 price;
        uint256 timestamp;
    }

    Trade[] public trades;

    event NewTrade(
        address indexed buyer,
        address indexed seller,
        string bookTitle,
        uint256 price,
        uint256 timestamp
    );

    function storeTrade(address _seller, string memory _bookTitle, uint256 _price) public {
        trades.push(Trade({
            buyer: msg.sender,
            seller: _seller,
            bookTitle: _bookTitle,
            price: _price,
            timestamp: block.timestamp // stores Unix timestamp
        }));
        
        emit NewTrade(msg.sender, _seller, _bookTitle, _price, block.timestamp);
    }

    function getTrade(uint256 index) public view returns (address, address, string memory, uint256, uint256) {
        Trade memory trade = trades[index];
        return (trade.buyer, trade.seller, trade.bookTitle, trade.price, trade.timestamp);
    }

    function getTotalTrades() public view returns (uint256) {
        return trades.length;
    }
}
