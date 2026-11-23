package puzlock.io;

import java.util.Optional;
import puzlock.model.VoxelGrid;

/** The voxel grid read from an input file, plus a note when loading had to clean it up. */
public record LoadedInput(VoxelGrid grid, Optional<String> note) {
}
