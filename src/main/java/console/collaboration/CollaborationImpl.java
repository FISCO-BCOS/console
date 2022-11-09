package console.collaboration;

import static org.fisco.bcos.sdk.v3.client.protocol.model.TransactionAttribute.LIQUID_CREATE;
import static org.fisco.bcos.sdk.v3.client.protocol.model.TransactionAttribute.LIQUID_SCALE_CODEC;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import console.common.ConsoleUtils;
import console.exception.ConsoleMessageException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.fisco.bcos.sdk.jni.utilities.tx.TxPair;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.ContractCodecException;
import org.fisco.bcos.sdk.v3.codec.datatypes.Bool;
import org.fisco.bcos.sdk.v3.codec.datatypes.DynamicArray;
import org.fisco.bcos.sdk.v3.codec.datatypes.DynamicBytes;
import org.fisco.bcos.sdk.v3.codec.datatypes.DynamicStruct;
import org.fisco.bcos.sdk.v3.codec.datatypes.StaticArray;
import org.fisco.bcos.sdk.v3.codec.datatypes.StaticStruct;
import org.fisco.bcos.sdk.v3.codec.datatypes.Type;
import org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String;
import org.fisco.bcos.sdk.v3.codec.scale.FunctionReturnDecoder;
import org.fisco.bcos.sdk.v3.codec.scale.ScaleCodecWriter;
import org.fisco.bcos.sdk.v3.codec.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.PrecompiledRetCode;
import org.fisco.bcos.sdk.v3.transaction.manager.AssembleTransactionProcessor;
import org.fisco.bcos.sdk.v3.transaction.manager.TransactionProcessorFactory;
import org.fisco.bcos.sdk.v3.transaction.model.dto.TransactionResponse;
import org.fisco.bcos.sdk.v3.utils.Hex;

public class CollaborationImpl implements CollaborationFace {
    private final Client client;
    private final AssembleTransactionProcessor assembleTransactionProcessor;
    private final CryptoKeyPair cryptoKeyPair;
    private final FunctionReturnDecoder functionReturnDecoder;
    private final ObjectMapper objectMapper;
    private final String FAKE_ABI =
            "[{\"inputs\":[],\"type\":\"constructor\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"internalType\":\"string\",\"type\":\"string\"}],\"type\":\"function\"}]";

    public CollaborationImpl(Client client) {
        this.client = client;
        CryptoKeyPair cryptoKeyPair = client.getCryptoSuite().getCryptoKeyPair();
        this.assembleTransactionProcessor =
                TransactionProcessorFactory.createAssembleTransactionProcessor(
                        client, cryptoKeyPair);
        this.cryptoKeyPair = client.getCryptoSuite().getCryptoKeyPair();
        this.functionReturnDecoder = new FunctionReturnDecoder();
        this.objectMapper = new ObjectMapper();
    }

    private Type buildType(ABIDefinition.NamedType namedType, String param)
            throws ContractCodecException, IOException {
        String typeStr = namedType.getType();
        ABIDefinition.Type paramType = new ABIDefinition.Type(typeStr);
        Type type = null;
        if (paramType.isList()) {
            List elements = new ArrayList();
            JsonNode jsonNode = this.objectMapper.readTree(param);
            assert jsonNode.isArray();

            ABIDefinition.NamedType subType = new ABIDefinition.NamedType();
            subType.setType(paramType.reduceDimensionAndGetType().getType());
            subType.setComponents(namedType.getComponents());

            for (JsonNode subNode : jsonNode) {
                String subNodeStr =
                        subNode.isTextual()
                                ? subNode.asText()
                                : this.objectMapper.writeValueAsString(subNode);
                Type element = buildType(subType, subNodeStr);
                elements.add(element);
            }
            type = paramType.isFixedList() ? new StaticArray(elements) : new DynamicArray(elements);
            return type;
        } else if (typeStr.equals("tuple")) {
            List<Type> components = new ArrayList<>();
            JsonNode jsonNode = this.objectMapper.readTree(param);
            assert jsonNode.isObject();
            for (ABIDefinition.NamedType component : namedType.getComponents()) {
                JsonNode subNode = jsonNode.get(component.getName());
                String subNodeStr =
                        subNode.isTextual()
                                ? subNode.asText()
                                : this.objectMapper.writeValueAsString(subNode);
                components.add(buildType(component, subNodeStr));
            }
            type =
                    namedType.isDynamic()
                            ? new DynamicStruct(components)
                            : new StaticStruct(components);
            return type;
        } else {
            if (typeStr.startsWith("uint")) {
                int bitSize = 256;
                if (!typeStr.equals("uint")) {
                    String bitSizeStr = typeStr.substring("uint".length());
                    try {
                        bitSize = Integer.parseInt(bitSizeStr);
                    } catch (NumberFormatException e) {
                        String errorMsg = " unrecognized uint type: " + typeStr;
                        throw new ContractCodecException(errorMsg);
                    }
                }

                try {
                    Class<?> uintClass =
                            Class.forName(
                                    "org.fisco.bcos.sdk.v3codec.datatypes.generated.Uint"
                                            + bitSize);
                    type =
                            (Type)
                                    uintClass
                                            .getDeclaredConstructor(BigInteger.class)
                                            .newInstance(new BigInteger(param));
                } catch (ClassNotFoundException
                        | NoSuchMethodException
                        | InstantiationException
                        | IllegalAccessException
                        | InvocationTargetException e) {
                    String errorMsg = "unrecognized uint type: " + typeStr;
                    throw new ContractCodecException(errorMsg);
                }

                return type;
            }

            if (typeStr.startsWith("int")) {
                int bitSize = 256;
                if (!typeStr.equals("int")) {
                    String bitSizeStr = typeStr.substring("int".length());
                    try {
                        bitSize = Integer.parseInt(bitSizeStr);
                    } catch (NumberFormatException e) {
                        String errorMsg = "unrecognized int type: " + typeStr;
                        throw new ContractCodecException(errorMsg);
                    }
                }

                try {
                    Class<?> uintClass =
                            Class.forName(
                                    "org.fisco.bcos.sdk.v3codec.datatypes.generated.Int" + bitSize);
                    type =
                            (Type)
                                    uintClass
                                            .getDeclaredConstructor(BigInteger.class)
                                            .newInstance(new BigInteger(param));
                } catch (ClassNotFoundException
                        | NoSuchMethodException
                        | InstantiationException
                        | IllegalAccessException
                        | InvocationTargetException e) {
                    String errorMsg = "unrecognized uint type: " + typeStr;
                    throw new ContractCodecException(errorMsg);
                }

                return type;
            }

            if (typeStr.equals("bool")) {
                type = new Bool(Boolean.parseBoolean(param));
                return type;
            }

            if (typeStr.equals("string")) {
                type = new Utf8String(param);
                return type;
            }

            if (typeStr.equals("bytes")) {
                type = new DynamicBytes(param.getBytes());
                return type;
            }

            if (typeStr.startsWith("bytes")) {
                String lengthStr = typeStr.substring("bytes".length());
                int length;
                try {
                    length = Integer.parseInt(lengthStr);
                } catch (NumberFormatException e) {
                    String errorMsg = "unrecognized static byte array type: " + typeStr;
                    throw new ContractCodecException(errorMsg);
                }

                if (length > 32) {
                    String errorMsg = "the length of static byte array exceeds 32: " + typeStr;
                    throw new ContractCodecException(errorMsg);
                }

                JsonNode jsonNode = this.objectMapper.readTree(param);
                assert jsonNode.isArray();
                if (jsonNode.size() != length) {
                    String errorMsg =
                            String.format(
                                    "expected byte array at length %d but length of provided in data is %d",
                                    length, jsonNode.size());
                    throw new ContractCodecException(errorMsg);
                }

                byte[] bytes = new byte[jsonNode.size()];
                for (int i = 0; i < jsonNode.size(); ++i) {
                    bytes[i] = ((byte) jsonNode.get(i).asInt());
                }
                try {
                    Class<?> bytesClass =
                            Class.forName(
                                    "org.fisco.bcos.sdk.v3codec.datatypes.generated.Bytes"
                                            + length);
                    type =
                            (Type)
                                    bytesClass
                                            .getDeclaredConstructor(byte[].class)
                                            .newInstance(bytes);
                } catch (ClassNotFoundException
                        | NoSuchMethodException
                        | InstantiationException
                        | IllegalAccessException
                        | InvocationTargetException e) {
                    e.printStackTrace();
                }
                return type;
            }
        }
        String errorMsg = "unrecognized type: " + typeStr;
        throw new ContractCodecException(errorMsg);
    }

    @Override
    public void initialize(String[] params) throws Exception {
        String binPath = ConsoleUtils.resolvePath(params[1]);
        String abiPath = ConsoleUtils.resolvePath(params[2]);

        try {
            File binFile = new File(binPath);
            byte[] bin = readBytes(binFile);
            String binStr = Hex.toHexString(bin);
            File abiFile = new File(abiPath);
            String abiStr = FileUtils.readFileToString(abiFile);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(abiStr);
            if (!jsonNode.isArray()) {
                throw new ConsoleMessageException("invalid ABI");
            }

            ByteArrayOutputStream paramsStream = new ByteArrayOutputStream();
            ScaleCodecWriter scaleCodecWriter = new ScaleCodecWriter(paramsStream);
            scaleCodecWriter.writeCompact(jsonNode.size());
            for (JsonNode contractAbi : jsonNode) {
                scaleCodecWriter.writeAsList(
                        contractAbi.toString().getBytes(StandardCharsets.UTF_8));
            }
            byte[] inputParams = paramsStream.toByteArray();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(new byte[1]);

            List<Type> deployParams = new ArrayList<>();
            deployParams.add(new DynamicBytes(bin));
            deployParams.add(new DynamicBytes(inputParams));
            deployParams.add(new Utf8String(abiStr));
            outputStream.write(
                    org.fisco.bcos.sdk.v3.codec.scale.FunctionEncoder.encodeParameters(
                            deployParams, null));

            String path = "collaboration/" + client.getCryptoSuite().hash(binStr + abiStr);
            int txAttribute = LIQUID_CREATE | LIQUID_SCALE_CODEC;

            TxPair txPair =
                    this.assembleTransactionProcessor.createSignedTransaction(
                            path, outputStream.toByteArray(), this.cryptoKeyPair, txAttribute);

            TransactionResponse response =
                    this.assembleTransactionProcessor.deployAndGetResponse(
                            FAKE_ABI, txPair.getSignedTx());
            if (response.getReturnCode() != PrecompiledRetCode.CODE_SUCCESS.getCode()) {
                System.out.println("initialize collaboration failed");
                System.out.println("return message: " + response.getReturnMessage());
                System.out.println("return code:" + response.getReturnCode());
                return;
            }

            System.out.println("initialize collaboration successfully");
            System.out.println(
                    "transaction hash: " + response.getTransactionReceipt().getTransactionHash());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ConsoleMessageException(
                    "initialize collaboration failed due to:" + e.getMessage(), e);
        }
    }

    // TODO: liquid collaboration not supported now
    @Override
    public void sign(String[] params) throws Exception {
        //        String contractName = params[1];
        //        List<String> inputParams = Arrays.asList(params).subList(2, params.length);
        //
        //        Tuple2<String, String> contractInfo =
        //                cnsService.selectByNameAndVersion(contractName, "collaboration");
        //        String address = contractInfo.getValue1();
        //        String abiStr = contractInfo.getValue2();
        //
        //        JsonNode jsonNode = this.objectMapper.readTree(abiStr);
        //        JsonNode data = jsonNode.get("data");
        //        System.out.println(data.toString());
        //        List<Type> inputs = new ArrayList<>();
        //        for (int i = 0; i < data.size(); ++i) {
        //            JsonNode item = data.get(i);
        //            ABIDefinition.NamedType input =
        //                    this.objectMapper.readValue(item.asText(),
        // ABIDefinition.NamedType.class);
        //            inputs.add(buildType(input, inputParams.get(i)));
        //        }
        //
        //        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //        outputStream.write(new byte[] {1});
        //
        //        byte[] methodID =
        //                Arrays.copyOfRange(
        //                        this.client
        //                                .getCryptoSuite()
        //                                .hash(contractName.getBytes(StandardCharsets.UTF_8)),
        //                        0,
        //                        4);
        //        byte[] encodedParams = FunctionEncoder.encodeParameters(inputs, methodID);
        //        outputStream.write(encodedParams);
        //        int txAttribute = LIQUID_SCALE_CODEC;
        //
        //        TxPair txPair =
        //                this.assembleTransactionProcessor.createSignedTransaction(
        //                        address, outputStream.toByteArray(), this.cryptoKeyPair,
        // txAttribute);
        //        TransactionReceipt receipt =
        //                this.client.sendTransaction(txPair.getSignedTx(),
        // false).getTransactionReceipt();
        //        if (receipt.getStatus() != 0) {
        //            System.out.println("sign contract failed");
        //            System.out.println("return message: " + receipt.getMessage());
        //            return;
        //        }
        //
        //        List<TypeReference<Type>> outputTypes = new ArrayList<>();
        //        outputTypes.add(TypeReference.makeTypeReference("uint32", false));
        //        System.out.println(
        //                "contract ID: "
        //                        + this.functionReturnDecoder.decode(receipt.getOutput(),
        // outputTypes));
    }

    // TODO: liquid collaboration not supported now
    @Override
    public void exercise(String[] params) throws Exception {
        //        String contract = params[1];
        //        String rightName = params[2];
        //        List<String> inputParams = Arrays.asList(params).subList(3, params.length);
        //
        //        String[] items = Strings.split(contract, '#');
        //        if (items.length != 2) {
        //            throw new ConsoleMessageException("invalid contract format");
        //        }
        //        String contractName = items[0];
        //        int contractID;
        //
        //        try {
        //            contractID = Integer.parseInt(items[1]);
        //        } catch (Exception e) {
        //            throw new ConsoleMessageException("invalid contract ID");
        //        }
        //
        //        Tuple2<String, String> contractInfo =
        //                this.cnsService.selectByNameAndVersion(contractName, "collaboration");
        //        String address = contractInfo.getValue1();
        //        String abi = contractInfo.getValue2();
        //
        //        JsonNode jsonNode = this.objectMapper.readTree(abi);
        //        JsonNode rights = jsonNode.get("rights");
        //        JsonNode rightAbi = null;
        //        for (JsonNode item : rights) {
        //            if (rightName.equals(item.get("name").toString())) {
        //                rightAbi = item;
        //            }
        //        }
        //
        //        if (rightAbi == null) {
        //            throw new ConsoleMessageException("no right named as: " + rightName);
        //        }
        //
        //        List<Type> inputTypes = new ArrayList<>();
        //        inputTypes.add(new Uint32(contractID));
        //
        //        JsonNode rightInputs = rightAbi.get("inputs");
        //        if (rightInputs.size() != params.length) {
        //            throw new ConsoleMessageException(
        //                    String.format(
        //                            "number of parameters mismatched: expected %d but got %d",
        //                            rightInputs.size(), params.length));
        //        }
        //
        //        for (int i = 0; i < rightInputs.size(); ++i) {
        //            JsonNode input = rightInputs.get(i);
        //            ABIDefinition.NamedType inputType =
        //                    this.objectMapper.readValue(input.asText(),
        // ABIDefinition.NamedType.class);
        //            inputTypes.add(buildType(inputType, inputParams.get(i)));
        //        }
        //
        //        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //        outputStream.write(new byte[] {1});
        //
        //        byte[] methodID =
        //                Arrays.copyOfRange(
        //                        this.client
        //                                .getCryptoSuite()
        //                                .hash(
        //                                        String.format("%s(%s)", contractName, rightName)
        //                                                .getBytes(StandardCharsets.UTF_8)),
        //                        0,
        //                        4);
        //        byte[] encodedParams = FunctionEncoder.encodeParameters(inputTypes, methodID);
        //        outputStream.write(encodedParams);
        //
        //        TxPair txPair =
        //                this.assembleTransactionProcessor.createSignedTransaction(
        //                        address,
        //                        outputStream.toByteArray(),
        //                        this.cryptoKeyPair,
        //                        LIQUID_SCALE_CODEC);
        //
        //        TransactionReceipt receipt =
        //                this.client.sendTransaction(txPair.getSignedTx(),
        // false).getTransactionReceipt();
        //        if (receipt.getStatus() != 0) {
        //            System.out.println("exercise right failed");
        //            System.out.println("return message: " + receipt.getMessage());
        //            return;
        //        }
        //
        //        List<TypeReference<Type>> outputTypes = new ArrayList<>();
        //        for (JsonNode output : rightAbi.get("outputs")) {
        //            ABIDefinition.NamedType outputType =
        //                    this.objectMapper.readValue(output.asText(),
        // ABIDefinition.NamedType.class);
        //            outputTypes.add(TypeReference.makeTypeReference(outputType.getType(), false));
        //        }
        //        List<Type> decodedOutputs =
        //                this.functionReturnDecoder.decode(receipt.getOutput(), outputTypes);
        //        ConsoleUtils.printReturnResults(decodedOutputs);
    }

    // TODO: liquid collaboration not supported now
    @Override
    public void fetch(String[] params) throws Exception {
        //        String contract = params[1];
        //
        //        String[] items = Strings.split(contract, '#');
        //        if (items.length != 2) {
        //            throw new ConsoleMessageException("invalid contract format");
        //        }
        //        String contractName = items[0];
        //        int contractID;
        //
        //        try {
        //            contractID = Integer.parseInt(items[1]);
        //        } catch (Exception e) {
        //            throw new ConsoleMessageException("invalid contract ID");
        //        }
        //
        //        Tuple2<String, String> contractInfo =
        //                cnsService.selectByNameAndVersion(contractName, "collaboration");
        //        String address = contractInfo.getValue1();
        //        String abiStr = contractInfo.getValue2();
        //
        //        List<Type> inputs = new ArrayList<>();
        //        inputs.add(new Uint32(contractID));
        //
        //        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //        outputStream.write(new byte[] {1});
        //
        //        byte[] methodID =
        //                Arrays.copyOfRange(
        //                        this.client
        //                                .getCryptoSuite()
        //                                .hash(contractName.getBytes(StandardCharsets.UTF_8)),
        //                        0,
        //                        4);
        //        byte[] encodedParams = FunctionEncoder.encodeParameters(inputs, methodID);
        //        outputStream.write(encodedParams);
        //
        //        TxPair txPair =
        //                this.assembleTransactionProcessor.createSignedTransaction(
        //                        address,
        //                        outputStream.toByteArray(),
        //                        this.cryptoKeyPair,
        //                        LIQUID_SCALE_CODEC);
        //
        //        TransactionReceipt receipt =
        //                this.client.sendTransaction(txPair.getSignedTx(),
        // false).getTransactionReceipt();
        //        if (receipt.getStatus() != 0) {
        //            System.out.println("exercise right failed");
        //            System.out.println("return message: " + receipt.getMessage());
        //            return;
        //        }
        //
        //        JsonNode root = this.objectMapper.readTree(abiStr);
        //        JsonNode data = root.get("data");
        //        List<TypeReference<Type>> outputTypes = new ArrayList<>();
        //        for (JsonNode output : data) {
        //            ABIDefinition.NamedType outputType =
        //                    this.objectMapper.readValue(output.asText(),
        // ABIDefinition.NamedType.class);
        //            outputTypes.add(TypeReference.makeTypeReference(outputType.getType(), false));
        //        }
        //        List<Type> decodedOutputs =
        //                this.functionReturnDecoder.decode(receipt.getOutput(), outputTypes);
        //        ConsoleUtils.printReturnResults(decodedOutputs);
    }

    private byte[] readBytes(File file) throws IOException {
        byte[] bytes = new byte[(int) file.length()];
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            if (fileInputStream.read(bytes) != bytes.length) {
                throw new IOException("incomplete reading of file: " + file);
            }
        }
        return bytes;
    }
}
