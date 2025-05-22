package com.longx.intelligent.app.imessage.server.util;

import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AudioUtil {

    public static long getAudioDurationSec(byte[] audioBytes) {
        File tempFile = null;
        try {
            tempFile = convertBytesToFile(audioBytes, "tmpChatAudioFile." + FileUtil.detectFileExtension(audioBytes));
            try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(tempFile)){
                grabber.start();
                long duration = grabber.getLengthInTime() / (1000 * 1000);
                grabber.stop();
                return duration;
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    private static File convertBytesToFile(byte[] bytes, String tempFilePath) throws IOException {
        File tempFile = new File(tempFilePath);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(bytes);
        }
        return tempFile;
    }
}
