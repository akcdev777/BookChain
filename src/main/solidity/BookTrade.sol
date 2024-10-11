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

    // The seller is msg.sender, and the buyer is passed as a parameter
    function storeTrade(address _buyer, string memory _bookTitle, uint256 _price) public {
        trades.push(Trade({
            buyer: _buyer,
            seller: msg.sender,
            bookTitle: _bookTitle,
            price: _price,
            timestamp: block.timestamp // stores Unix timestamp
        }));
        
        emit NewTrade(_buyer, msg.sender, _bookTitle, _price, block.timestamp);
    }

    // Retrieve the details of a trade
    function getTrade(uint256 index) public view returns (address, address, string memory, uint256, uint256) {
        Trade memory trade = trades[index];
        return (trade.buyer, trade.seller, trade.bookTitle, trade.price, trade.timestamp);
    }

    // Get the total number of trades
    function getTotalTrades() public view returns (uint256) {
        return trades.length;
    }
}
