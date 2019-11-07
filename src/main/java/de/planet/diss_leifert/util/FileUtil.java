package de.planet.diss_leifert.util;

import java.io.File;
import java.nio.file.Path;

public class FileUtil {
    public static File getTgtFile(Path srcFolder, Path tgtFolder, File srcFile) {
        return new File(tgtFolder.toFile(), srcFolder.relativize(srcFile.toPath()).toString());
    }

    public static File getTgtFile(File srcFolder, File tgtFolder, File srcFile) {
        return getTgtFile(srcFolder.getAbsoluteFile().toPath(), tgtFolder.getAbsoluteFile().toPath(), srcFile.getAbsoluteFile());
    }

}
