package puzlock.io;

import java.util.BitSet;
import java.util.Optional;
import puzlock.model.Connectivity;
import puzlock.model.VoxelGrid;

/**
 * Voxelising a mesh leaves stray voxels that touch the body only at an edge or corner. This
 * keeps the largest face-connected part when everything else is a negligible speck; a shape
 * that genuinely has several parts is left alone, and the generator then reports it.
 */
final class SpeckFilter {
    static final double MAX_DROPPED_SHARE = 0.02;

    private SpeckFilter() {
    }

    record Result(VoxelGrid grid, Optional<String> note) {
    }

    static Result keepMainBody(VoxelGrid grid) {
        BitSet largest = largestComponent(grid);
        int dropped = grid.voxelCount() - largest.cardinality();
        if (dropped == 0 || dropped > MAX_DROPPED_SHARE * grid.voxelCount()) {
            return new Result(grid, Optional.empty());
        }
        String note = "Dropped " + dropped + " loose voxel" + (dropped == 1 ? "" : "s")
                + " that touched the shape only at an edge or corner";
        return new Result(new VoxelGrid(grid.shape(), largest), Optional.of(note));
    }

    private static BitSet largestComponent(VoxelGrid grid) {
        BitSet unvisited = grid.voxels();
        BitSet largest = new BitSet();
        for (int cell = unvisited.nextSetBit(0); cell >= 0; cell = unvisited.nextSetBit(cell + 1)) {
            BitSet component = Connectivity.floodFill(grid.shape(), unvisited, cell);
            unvisited.andNot(component);
            if (component.cardinality() > largest.cardinality()) {
                largest = component;
            }
        }
        return largest;
    }
}
