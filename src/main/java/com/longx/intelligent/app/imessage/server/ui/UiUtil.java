package com.longx.intelligent.app.imessage.server.ui;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by LONG on 2024/12/2 at 11:20 PM.
 */
public class UiUtil {

    public static BufferedImage svgToImage(String svgFilePath) throws Exception {
        return svgToImage(svgFilePath, -1, -1);
    }

    public static BufferedImage svgToImage(String svgFilePath, int width, int height) throws Exception {
        try {
            String parserClassName = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parserClassName);
            InputStream inputStream = UiUtil.class.getResourceAsStream(svgFilePath);
            if (inputStream == null) {
                throw new FileNotFoundException("SVG file not found: " + svgFilePath);
            }
            SVGDocument document = factory.createSVGDocument(null, inputStream);
            return renderSVGToBufferedImage(document, width, height);
        } catch (Exception e) {
            throw e;
        }
    }

    public static BufferedImage renderSVGToBufferedImage(SVGDocument document, int width, int height) throws Exception {
        try {
            PNGTranscoder transcoder = new PNGTranscoder();
            if(width != -1) transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float) width);
            if(height != -1) transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) height);
            TranscoderInput input = new TranscoderInput(document);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            TranscoderOutput output = new TranscoderOutput(byteArrayOutputStream);
            transcoder.transcode(input, output);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            return ImageIO.read(byteArrayInputStream);
        } catch (Exception e) {
            throw e;
        }
    }


}
