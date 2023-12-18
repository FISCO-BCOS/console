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
package console.command;

import console.command.category.AccountOpCommand;
import console.command.category.AuthOpCommand;
import console.command.category.BalanceOpCommand;
import console.command.category.BasicCommand;
import console.command.category.BfsCommand;
import console.command.category.ConsensusOpCommand;
import console.command.category.ContractOpCommand;
import console.command.category.CrudCommand;
import console.command.category.GroupCommand;
import console.command.category.ShardingCommand;
import console.command.category.StatusQueryCommand;
import console.command.model.BasicCategoryCommand;
import console.command.model.CommandInfo;
import console.common.ConsoleUtils;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class SupportedCommand {
    protected static final List<BasicCategoryCommand> categoryCommandList = new ArrayList<>();
    public static boolean isWasm = false;
    public static boolean isAuthOpen = false;

    private SupportedCommand() {}

    public static void setIsAuthOpen(boolean authOpen) {
        isAuthOpen = authOpen;
    }

    public static void setIsWasm(boolean wasm) {
        isWasm = wasm;
        ContractOpCommand.setIsWasm(wasm);
    }

    public static final BasicCommand basicCommand = new BasicCommand();
    public static final ContractOpCommand contractOpCommand = new ContractOpCommand();
    public static final StatusQueryCommand statusQueryCommand = new StatusQueryCommand();
    public static final ConsensusOpCommand consensusOpCommand = new ConsensusOpCommand();
    public static final BfsCommand bfsCommand = new BfsCommand();
    public static final CrudCommand crudCommand = new CrudCommand();
    public static final GroupCommand groupCommand = new GroupCommand();
    public static final AuthOpCommand authOpCommand = new AuthOpCommand();
    public static final AccountOpCommand accountOpCommand = new AccountOpCommand();
    public static final ShardingCommand shardingCommand = new ShardingCommand();
    public static final BalanceOpCommand balanceOpCommand = new BalanceOpCommand();

    /// FIXME: not supported now
    // public static CollaborationOpCommand collaborationOpCommand = new CollaborationOpCommand();

    public static final CommandInfo HELP =
            new CommandInfo(
                    "help",
                    "Provide help information",
                    new CommandInfo.UsageDisplay() {
                        @Override
                        public void printUsageInfo() {
                            printUsageInfo();
                        }
                    },
                    new ArrayList<>(
                            Arrays.asList("-h", "-help", "--h", "--H", "--help", "-H", "h")),
                    (consoleInitializer, params, pwd) -> printDescInfo(isWasm, isAuthOpen));

    static {
        Field[] fields = SupportedCommand.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                if (field.get(null) instanceof BasicCategoryCommand) {
                    BasicCategoryCommand basicCategoryCommand =
                            (BasicCategoryCommand) field.get(null);
                    categoryCommandList.add(basicCategoryCommand);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static CommandInfo getCommandInfo(String command, boolean isWasm, boolean isAuthOpen) {
        if (Objects.equals(command, HELP.getCommand()) || HELP.getOptionCommand().contains(command))
            return HELP;
        Function<CommandInfo, Boolean> commandInfoFilter =
                (CommandInfo info) ->
                        !(isWasm && !info.isWasmSupport()
                                || (!isAuthOpen && info.isNeedAuthOpen()));
        for (BasicCategoryCommand basicCategoryCommand : categoryCommandList) {
            CommandInfo commandInfo = basicCategoryCommand.getCommandInfo(command);
            if (commandInfo != null && commandInfoFilter.apply(commandInfo)) {
                return commandInfo;
            }
        }
        return null;
    }

    public static List<String> getAllCommand(boolean isWasm, boolean isAuthOpen) {
        List<String> commands = new ArrayList<>();
        for (BasicCategoryCommand basicCategoryCommand : categoryCommandList) {
            commands.addAll(basicCategoryCommand.getAllCommand(isWasm, isAuthOpen));
        }
        commands.add(HELP.getCommand());
        return commands;
    }

    public static void printDescInfo(boolean isWasm, boolean isAuthOpen) {
        HELP.printDescInfo();
        for (BasicCategoryCommand basicCategoryCommand : categoryCommandList) {
            basicCategoryCommand.printDescInfo(isWasm, isAuthOpen);
        }
        ConsoleUtils.singleLine();
    }

    public static void printNonInteractiveDescInfo() {
        System.out.println("# bash console.sh [groupId] [Subcommand]");
        System.out.println(
                "# groupId(Optional): The groupId that  that received the request, default is 1");
        System.out.println(
                "# Subcommand[Required]: The command sent to the node, Please refer to the following for the list of subCommand");
        System.out.println(
                "use command \"bash console.sh [subCommand] -h\" to get the help of the subcommand.\n");
        System.out.println("# Subcommand list:");
        HELP.printDescInfo();
        for (BasicCategoryCommand basicCategoryCommand : categoryCommandList) {
            basicCategoryCommand.printDescInfo(isWasm, isAuthOpen);
        }
        ConsoleUtils.singleLine();
    }
}
