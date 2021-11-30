package console.collaboration;

public interface CollaborationFace {

    void initialize(String[] params) throws Exception;

    void sign(String[] params) throws Exception;

    void exercise(String[] params) throws Exception;

    void fetch(String[] params) throws Exception;
}
