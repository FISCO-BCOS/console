package console.command.category;

import console.command.model.BasicCategoryCommand;
import console.command.model.CommandInfo;
import console.command.model.CommandType;
import console.command.model.HelpInfo;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AuthOpCommand extends BasicCategoryCommand {
    protected static final Map<String, CommandInfo> commandToCommandInfo = new HashMap<>();

    public AuthOpCommand() {
        super(CommandType.AUTH_OP);
    }

    @Override
    public CommandInfo getCommandInfo(String command) {
        if (commandToCommandInfo.containsKey(command)) {
            return commandToCommandInfo.get(command);
        }
        return null;
    }

    @Override
    public List<String> getAllCommand(boolean isWasm, boolean isAuthOpen) {
        return commandToCommandInfo
                .keySet()
                .stream()
                .filter(
                        key ->
                                !(isWasm && !commandToCommandInfo.get(key).isWasmSupport()
                                        || (!isAuthOpen
                                                && commandToCommandInfo.get(key).isNeedAuthOpen())))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, CommandInfo> getAllCommandInfo(boolean isWasm) {
        return commandToCommandInfo;
    }

    public static final CommandInfo UPDATE_PROPOSAL =
            new CommandInfo(
                    "updateGovernorProposal",
                    "Create a proposal to committee, which attempt to update a governor.",
                    HelpInfo::updateGovernorProposalHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().createUpdateGovernorProposal(params),
                    2,
                    2,
                    false,
                    false,
                    true);

    public static final CommandInfo SET_RATE_PROPOSAL =
            new CommandInfo(
                    "setRateProposal",
                    "Create a proposal to committee, which attempt to update committee vote rate.",
                    HelpInfo::setRateProposalHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().createSetRateProposal(params),
                    2,
                    2,
                    false,
                    false,
                    true);

    public static final CommandInfo SET_DEPLOY_AUTH_TYPE_PROPOSAL =
            new CommandInfo(
                    "setDeployAuthTypeProposal",
                    "Create a proposal to committee, which attempt to set deploy ACL type globally.",
                    HelpInfo::setDeployAuthTypeProposalHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getAuthFace()
                                    .createSetDeployAuthTypeProposal(params),
                    1,
                    1,
                    false,
                    false,
                    true);

    public static final CommandInfo OPEN_DEPLOY_ACL_PROPOSAL =
            new CommandInfo(
                    "openDeployAuthProposal",
                    "Create a proposal to committee, which attempt to open deploy ACL for specific account.",
                    HelpInfo::openDeployAuthProposalHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().createOpenDeployAuthProposal(params),
                    1,
                    1,
                    false,
                    false,
                    true);

    public static final CommandInfo CLOSE_DEPLOY_ACL_PROPOSAL =
            new CommandInfo(
                    "closeDeployAuthProposal",
                    "Create a proposal to committee, which attempt to close deploy ACL for specific account.",
                    HelpInfo::closeDeployAuthProposalHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().createCloseDeployAuthProposal(params),
                    1,
                    1,
                    false,
                    false,
                    true);

    public static final CommandInfo RESET_ADMIN_PROPOSAL =
            new CommandInfo(
                    "resetAdminProposal",
                    "Create a proposal to committee, which attempt to reset a specific contract's admin.",
                    HelpInfo::resetAdminProposalHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().createResetAdminProposal(params),
                    2,
                    2,
                    false,
                    false,
                    true);

    public static final CommandInfo SET_NODE_WEIGHT_PROPOSAL =
            new CommandInfo(
                    "setConsensusNodeWeightProposal",
                    "Create a proposal to committee, which attempt to set a consensus node's weight.",
                    HelpInfo::setNodeWeightProposalHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getAuthFace()
                                    .createSetConsensusWeightProposal(params),
                    2,
                    2,
                    false,
                    false,
                    true);

    public static final CommandInfo ADD_SEALER_PROPOSAL =
            new CommandInfo(
                    "addSealerProposal",
                    "Create a proposal to committee, which attempt to add new consensus sealer node.",
                    HelpInfo::addSealerProposalHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().createAddSealerProposal(params),
                    2,
                    2,
                    false,
                    false,
                    true);

    public static final CommandInfo ADD_OBSERVER_PROPOSAL =
            new CommandInfo(
                    "addObserverProposal",
                    "Create a proposal to committee, which attempt to add new consensus observer node.",
                    HelpInfo::addObserverProposalHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().createAddObserverProposal(params),
                    1,
                    1,
                    false,
                    false,
                    true);

    public static final CommandInfo REMOVE_NODE_PROPOSAL =
            new CommandInfo(
                    "removeNodeProposal",
                    "Create a proposal to committee, which attempt to remove a consensus node.",
                    HelpInfo::removeNodeProposalHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().createRemoveNodeProposal(params),
                    1,
                    1,
                    false,
                    false,
                    true);

    public static final CommandInfo SET_SYS_CONFIG_PROPOSAL =
            new CommandInfo(
                    "setSysConfigProposal",
                    "Create a proposal to committee, which attempt to set system config.",
                    HelpInfo::setSysConfigProposalHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getAuthFace()
                                    .createSetSysConfigProposal(consoleInitializer, params),
                    2,
                    3,
                    false,
                    false,
                    true);

    public static final CommandInfo UPGRADE_VOTE_PROPOSAL =
            new CommandInfo(
                    "upgradeVoteProposal",
                    "Create a proposal to committee, which attempt to upgrade committee vote compute logic.",
                    HelpInfo::upgradeVoteProposalHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getAuthFace()
                                    .createUpgradeVoteComputerProposal(params),
                    1,
                    1,
                    false,
                    false,
                    true);

    public static final CommandInfo REVOKE_PROPOSAL =
            new CommandInfo(
                    "revokeProposal",
                    "Revoke a specific proposal from committee.",
                    HelpInfo::revokeProposalHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().revokeProposal(params),
                    1,
                    1,
                    false,
                    false,
                    true);

    public static final CommandInfo VOTE_PROPOSAL =
            new CommandInfo(
                    "voteProposal",
                    "Vote a specific proposal to committee.",
                    HelpInfo::voteProposalHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().voteProposal(params),
                    1,
                    2,
                    false,
                    false,
                    true);

    public static final CommandInfo GET_PROPOSAL_INFO =
            new CommandInfo(
                    "getProposalInfo",
                    "Get a specific proposal info or proposal info list from committee.",
                    HelpInfo::getProposalInfoHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().getProposalInfoList(params),
                    1,
                    2,
                    false,
                    false,
                    true);

    public static final CommandInfo GET_COMMITTEE_INFO =
            new CommandInfo(
                    "getCommitteeInfo",
                    "Get committee info.",
                    HelpInfo::getCommitteeInfoHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().getCommitteeInfo(params),
                    0,
                    0,
                    false,
                    false,
                    true);

    public static final CommandInfo GET_CONTRACT_ADMIN =
            new CommandInfo(
                    "getContractAdmin",
                    "Get admin address from specific contract.",
                    HelpInfo::getContractAdminHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().getContractAdmin(params),
                    1,
                    1,
                    false,
                    false,
                    true);

    public static final CommandInfo GET_DEPLOY_AUTH =
            new CommandInfo(
                    "getDeployAuth",
                    "Get deploy ACL strategy globally.",
                    HelpInfo::getDeployAuthHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().getDeployStrategy(params),
                    0,
                    0,
                    false,
                    false,
                    true);

    public static final CommandInfo CHECK_DEPLOY_AUTH =
            new CommandInfo(
                    "checkDeployAuth",
                    "Check whether account has deploy access.",
                    HelpInfo::checkDeployAuthHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getAuthFace()
                                    .checkDeployAuth(consoleInitializer, params),
                    0,
                    1,
                    false,
                    false,
                    true);

    public static final CommandInfo SET_METHOD_AUTH_TYPE =
            new CommandInfo(
                    "setMethodAuth",
                    "Set a method ACL type in specific contract.",
                    HelpInfo::setMethodAuthHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getAuthFace()
                                    .setMethodAuthType(consoleInitializer, params),
                    3,
                    3,
                    false,
                    false,
                    true);

    public static final CommandInfo OPEN_METHOD_AUTH =
            new CommandInfo(
                    "openMethodAuth",
                    "Open method ACL for account in specific contract.",
                    HelpInfo::openMethodAuthHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getAuthFace()
                                    .openMethodAuth(consoleInitializer, params),
                    3,
                    3,
                    false,
                    false,
                    true);

    public static final CommandInfo CLOSE_METHOD_AUTH =
            new CommandInfo(
                    "closeMethodAuth",
                    "Close method ACL for account in specific contract.",
                    HelpInfo::closeMethodAuthHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getAuthFace()
                                    .closeMethodAuth(consoleInitializer, params),
                    3,
                    3,
                    false,
                    false,
                    true);

    public static final CommandInfo CHECK_METHOD_AUTH =
            new CommandInfo(
                    "checkMethodAuth",
                    "Check method ACL for account in specific contract.",
                    HelpInfo::checkMethodAuthHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getAuthFace()
                                    .checkMethodAuth(consoleInitializer, params),
                    2,
                    3,
                    false,
                    false,
                    true);

    public static final CommandInfo GET_METHOD_AUTH =
            new CommandInfo(
                    "getMethodAuth",
                    "Get method ACL for account in specific contract.",
                    HelpInfo::getMethodAuthHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer
                                    .getAuthFace()
                                    .getMethodAuth(consoleInitializer, params),
                    2,
                    2,
                    false,
                    false,
                    true);

    public static final CommandInfo GET_LATEST_PROPOSAL =
            new CommandInfo(
                    "getLatestProposal",
                    "Get the latest proposal info.",
                    HelpInfo::getLatestProposalHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().getLatestProposal(params),
                    0,
                    0,
                    false,
                    false,
                    true);

    public static final CommandInfo FREEZE_CONTRACT =
            new CommandInfo(
                    "freezeContract",
                    "Freeze a specific contract.",
                    HelpInfo::freezeContractHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().freezeContract(params),
                    1,
                    1,
                    false,
                    false,
                    true);

    public static final CommandInfo UNFREEZE_CONTRACT =
            new CommandInfo(
                    "unfreezeContract",
                    "Unfreeze a specific contract.",
                    HelpInfo::unfreezeContractHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().unfreezeContract(params),
                    1,
                    1,
                    false,
                    false,
                    true);

    public static final CommandInfo ABOLISH_CONTRACT =
            new CommandInfo(
                    "abolishContract",
                    "Abolish a specific contract.",
                    HelpInfo::abolishContractHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().abolishContract(params),
                    1,
                    1,
                    false,
                    false,
                    true);

    public static final CommandInfo GET_CONTRACT_STATUS =
            new CommandInfo(
                    "getContractStatus",
                    "Get the status of the contract.",
                    HelpInfo::getContractStatusHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().getContractStatus(params),
                    1,
                    1,
                    false,
                    false,
                    true);

    public static final CommandInfo FREEZE_ACCOUNT =
            new CommandInfo(
                    "freezeAccount",
                    "Freeze a specific contract.",
                    HelpInfo::freezeAccountHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().freezeAccount(params),
                    1,
                    1,
                    false,
                    false,
                    true);

    public static final CommandInfo UNFREEZE_ACCOUNT =
            new CommandInfo(
                    "unfreezeAccount",
                    "Unfreeze a specific account.",
                    HelpInfo::unfreezeAccountHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().unfreezeAccount(params),
                    1,
                    1,
                    false,
                    false,
                    true);

    public static final CommandInfo ABOLISH_ACCOUNT =
            new CommandInfo(
                    "abolishAccount",
                    "Abolish a specific account.",
                    HelpInfo::abolishAccountHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().abolishAccount(params),
                    1,
                    1,
                    false,
                    false,
                    true);

    public static final CommandInfo GET_ACCOUNT_STATUS =
            new CommandInfo(
                    "getAccountStatus",
                    "Get the status of the account.",
                    HelpInfo::getAccountStatusHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().getAccountStatus(params),
                    1,
                    1,
                    false,
                    false,
                    true);

    public static final CommandInfo INIT_AUTH =
            new CommandInfo(
                    "initAuth",
                    "Initialize committee contract system.",
                    HelpInfo::initAuthHelp,
                    (consoleInitializer, params, pwd) ->
                            consoleInitializer.getAuthFace().initAuth(params),
                    1,
                    1,
                    false,
                    false,
                    false);

    static {
        Field[] fields = AuthOpCommand.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(CommandInfo.class)) {
                try {
                    CommandInfo constantCommandInfo = (CommandInfo) field.get(null);
                    commandToCommandInfo.put(constantCommandInfo.getCommand(), constantCommandInfo);
                    if (constantCommandInfo.getOptionCommand() != null) {
                        List<String> subCommandList = constantCommandInfo.getOptionCommand();
                        for (String s : subCommandList) {
                            commandToCommandInfo.put(s, constantCommandInfo);
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
