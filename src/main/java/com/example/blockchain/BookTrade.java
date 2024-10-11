package com.example.blockchain;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple5;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/hyperledger/web3j/tree/main/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 1.6.1.
 */
@SuppressWarnings("rawtypes")
public class BookTrade extends Contract {
    public static final String BINARY = "0x6080604052348015600f57600080fd5b5061075e8061001f6000396000f3fe608060405234801561001057600080fd5b506004361061004c5760003560e01c80631e6c598e146100515780632db25e051461007e578063b5569e4014610091578063ebd34f50146100a6575b600080fd5b61006461005f36600461040e565b6100b7565b60405161007595949392919061046d565b60405180910390f35b61006461008c36600461040e565b61018f565b6100a461009f3660046104c3565b6102bf565b005b600054604051908152602001610075565b600081815481106100c757600080fd5b60009182526020909120600590910201805460018201546002830180546001600160a01b039384169550929091169291610100906105a5565b80601f016020809104026020016040519081016040528092919081815260200182805461012c906105a5565b80156101795780601f1061014e57610100808354040283529160200191610179565b820191906000526020600020905b81548152906001019060200180831161015c57829003601f168201915b5050505050908060030154908060040154905085565b600080606060008060008087815481106101ab576101ab6105df565b60009182526020918290206040805160a081018252600590930290910180546001600160a01b03908116845260018201541693830193909352600283018054929392918401916101fa906105a5565b80601f0160208091040260200160405190810160405280929190818152602001828054610226906105a5565b80156102735780601f1061024857610100808354040283529160200191610273565b820191906000526020600020905b81548152906001019060200180831161025657829003601f168201915b5050505050815260200160038201548152602001600482015481525050905080600001518160200151826040015183606001518460800151955095509550955095505091939590929450565b6040805160a0810182523381526001600160a01b03858116602083019081529282018581526060830185905242608084015260008054600181018255908052835160059091027f290decd9548b62a8d60345a988386fc84ba6bc95484008f6362f93160ef3e563810180549285166001600160a01b031993841617815595517f290decd9548b62a8d60345a988386fc84ba6bc95484008f6362f93160ef3e5648201805491909516921691909117909255519192917f290decd9548b62a8d60345a988386fc84ba6bc95484008f6362f93160ef3e565909101906103a39082610644565b5060608201518160030155608082015181600401555050826001600160a01b0316336001600160a01b03167f02a088c25cb1c930c7d2430ae177f00971f819c241680a8c4bbff905dcc447f984844260405161040193929190610703565b60405180910390a3505050565b60006020828403121561042057600080fd5b5035919050565b6000815180845260005b8181101561044d57602081850181015186830182015201610431565b506000602082860101526020601f19601f83011685010191505092915050565b6001600160a01b0386811682528516602082015260a06040820181905260009061049990830186610427565b606083019490945250608001529392505050565b634e487b7160e01b600052604160045260246000fd5b6000806000606084860312156104d857600080fd5b83356001600160a01b03811681146104ef57600080fd5b9250602084013567ffffffffffffffff81111561050b57600080fd5b8401601f8101861361051c57600080fd5b803567ffffffffffffffff811115610536576105366104ad565b604051601f8201601f19908116603f0116810167ffffffffffffffff81118282101715610565576105656104ad565b60405281815282820160200188101561057d57600080fd5b8160208401602083013760009181016020019190915293969395505050506040919091013590565b600181811c908216806105b957607f821691505b6020821081036105d957634e487b7160e01b600052602260045260246000fd5b50919050565b634e487b7160e01b600052603260045260246000fd5b601f82111561063f57806000526020600020601f840160051c8101602085101561061c5750805b601f840160051c820191505b8181101561063c5760008155600101610628565b50505b505050565b815167ffffffffffffffff81111561065e5761065e6104ad565b6106728161066c84546105a5565b846105f5565b6020601f8211600181146106a6576000831561068e5750848201515b600019600385901b1c1916600184901b17845561063c565b600084815260208120601f198516915b828110156106d657878501518255602094850194600190920191016106b6565b50848210156106f45786840151600019600387901b60f8161c191681555b50505050600190811b01905550565b6060815260006107166060830186610427565b6020830194909452506040015291905056fea2646970667358221220f1e63dfdb695c03a4857a7bf8457672b59b40b1048b2f6a6d526ada2da89c76064736f6c634300081b0033\n";

    private static String librariesLinkedBinary;

    public static final String FUNC_GETTOTALTRADES = "getTotalTrades";

    public static final String FUNC_GETTRADE = "getTrade";

    public static final String FUNC_STORETRADE = "storeTrade";

    public static final String FUNC_TRADES = "trades";

    public static final Event NEWTRADE_EVENT = new Event("NewTrade", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
    ;

    @Deprecated
    protected BookTrade(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected BookTrade(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected BookTrade(String contractAddress, Web3j web3j, TransactionManager transactionManager,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected BookTrade(String contractAddress, Web3j web3j, TransactionManager transactionManager,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteFunctionCall<BigInteger> getTotalTrades() {
        final Function function = new Function(FUNC_GETTOTALTRADES, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<Tuple5<String, String, String, BigInteger, BigInteger>> getTrade(
            BigInteger index) {
        final Function function = new Function(FUNC_GETTRADE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(index)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple5<String, String, String, BigInteger, BigInteger>>(function,
                new Callable<Tuple5<String, String, String, BigInteger, BigInteger>>() {
                    @Override
                    public Tuple5<String, String, String, BigInteger, BigInteger> call() throws
                            Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple5<String, String, String, BigInteger, BigInteger>(
                                (String) results.get(0).getValue(), 
                                (String) results.get(1).getValue(), 
                                (String) results.get(2).getValue(), 
                                (BigInteger) results.get(3).getValue(), 
                                (BigInteger) results.get(4).getValue());
                    }
                });
    }

    public RemoteFunctionCall<TransactionReceipt> storeTrade(String _seller, String _bookTitle,
            BigInteger _price) {
        final Function function = new Function(
                FUNC_STORETRADE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _seller), 
                new org.web3j.abi.datatypes.Utf8String(_bookTitle), 
                new org.web3j.abi.datatypes.generated.Uint256(_price)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Tuple5<String, String, String, BigInteger, BigInteger>> trades(
            BigInteger param0) {
        final Function function = new Function(FUNC_TRADES, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple5<String, String, String, BigInteger, BigInteger>>(function,
                new Callable<Tuple5<String, String, String, BigInteger, BigInteger>>() {
                    @Override
                    public Tuple5<String, String, String, BigInteger, BigInteger> call() throws
                            Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple5<String, String, String, BigInteger, BigInteger>(
                                (String) results.get(0).getValue(), 
                                (String) results.get(1).getValue(), 
                                (String) results.get(2).getValue(), 
                                (BigInteger) results.get(3).getValue(), 
                                (BigInteger) results.get(4).getValue());
                    }
                });
    }

    public static List<NewTradeEventResponse> getNewTradeEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(NEWTRADE_EVENT, transactionReceipt);
        ArrayList<NewTradeEventResponse> responses = new ArrayList<NewTradeEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            NewTradeEventResponse typedResponse = new NewTradeEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.buyer = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.seller = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.bookTitle = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.price = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static NewTradeEventResponse getNewTradeEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(NEWTRADE_EVENT, log);
        NewTradeEventResponse typedResponse = new NewTradeEventResponse();
        typedResponse.log = log;
        typedResponse.buyer = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.seller = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.bookTitle = (String) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.price = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
        return typedResponse;
    }

    public Flowable<NewTradeEventResponse> newTradeEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getNewTradeEventFromLog(log));
    }

    public Flowable<NewTradeEventResponse> newTradeEventFlowable(DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(NEWTRADE_EVENT));
        return newTradeEventFlowable(filter);
    }

    @Deprecated
    public static BookTrade load(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return new BookTrade(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static BookTrade load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new BookTrade(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static BookTrade load(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return new BookTrade(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static BookTrade load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new BookTrade(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<BookTrade> deploy(Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return deployRemoteCall(BookTrade.class, web3j, credentials, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<BookTrade> deploy(Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(BookTrade.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    public static RemoteCall<BookTrade> deploy(Web3j web3j, TransactionManager transactionManager,
            ContractGasProvider contractGasProvider) {
        return deployRemoteCall(BookTrade.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<BookTrade> deploy(Web3j web3j, TransactionManager transactionManager,
            BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(BookTrade.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    public static void linkLibraries(List<Contract.LinkReference> references) {
        librariesLinkedBinary = linkBinaryWithReferences(BINARY, references);
    }

    private static String getDeploymentBinary() {
        if (librariesLinkedBinary != null) {
            return librariesLinkedBinary;
        } else {
            return BINARY;
        }
    }

    public static class NewTradeEventResponse extends BaseEventResponse {
        public String buyer;

        public String seller;

        public String bookTitle;

        public BigInteger price;

        public BigInteger timestamp;
    }
}
