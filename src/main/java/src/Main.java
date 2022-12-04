package src;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Main {

    public static BytecodeGenerator parserFromBytecode(String bytecode) {
        if (bytecode.length() < 1)
            return null;
        bytecode = bytecode.replaceAll("a165627a7a72305820\\S{64}0029$", ""); //remove swarm hash
        // we only need runtime bytecode, remove creation bytecode
        if (bytecode.contains("f30060806040")) {
            bytecode = bytecode.substring(bytecode.indexOf("f30060806040") + 4);
        }

        BinaryAnalyzer binaryAnalyzer = new BinaryAnalyzer(bytecode);
        if (binaryAnalyzer.legalContract) {
            try {
                BytecodeGenerator bytecodeGenerator = new BytecodeGenerator(binaryAnalyzer);
                bytecodeGenerator.detectAllSmells();
                return bytecodeGenerator;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }


    public static void parserFromSourceCodeFile(String filePath, String mainContracts) throws IOException {
        // String binary = Utils.runCMDWithTimeout(new String[]{"solc-0.4.25", "--bin-runtime", filePath});
        // Mac 端使用的是 solc
        String binary = Utils.runCMDWithTimeout(new String[]{"solc", "--bin-runtime", filePath});
        if (binary == null || binary.length() < 1) {
            System.out.println("Compile Error: " + filePath);
        }

        String[] tmp = binary.split("\n");
        boolean hasUnchechedExternalCalls = false;
        boolean hasStrictBalanceEquality = false;
        boolean hasTransactionStateDependency = false;
        boolean hasBlockInfoDependency = false;
        boolean hasDoSUnderExternalInfluence = false;
        boolean hasNestCall = false;
        boolean hasReentrancy = false;
        boolean hasGreedyContract = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tmp.length - 1; i++) {
            if (tmp[i].startsWith("Binary")) {
                String address = tmp[i - 1].replaceAll("=", "").replaceAll(" ", "").replace("\n", "");
                String bytecode = tmp[i + 1];
                if (!address.contains(mainContracts))
                    continue;
                System.out.println(address);
                BytecodeGenerator bytecodeGenerator = parserFromBytecode(bytecode);
                if (bytecodeGenerator != null) {
                    hasUnchechedExternalCalls |= bytecodeGenerator.hasUnchechedExternalCalls;
                    hasStrictBalanceEquality |= bytecodeGenerator.hasStrictBalanceEquality;
                    hasTransactionStateDependency |= bytecodeGenerator.hasTransactionStateDependency;
                    hasBlockInfoDependency |= bytecodeGenerator.hasBlockInfoDependency;
                    hasDoSUnderExternalInfluence |= bytecodeGenerator.hasDoSUnderExternalInfluence;
                    hasNestCall |= bytecodeGenerator.hasNestCall;
                    hasReentrancy |= bytecodeGenerator.hasReentrancy;
                    hasGreedyContract |= bytecodeGenerator.hasGreedyContract;
                    sb.append("Uncheck External Calls: " + hasUnchechedExternalCalls + "\n");
                    sb.append("Strict Balance Equality: " + hasStrictBalanceEquality + "\n");
                    sb.append("Transaction State Dependency: " + hasTransactionStateDependency + "\n");
                    sb.append("Block Info Dependency: " + hasBlockInfoDependency + "\n");
                    sb.append("Greedy Contract: " + hasGreedyContract + "\n");
                    sb.append("DoS Under External Influence: " + hasDoSUnderExternalInfluence + "\n");
                    sb.append("Nest Call: " + hasNestCall + "\n");
                    sb.append("Reentrancy: " + hasReentrancy + "\n");
                    System.out.println("Uncheck External Calls: " + hasUnchechedExternalCalls);
                    System.out.println("Strict Balance Equality: " + hasStrictBalanceEquality);
                    System.out.println("Transaction State Dependency: " + hasTransactionStateDependency);
                    System.out.println("Block Info Dependency: " + hasBlockInfoDependency);
                    System.out.println("Greedy Contract: " + hasGreedyContract);
                    System.out.println("DoS Under External Influence: " + hasDoSUnderExternalInfluence);
                    System.out.println("Nest Call: " + hasNestCall);
                    System.out.println("Reentrancy: " + hasReentrancy);
                }
            }
        }


    }

    public static void defectFromBytecode() throws Exception {

        /*****从字节码中检测*****/
        String bytecode =
                "608060405260043610603f576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff168063b9d92de8146044575b600080fd5b348015604f57600080fd5b50606c60048036038101908080359060200190929190505050608d565b60405180831515151581526020018281526020019250505060405180910390f35b60008060008310151515609c57fe5b9150915600";
        BytecodeGenerator byteBytecodeGenerator = parserFromBytecode(bytecode);

        // 打印检测结果
        System.out.println(byteBytecodeGenerator.printAllDetectResult());

        // 将 DefectChecker 类生成json 文件
        // WriteNonStringKeyAsString: 如果 key 不是字符串，则序列化为字符串
        String resultJson = JSONObject.toJSONString(byteBytecodeGenerator,
                SerializerFeature.PrettyFormat,
                SerializerFeature.WriteNonStringKeyAsString);
        System.out.println(resultJson);
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream("test.json"), "UTF-8");
        osw.write(resultJson);
        osw.flush();//清空缓冲区，强制输出数据
        osw.close();//关闭输出流
    }

    public static void defectFromSourcecode(String fileName) throws Exception {
        // 获取 resources 目录下的文件路径
        String filePath = Main.class.getClassLoader().getResource(fileName + ".sol").getPath();
        parserFromSourceCodeFile(filePath, "Basic");


    }


    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        /*  从字节码从检测 */
        defectFromBytecode();


        /*  从源代码中检测 */
        String fileName = "test";
        // defectFromSourcecode(fileName);

        long endTime = System.currentTimeMillis();
        System.out.println("Running time：" + (endTime - startTime) + "ms");
    }
}
