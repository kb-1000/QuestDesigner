package questdesigner;

import java.nio.file.Path;

public final class ExportJavaSettings {
    private final Path path;
    private final String packageName;
    private final String className;

    public Path getPath() {
        return path;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public ExportJavaSettings(final Path path, final String packageName, final String className) {
        this.path = path;
        this.packageName = packageName;
        this.className = className;
    }
}
