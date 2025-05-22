package com.longx.intelligent.app.imessage.server.util;

import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

import java.io.File;
import java.io.IOException;

/**
 * Created by LONG on 2024/4/16 at 10:47 AM.
 */
public class FileUtil {

    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex != -1) {
            return fileName.substring(lastDotIndex).toLowerCase();
        } else {
            return "";
        }
    }

    public static String detectFileExtension(byte[] bytes) {
        Tika tika = new Tika();
        String mimeType;
        mimeType = tika.detect(bytes);
        MimeTypes defaultMimeTypes = MimeTypes.getDefaultMimeTypes();
        MimeType mimeTypeObj;
        try {
            mimeTypeObj = defaultMimeTypes.forName(mimeType);
        } catch (MimeTypeException e) {
            e.printStackTrace();
            return "";
        }
        String extension = mimeTypeObj.getExtension();
        if (extension.startsWith(".")) {
            extension = extension.substring(1);
        }
        return extension;
    }

    public static String detectFileExtension(File file) {
        Tika tika = new Tika();
        String mimeType;
        try {
            mimeType = tika.detect(file);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        MimeTypes defaultMimeTypes = MimeTypes.getDefaultMimeTypes();
        MimeType mimeTypeObj;
        try {
            mimeTypeObj = defaultMimeTypes.forName(mimeType);
        } catch (MimeTypeException e) {
            e.printStackTrace();
            return "";
        }
        String extension = mimeTypeObj.getExtension();
        if (extension.startsWith(".")) {
            extension = extension.substring(1);
        }
        return extension;
    }
}
