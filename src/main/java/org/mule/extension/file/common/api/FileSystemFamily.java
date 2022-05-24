package org.mule.extension.file.common.api;

import java.nio.file.Paths;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public enum FileSystemFamily {
    LINUX{
        @Override
        Predicate<String> getPredicate() {
            return null;
        }
    },
    WINDOWS{
        @Override
        Predicate<String> getPredicate(final String pattern) {
            final Pattern p = Pattern.compile("[a-zA-Z]:\\\\(?:([^<>:\"\\/\\\\|?*]*[^<>:\"\\/\\\\|?*.]\\\\|..\\\\)*([^<>:\"\\/\\\\|?*]*[^<>:\"\\/\\\\|?*.]\\\\?|..\\\\))?");
            p.asPredicate();
            return path -> matcher.matches(Paths.get(path));
        }
    },
     DEFAULT {
        @Override
        Predicate<String> getPredicate() {
            return null;
        }
    }

    abstract Predicate<String> getPredicate(final String pattern);
}
