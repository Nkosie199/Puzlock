package puzlock.algorithm;

/** Why a piece could not be extracted. Descriptions complete the sentence "Could not build piece i: …". */
enum FailureReason {
    NO_KEY_SEED("no voxel has a free top face plus exactly one free side face, so the key piece has no starting point"),
    NO_NEXT_SEED("no remaining voxel touches the previous piece across a side face"),
    CANNOT_BLOCK("every candidate could still slide out in a direction it must be blocked in"),
    FRAGMENTS_REMAINDER("every candidate split the remaining volume into separate parts"),
    NOT_REMOVABLE("every candidate was stuck in its own removal direction"),
    VERIFICATION_FAILED("the finished puzzle did not pass the interlocking check");

    private final String description;

    FailureReason(String description) {
        this.description = description;
    }

    String description() {
        return description;
    }
}
