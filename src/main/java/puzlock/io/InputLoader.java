package puzlock.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

/** Turns any supported input file into a voxel grid, voxelising meshes at the given resolution. */
public final class InputLoader {
    public static final int DEFAULT_RESOLUTION = 16;

    private InputLoader() {
    }

    public static LoadedInput load(Path file, int resolution) throws IOException {
        return load(file.getFileName().toString(), Files.readAllBytes(file), resolution);
    }

    public static LoadedInput load(String fileName, byte[] content, int resolution) {
        if (content.length == 0) {
            throw new InvalidInputException("\"" + fileName + "\" is empty");
        }
        return switch (formatOf(fileName, content)) {
            case STL -> fromMesh(StlReader.read(content), resolution);
            case OBJ -> fromMesh(ObjReader.read(text(content)), resolution);
            case GRID -> new LoadedInput(GridFormat.read(text(content)), Optional.empty());
            case UNKNOWN -> throw new InvalidInputException("Unsupported input \"" + fileName
                    + "\": expected a Puzlock grid, .stl or .obj file");
        };
    }

    private static LoadedInput fromMesh(TriangleMesh mesh, int resolution) {
        SpeckFilter.Result cleaned = SpeckFilter.keepMainBody(Voxeliser.voxelise(mesh, resolution));
        return new LoadedInput(cleaned.grid(), cleaned.note());
    }

    private enum Format { STL, OBJ, GRID, UNKNOWN }

    private static Format formatOf(String fileName, byte[] content) {
        String name = fileName.toLowerCase(Locale.ROOT);
        if (name.endsWith(".stl")) {
            return Format.STL;
        }
        if (name.endsWith(".obj")) {
            return Format.OBJ;
        }
        return looksLikeGrid(content) ? Format.GRID : Format.UNKNOWN;
    }

    private static boolean looksLikeGrid(byte[] content) {
        for (byte b : content) {
            boolean allowed = (b >= '0' && b <= '9') || b == ',' || b == '\n' || b == '\r' || b == ' ' || b == '\t';
            if (!allowed) {
                return false;
            }
        }
        return true;
    }

    private static String text(byte[] content) {
        return new String(content, StandardCharsets.UTF_8);
    }
}
