package console.common;

import console.exception.ConsoleMessageException;
import java.io.File;

public class PathUtils {
    /** the solidity file default directory */
    public static final String SOL_DIRECTORY = "contracts/solidity";
    /** */
    public static final String ACCOUNT_DIRECTORY = "accounts/";
    /** solidity file ext name */
    public static final String SOL_POSTFIX = ".sol";

    /**
     * Remove the postfix of the name
     *
     * @param name
     * @param postfix
     * @return
     */
    public static String removePostfix(String name, String postfix) {
        if (name.endsWith(postfix)) {
            return name.substring(0, name.length() - postfix.length());
        }

        return name;
    }

    /**
     * Remove the .sol postfix of the name
     *
     * @param name
     * @return
     */
    public static String removeSolPostfix(String name) {
        return removePostfix(name, SOL_POSTFIX);
    }

    /**
     * Add the .sol postfix to the name
     *
     * @param name
     * @return
     */
    public static String addSolPostfix(String name) {
        name = removePostfix(name, SOL_POSTFIX);
        return name + SOL_POSTFIX;
    }

    /**
     * @param solFileNameOrPath
     * @return
     */
    public static File getSolFile(String solFileNameOrPath) throws ConsoleMessageException {

        String filePath = solFileNameOrPath;
        filePath = removePostfix(filePath, SOL_POSTFIX);
        filePath += SOL_POSTFIX;
        /** Check that the file exists in the default directory first */
        File solFile = new File(SOL_DIRECTORY + File.separator + filePath);
        if (!solFile.exists()) {
            /** Check if the file exists */
            solFile = new File(filePath);
        }

        /** file not exist */
        if (!solFile.exists()) {
            throw new ConsoleMessageException(solFileNameOrPath + " does not exist ");
        }

        return solFile;
    }
}
