package puzlock.algorithm;

import java.util.Optional;
import puzlock.model.Direction;

/** Either a confirmed piece with its move direction, or the reason no piece could be made. */
record ExtractionResult(Optional<Draft> draft, Direction direction, FailureReason failure) {

    static ExtractionResult success(Draft draft, Direction direction) {
        return new ExtractionResult(Optional.of(draft), direction, null);
    }

    static ExtractionResult failure(FailureReason reason) {
        return new ExtractionResult(Optional.empty(), null, reason);
    }

    boolean succeeded() {
        return draft.isPresent();
    }
}
