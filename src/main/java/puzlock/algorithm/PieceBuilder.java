package puzlock.algorithm;

import java.util.List;
import java.util.Optional;
import puzlock.model.Connectivity;

/**
 * The steps shared by the key and every later piece once an initial piece exists: block the
 * directions it must not move in, protect that blockage with anchors, expand to ~m voxels and
 * confirm (Song §5.1 steps 3–5, §5.2 steps 3–5).
 */
final class PieceBuilder {

    /**
     * Song anchors the seed's existing blockage before blocking the rest. In small remaining
     * volumes those anchor zones can leave no room to block, so the other policies are fallbacks;
     * they are safe because expansion and confirmation test the requirements explicitly.
     */
    private enum AnchorPolicy { BEFORE_BLOCKING, AFTER_BLOCKING, NONE }

    private PieceBuilder() {
    }

    static ExtractionResult build(ExtractionContext context, Draft initial, List<BlockingRequirement> requirements) {
        FailureReason lastFailure = FailureReason.CANNOT_BLOCK;
        for (AnchorPolicy policy : AnchorPolicy.values()) {
            Optional<Draft> blocked = blockAll(context, initial, requirements, policy);
            if (blocked.isEmpty()) {
                continue;
            }
            blocked = FragmentAbsorber.absorb(context, blocked.get(), requirements);
            if (blocked.isEmpty()) {
                lastFailure = FailureReason.FRAGMENTS_REMAINDER;
                continue;
            }
            for (int expansion = 0; expansion < context.options().expansionsPerSeed(); expansion++) {
                Draft expanded = PieceExpander.expand(context, blocked.get(), requirements);
                Optional<FailureReason> problem = confirm(context, expanded, requirements);
                if (problem.isEmpty()) {
                    return ExtractionResult.success(expanded, context.moveDirection());
                }
                lastFailure = problem.get();
            }
        }
        return ExtractionResult.failure(lastFailure);
    }

    private static Optional<Draft> blockAll(ExtractionContext context, Draft initial, List<BlockingRequirement> requirements,
                                            AnchorPolicy policy) {
        List<BlockingRequirement> initiallyMet = requirements.stream()
                .filter(requirement -> requirement.isMetBy(context, initial.cells()))
                .toList();
        Draft draft = policy == AnchorPolicy.BEFORE_BLOCKING ? protectAll(context, initial, initiallyMet) : initial;
        for (BlockingRequirement requirement : requirements) {
            if (!requirement.isMetBy(context, draft.cells())) {
                Optional<Draft> extended = BlockingStrategy.block(context, draft, requirement);
                if (extended.isEmpty()) {
                    return Optional.empty();
                }
                draft = policy == AnchorPolicy.NONE ? extended.get() : Anchors.protect(context, extended.get(), requirement);
            }
        }
        if (policy == AnchorPolicy.AFTER_BLOCKING) {
            draft = protectAll(context, draft, initiallyMet);
        }
        Draft finalDraft = draft;
        boolean allMet = requirements.stream().allMatch(requirement -> requirement.isMetBy(context, finalDraft.cells()));
        return allMet ? Optional.of(draft) : Optional.empty();
    }

    private static Draft protectAll(ExtractionContext context, Draft draft, List<BlockingRequirement> requirements) {
        Draft protectedDraft = draft;
        for (BlockingRequirement requirement : requirements) {
            protectedDraft = Anchors.protect(context, protectedDraft, requirement);
        }
        return protectedDraft;
    }

    /** Song §5.1 step 5 plus explicit blocking and removal checks, which are cheap at piece scale. */
    private static Optional<FailureReason> confirm(ExtractionContext context, Draft draft, List<BlockingRequirement> requirements) {
        var remainder = draft.remainderOf(context.volume());
        if (!Connectivity.isConnected(context.shape(), draft.cells()) || !Connectivity.isConnected(context.shape(), remainder)) {
            return Optional.of(FailureReason.FRAGMENTS_REMAINDER);
        }
        if (!requirements.stream().allMatch(requirement -> requirement.isMetBy(context, draft.cells()))) {
            return Optional.of(FailureReason.CANNOT_BLOCK);
        }
        if (!Mobility.isRemovable(context.shape(), draft.cells(), remainder, context.moveDirection())) {
            return Optional.of(FailureReason.NOT_REMOVABLE);
        }
        return Optional.empty();
    }
}
