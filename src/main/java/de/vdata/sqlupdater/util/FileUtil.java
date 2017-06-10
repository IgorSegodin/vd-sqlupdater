package de.vdata.sqlupdater.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author isegodin
 */
public class FileUtil {

    /**
     * Removes end slash from given path.
     * @param path path with optional slash at the end.
     * @return path without slash at the end.
     */
    public static String getPathWithoutEndSlash(String path) {
        return path != null ? path.replaceAll("^(.*)[\\\\/]$", "$1") : null;
    }

    /**
     * Trims path till last slash.
     * @param path path to file or folder.
     * @return Path to folder without slash at the end.
     */
    public static String getPathToFolder(String path) {
        String fixedPath = path.replaceAll("/+", "/").replaceAll("\\+", "\\").trim();

        int slashIdx = fixedPath.lastIndexOf("/");
        if (slashIdx == -1) {
            slashIdx = fixedPath.lastIndexOf("\\");
        }
        if (slashIdx > 0) {
            path = path.substring(0, slashIdx + 1);
            return getPathWithoutEndSlash(path);
        } else if (slashIdx == 0) {
            return "/";
        } else {
            return path;
        }
    }

    /**
     * Write input to output without closing streams.
     * @param in input
     * @param out output
     */
    public static void writeStream(InputStream in, OutputStream out) {
        try {
            byte[] buffer = new byte[1024];
            int read;
            while (-1 != (read = in.read(buffer))) {
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}