package console.data.tools;

public class Common {
    public static String SafeKeeper_ROLE_ADMIN = "admin";
    public static String SafeKeeper_ROLE_VISITOR = "visitor";
    public static int SafeKeeper_ROLE_ADMIN_ID = 100000;
    public static int SafeKeeper_ROLE_VISITOR_ID = 100001;
    public static String FILE_PATH = "accounts/";
    public static String FILE_GM_PATH = "accounts_gm/";
    public static String ACCOUNT_NAME_FORMAT =
            "Begins with a letter, between 5 and 20 in length, and contain only characters, numbers, and underscores.";
    public static String PASSWORD_FORMAT =
            "Between 6 and 20 in length, and contain only characters, numbers, and underscores.";

    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }

        return sb.toString();
    }

    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1) {
            return null;
        }
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    public static boolean checkUserName(String userName) {
        // Begins with a letter, between 5 and 20 in length, and contain only characters, Numbers,
        // and underscores.
        String regExp = "^[a-zA-Z][a-zA-Z0-9_]{4,19}$";
        return userName.matches(regExp);
    }

    public static boolean checkPassword(String password) {
        // Between 6 and 20 in length, and contain only characters, Numbers, and underscores.
        String regExp = "^[0-9a-zA-Z_]{6,20}$";
        return password.matches(regExp);
    }
}
