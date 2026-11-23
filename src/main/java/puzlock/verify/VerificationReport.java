package puzlock.verify;

import java.util.List;

/** The verifier's verdict. {@code exhaustive} is false when large stages were checked by Song's local rules. */
public record VerificationReport(boolean valid, boolean exhaustive, List<String> problems) {

    public VerificationReport {
        problems = List.copyOf(problems);
    }

    public String summary() {
        if (valid) {
            return "Recursive interlocking verified (" + (exhaustive ? "exhaustive subset check" : "local interlocking check") + ")";
        }
        return "Not a valid recursive interlocking puzzle: " + problems.get(0);
    }
}
