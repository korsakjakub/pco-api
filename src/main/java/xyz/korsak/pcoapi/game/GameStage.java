package xyz.korsak.pcoapi.game;

public enum GameStage {
    SMALL_BLIND,
    BIG_BLIND,
    PRE_FLOP,
    FLOP,
    TURN,
    RIVER,
    SHOWDOWN;

    public GameStage next() {
        int currentStageOrdinal = this.ordinal();
        GameStage[] stages = GameStage.values();
        int nextStageOrdinal = (currentStageOrdinal + 1) % stages.length;
        return stages[nextStageOrdinal];
    }
}
