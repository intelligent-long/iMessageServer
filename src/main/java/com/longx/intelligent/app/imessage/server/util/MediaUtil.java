package com.longx.intelligent.app.imessage.server.util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.longx.intelligent.app.imessage.server.data.Size;
import com.longx.intelligent.app.imessage.server.data.VideoInfo;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.UUID;

/**
 * Created by LONG on 2024/8/8 at 5:36 AM.
 */
public class MediaUtil {

    public static Size getImageSize(InputStream is) throws IOException {
        byte[] bytes = is.readAllBytes();
        InputStream imageStream = new ByteArrayInputStream(bytes);

        ExifIFD0Directory directory;
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(imageStream);
            directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        } catch (ImageProcessingException e) {
            throw new RuntimeException(e);
        }
        imageStream = new ByteArrayInputStream(bytes);
        BufferedImage bufferedImage = ImageIO.read(imageStream);
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        if (directory != null) {
            Integer orientation = directory.getInteger(ExifIFD0Directory.TAG_ORIENTATION);
            if (orientation != null) {
                switch (orientation) {
                    case 6:
                    case 8:
                        int temp = width;
                        width = height;
                        height = temp;
                        break;
                    case 3:
                    default:
                        break;
                }
            }
        }
        return new Size(width, height);
    }

    public static VideoInfo getVideoInfo(InputStream videoStream, String extension) throws IOException {
        File tempFile = File.createTempFile("video_temp_" + UUID.randomUUID(), "." + extension);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = videoStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(tempFile)) {
            grabber.start();
            int width = grabber.getImageWidth();
            int height = grabber.getImageHeight();
            long durationInMicroseconds = grabber.getLengthInTime();
            grabber.stop();
            return new VideoInfo(new Size(width, height), durationInMicroseconds / 1000);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get video size", e);
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

}
