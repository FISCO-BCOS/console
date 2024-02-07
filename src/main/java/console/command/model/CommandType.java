package console.command.model;

public enum CommandType {
    BASIC_CMD,
    CONTRACT_OP,
    STATUS_QUERY,
    CONSENSUS_OP,
    CRUD_OP,
    BFS_OP,
    GROUP_QUERY,
    AUTH_OP,
    ACCOUNT_OP,
    COLLABORATE_OP,
    SHARDING_OP,
    BALANCE_PRECOMPILED_OP;

    @Override
    public String toString() {
        switch (this) {
            case BASIC_CMD:
                return "Basic Command";
            case CONTRACT_OP:
                return "Contract Operation";
            case STATUS_QUERY:
                return "Blockchain Status Query";
            case CONSENSUS_OP:
                return "Consensus Operation";
            case CRUD_OP:
                return "CRUD Contract Operation";
            case BFS_OP:
                return "BFS Operation";
            case GROUP_QUERY:
                return "Group Info Query";
            case AUTH_OP:
                return "Authority Operation";
            case ACCOUNT_OP:
                return "Account Operation";
            case COLLABORATE_OP:
                return "Wasm Collaboration Operation";
            case SHARDING_OP:
                return "Sharding Operation";
            case BALANCE_PRECOMPILED_OP:
                return "Balance Precompiled Operation";
            default:
                return "Unknown Command";
        }
    }
}
