/**
 * Copyright 2014-2020 [fisco-dev]
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package console.command.model;

import console.ConsoleInitializer;
import console.command.SupportedCommand;
import java.util.List;

public class CommandInfo {
    @FunctionalInterface
    public interface UsageDisplay {
        void printUsageInfo();
    }

    @FunctionalInterface
    public interface CommandImplement {
        void call(ConsoleInitializer consoleInitializer, String[] params, String pwd)
                throws Exception;
    }

    private final String command;
    private String desc;
    private UsageDisplay usageDisplay;
    private List<String> optionCommand = null;
    private final CommandImplement commandImplement;
    private int minParamLength = -1;
    private int maxParamLength = -1;
    private boolean isWasmSupport = true;
    private boolean needAuthOpen = false;
    String minSupportVersion = null;
    boolean supportNonInteractive = true;

    public CommandInfo(String command, String desc, CommandImplement commandImplement) {
        this.command = command;
        this.desc = desc;
        this.commandImplement = commandImplement;
    }

    public CommandInfo(
            String command,
            String desc,
            UsageDisplay usageDisplay,
            List<String> optionCommand,
            CommandImplement commandImplement) {
        this(command, desc, optionCommand, commandImplement);
        this.usageDisplay = usageDisplay;
    }

    public CommandInfo(
            String command,
            String desc,
            UsageDisplay usageDisplay,
            CommandImplement commandImplement) {
        this(command, desc, commandImplement);
        this.usageDisplay = usageDisplay;
    }

    public CommandInfo(
            String command,
            String desc,
            CommandImplement commandImplement,
            String minSupportVersion) {
        this(command, desc, commandImplement);
        this.minSupportVersion = minSupportVersion;
    }

    public CommandInfo(
            String command,
            String desc,
            UsageDisplay usageDisplay,
            CommandImplement commandImplement,
            int minParamLength,
            int maxParamLength) {
        this(command, desc, usageDisplay, commandImplement);
        this.minParamLength = minParamLength;
        this.maxParamLength = maxParamLength;
    }

    public CommandInfo(
            String command,
            String desc,
            UsageDisplay usageDisplay,
            CommandImplement commandImplement,
            int minParamLength,
            int maxParamLength,
            boolean supportNonInteractive) {
        this(command, desc, usageDisplay, commandImplement, minParamLength, maxParamLength);
        this.supportNonInteractive = supportNonInteractive;
    }

    public CommandInfo(
            String command,
            String desc,
            UsageDisplay usageDisplay,
            CommandImplement commandImplement,
            int minParamLength,
            int maxParamLength,
            boolean supportNonInteractive,
            boolean isWasmSupport) {
        this(command, desc, usageDisplay, commandImplement, minParamLength, maxParamLength);
        this.supportNonInteractive = supportNonInteractive;
        this.isWasmSupport = isWasmSupport;
    }

    public CommandInfo(
            String command,
            String desc,
            UsageDisplay usageDisplay,
            CommandImplement commandImplement,
            int minParamLength,
            int maxParamLength,
            boolean supportNonInteractive,
            boolean isWasmSupport,
            boolean needAuthOpen) {
        this(
                command,
                desc,
                usageDisplay,
                commandImplement,
                minParamLength,
                maxParamLength,
                supportNonInteractive,
                isWasmSupport);
        this.needAuthOpen = needAuthOpen;
    }

    public CommandInfo(
            String command,
            String desc,
            List<String> optionCommand,
            CommandImplement commandImplement) {
        this(command, desc, commandImplement);
        this.optionCommand = optionCommand;
    }

    public CommandInfo(
            String command,
            String desc,
            List<String> optionCommand,
            CommandImplement commandImplement,
            boolean supportNonInteractive) {
        this(command, desc, optionCommand, commandImplement);
        this.supportNonInteractive = supportNonInteractive;
    }

    public CommandInfo(
            String command,
            String desc,
            List<String> optionCommand,
            UsageDisplay usageDisplay,
            CommandImplement commandImplement,
            int minParamLength,
            int maxParamLength) {
        this(command, desc, usageDisplay, optionCommand, commandImplement);
        this.minParamLength = minParamLength;
        this.maxParamLength = maxParamLength;
    }

    public CommandInfo(
            String command,
            String desc,
            List<String> optionCommand,
            UsageDisplay usageDisplay,
            CommandImplement commandImplement,
            int minParamLength,
            int maxParamLength,
            boolean supportNonInteractive) {
        this(
                command,
                desc,
                optionCommand,
                usageDisplay,
                commandImplement,
                minParamLength,
                maxParamLength);
        this.supportNonInteractive = supportNonInteractive;
    }

    public boolean isWasmSupport() {
        return isWasmSupport;
    }

    public boolean isNeedAuthOpen() {
        return needAuthOpen;
    }

    public boolean isSupportNonInteractive() {
        return supportNonInteractive;
    }

    public void setSupportNonInteractive(boolean supportNonInteractive) {
        this.supportNonInteractive = supportNonInteractive;
    }

    public List<String> getOptionCommand() {
        return optionCommand;
    }

    public String getCommand() {
        return command;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void printDescInfo() {
        if (optionCommand != null) {
            System.out.printf("* %-40s  %s%n", command + "(" + optionCommand + ")", desc);
        } else {
            System.out.printf("* %-40s  %s%n", command, desc);
        }
    }

    public void printUsageInfo() {
        if (this.usageDisplay != null) {
            usageDisplay.printUsageInfo();
        } else {
            printDescInfo();
        }
        System.out.println();
    }

    public UsageDisplay getUsageDisplay() {
        return usageDisplay;
    }

    public void setUsageDisplay(UsageDisplay usageDisplay) {
        this.usageDisplay = usageDisplay;
    }

    public void setOptionCommand(List<String> optionCommand) {
        this.optionCommand = optionCommand;
    }

    public CommandImplement getCommandImplement() {
        return commandImplement;
    }

    public int getMinParamLength() {
        return minParamLength;
    }

    public int getMaxParamLength() {
        return maxParamLength;
    }

    public boolean commandEqual(String command) {
        if (this.getCommand().equals(command)) {
            return true;
        }
        if (optionCommand != null && optionCommand.contains(command)) {
            return true;
        }
        return false;
    }

    public String getMinSupportVersion() {
        return minSupportVersion;
    }

    public void callCommand(ConsoleInitializer consoleInitializer, String[] params, String pwd)
            throws Exception {
        // print help info
        if (params.length >= 2 && SupportedCommand.HELP.getOptionCommand().contains(params[1])) {
            HelpInfo.printHelp(
                    command,
                    consoleInitializer.getClient().isWASM(),
                    consoleInitializer.getClient().isEnableCommittee());
            return;
        }

        if (maxParamLength != -1 && (params.length - 1) > maxParamLength) {
            System.out.printf(
                    "Expected at most %d arguments but found %d%n",
                    maxParamLength, params.length - 1);
            HelpInfo.promptHelp(command);
            return;
        }

        if (minParamLength != -1 && (params.length - 1) < minParamLength) {
            System.out.printf(
                    "Expected at least %d arguments but found %d%n",
                    minParamLength, params.length - 1);
            HelpInfo.printHelp(
                    command,
                    consoleInitializer.getClient().isWASM(),
                    consoleInitializer.getClient().isEnableCommittee());
            return;
        }
        // check version
        if (minSupportVersion != null) {
            return;
        }
        commandImplement.call(consoleInitializer, params, pwd);
        System.out.println();
    }

    public void setMaxParamLength(int maxParamLength) {
        this.maxParamLength = maxParamLength;
    }

    public void setMinParamLength(int minParamLength) {
        this.minParamLength = minParamLength;
    }
}
