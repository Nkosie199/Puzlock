package puzlock.web;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/** The sample inputs in a folder; only files actually listed there can be read (no path tricks). */
final class Samples {
    private final Path folder;

    Samples(Path folder) {
        this.folder = folder;
    }

    List<String> names() throws IOException {
        if (!Files.isDirectory(folder)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(folder)) {
            return files.filter(Files::isRegularFile).map(file -> file.getFileName().toString()).sorted().toList();
        }
    }

    Optional<byte[]> read(String name) throws IOException {
        if (!names().contains(name)) {
            return Optional.empty();
        }
        return Optional.of(Files.readAllBytes(folder.resolve(name)));
    }
}
