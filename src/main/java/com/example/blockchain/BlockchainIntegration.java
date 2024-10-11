package com.example.blockchain;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.http.HttpService;
import org.web3j.tuples.generated.Tuple5;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.tx.RawTransactionManager;
import org.web3j.crypto.Credentials;

import java.math.BigInteger;

public class BlockchainIntegration {

    private Web3j web3;
    private BookTrade contract;
    private Credentials credentials;

    public BlockchainIntegration(String contractAddress, String privateKey) {
        // Initialize Web3j to communicate with local Anvil node
        web3 = Web3j.build(new HttpService("http://localhost:8545"));
        credentials = Credentials.create(privateKey);

        // Set gas provider
        ContractGasProvider gasProvider = new StaticGasProvider(BigInteger.valueOf(1000), BigInteger.valueOf(1000000));

        // Load the deployed contract
        contract = BookTrade.load(contractAddress, web3, credentials, gasProvider);
    }

    public void storeTrade(String sellerAddress, String bookTitle, BigInteger storePrice) throws Exception {
        BigInteger priceBig = storePrice;
        RemoteCall<?> remoteCall = contract.storeTrade(sellerAddress, bookTitle, priceBig);
        remoteCall.send();
        System.out.println("Trade has been stored on the blockchain.");
    }

    public void getTrade(int index) throws Exception {
        Tuple5<String, String, String, BigInteger, BigInteger> trade = contract.getTrade(BigInteger.valueOf(index)).send();
        System.out.println("Buyer: " + trade.getValue1());
        System.out.println("Seller: " + trade.getValue2());
        System.out.println("Book Title: " + trade.getValue3());
        System.out.println("Price: " + trade.getValue4());
        System.out.println("Timestamp: " + trade.getValue5());
    }

    
}
