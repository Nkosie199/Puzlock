package puzlock.algorithm;

/**
 * Tuning for one generation run. Defaults follow Song et al.: β = 3 (range 1–6), Nb1 = 50
 * nearest blockee/blocking pairs, Nb2 = 10 kept by accessibility, ten shortlisted seeds.
 */
public record GenerationOptions(
        int pieceCount,
        long seed,
        int attempts,
        double beta,
        int nearestPairs,
        int candidatePairs,
        int seedsPerPiece,
        int expansionsPerSeed) {

    public static final int DEFAULT_ATTEMPTS = 40;

    public static GenerationOptions defaults(int pieceCount) {
        return new GenerationOptions(pieceCount, 1L, DEFAULT_ATTEMPTS, 3.0, 50, 10, 10, 5);
    }

    public GenerationOptions withSeed(long newSeed) {
        return new GenerationOptions(pieceCount, newSeed, attempts, beta, nearestPairs, candidatePairs,
                seedsPerPiece, expansionsPerSeed);
    }

    public GenerationOptions withAttempts(int newAttempts) {
        return new GenerationOptions(pieceCount, seed, newAttempts, beta, nearestPairs, candidatePairs,
                seedsPerPiece, expansionsPerSeed);
    }
}
