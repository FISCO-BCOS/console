package console.command.model;

import console.common.ConsoleUtils;
import console.common.ConsoleVersion;

public class WelcomeInfo {

    public static void welcome() {
        ConsoleUtils.doubleLine();
        System.out.println("Welcome to FISCO BCOS console(" + ConsoleVersion.Version + ")!");
        System.out.println("Type 'help' or 'h' for help. Type 'quit' or 'q' to quit console.");
        String logo =
                " ________ ______  ______   ______   ______       _______   ______   ______   ______  \n"
                        + "|        |      \\/      \\ /      \\ /      \\     |       \\ /      \\ /      \\ /      \\ \n"
                        + "| $$$$$$$$\\$$$$$|  $$$$$$|  $$$$$$|  $$$$$$\\    | $$$$$$$|  $$$$$$|  $$$$$$|  $$$$$$\\\n"
                        + "| $$__     | $$ | $$___\\$| $$   \\$| $$  | $$    | $$__/ $| $$   \\$| $$  | $| $$___\\$$\n"
                        + "| $$  \\    | $$  \\$$    \\| $$     | $$  | $$    | $$    $| $$     | $$  | $$\\$$    \\ \n"
                        + "| $$$$$    | $$  _\\$$$$$$| $$   __| $$  | $$    | $$$$$$$| $$   __| $$  | $$_\\$$$$$$\\\n"
                        + "| $$      _| $$_|  \\__| $| $$__/  | $$__/ $$    | $$__/ $| $$__/  | $$__/ $|  \\__| $$\n"
                        + "| $$     |   $$ \\\\$$    $$\\$$    $$\\$$    $$    | $$    $$\\$$    $$\\$$    $$\\$$    $$\n"
                        + " \\$$      \\$$$$$$ \\$$$$$$  \\$$$$$$  \\$$$$$$      \\$$$$$$$  \\$$$$$$  \\$$$$$$  \\$$$$$$";
        System.out.println(logo);
        System.out.println();
        ConsoleUtils.doubleLine();
    }
}
