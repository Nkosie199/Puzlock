package puzlock.cli;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Positional arguments and {@code --name value} options. */
final class Arguments {
    private final List<String> positional = new ArrayList<>();
    private final Map<String, String> options = new HashMap<>();

    Arguments(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--")) {
                if (i + 1 >= args.length) {
                    throw new UsageException("Option " + args[i] + " needs a value");
                }
                options.put(args[i].substring(2), args[++i]);
            } else {
                positional.add(args[i]);
            }
        }
    }

    Optional<String> positional(int index) {
        return index < positional.size() ? Optional.of(positional.get(index)) : Optional.empty();
    }

    Optional<String> option(String name) {
        return Optional.ofNullable(options.get(name));
    }

    int intOption(String name, int fallback) {
        return option(name).map(value -> parseInt(name, value)).orElse(fallback);
    }

    double doubleOption(String name, double fallback) {
        return option(name).map(value -> parseDouble(name, value)).orElse(fallback);
    }

    private static double parseDouble(String name, String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new UsageException("--" + name + " must be a number, got '" + value + "'");
        }
    }

    private static int parseInt(String name, String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new UsageException("--" + name + " must be a whole number, got '" + value + "'");
        }
    }

    static final class UsageException extends RuntimeException {
        UsageException(String message) {
            super(message);
        }
    }
}
