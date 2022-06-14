package console.command.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class BasicCategoryCommand {
    private CommandType commandType;

    protected BasicCategoryCommand(CommandType commandType) {
        this.commandType = commandType;
    }

    public abstract CommandInfo getCommandInfo(String command);

    public abstract List<String> getAllCommand(boolean isWasm, boolean isAuthOpen);

    public abstract Map<String, CommandInfo> getAllCommandInfo(boolean isWasm);

    public void printDescInfo(boolean isWasm, boolean isAuthOpen) {
        List<String> allCommand = getAllCommand(isWasm, isAuthOpen);
        List<String> outputCmd = new ArrayList<>();
        if (allCommand == null || allCommand.isEmpty()) return;
        Collections.sort(allCommand);
        System.out.printf(
                "---------------------------%s----------------------------%n",
                getCommandType().toString());
        for (String key : allCommand) {
            CommandInfo commandInfo = getCommandInfo(key);
            if (outputCmd.contains(commandInfo.getCommand())) continue;
            outputCmd.add(commandInfo.getCommand());
            commandInfo.printDescInfo();
        }
    }

    public CommandType getCommandType() {
        return commandType;
    }
}
