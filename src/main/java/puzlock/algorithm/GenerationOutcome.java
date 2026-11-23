package puzlock.algorithm;

import java.time.Duration;
import puzlock.model.Puzzle;
import puzlock.verify.VerificationReport;

/** What {@link PuzzleGenerator} returns: a verified puzzle, or a message saying why none could be made. */
public sealed interface GenerationOutcome {

    Duration elapsed();

    record Success(Puzzle puzzle, VerificationReport verification, int attemptsUsed, Duration elapsed)
            implements GenerationOutcome {
    }

    record Failure(String message, Duration elapsed) implements GenerationOutcome {
    }
}
