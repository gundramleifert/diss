package de.planet.diss_leifert.types;


import com.achteck.misc.log.Logger;
import com.achteck.misc.types.ConfMat;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author richard
 */
public class ConfMatStreamUtil {

    private static final Logger LOG = Logger.getLogger(ConfMatStreamUtil.class);

    public static List<ConfMat> readConfmatsFromStream(File stream) {
        try {
            FileInputStream fileInputStream = new FileInputStream(stream);
            byte[] bFile = new byte[(int) stream.length()];

            //read file into bytes[]
            fileInputStream = new FileInputStream(stream);
            fileInputStream.read(bFile);
            return getConfmatsFromStream(bFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<ConfMat> getConfmatsFromStream(byte[] stream) {
        List<ConfMat> confmats = new ArrayList<>();
        ShortBuffer shortBuffer = ByteBuffer.wrap(stream).order(ByteOrder.nativeOrder()).asShortBuffer();
        while (shortBuffer.position() < shortBuffer.limit()) {
            short[] sa = ConfMat.shortArrayfromShortBuffer(shortBuffer);
            confmats.add(ConfMat.fromShortArray(sa, false));
        }
        return confmats;
    }

    public static byte[] getStreamFromConfmats(List<ConfMat> confmats) {
        return getStreamFromConfmats(confmats, false, false);
    }

    public static void writeStreamFromConfmats(List<ConfMat> confmats, File stream) {
        byte[] streamFromConfmats = getStreamFromConfmats(confmats);
        FileOutputStream fileOuputStream = null;
        try {
            fileOuputStream = new FileOutputStream(stream);
            fileOuputStream.write(streamFromConfmats);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileOuputStream != null) {
                try {
                    fileOuputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static byte[] getStreamFromConfmats(List<ConfMat> confmats, boolean toUpper, boolean triggerLeastCost) {
        byte[] stream = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            for (ConfMat cm : confmats) {
                if (cm == null) {
                    continue;
                }
                if (toUpper) {
                    cm = cm.toUpper();
                }
                if (triggerLeastCost) {
                    cm.getLeastCosts(""); //trigger creation of least cost vector to also put it into stream
                }
                cm.toShortStream(baos);
            }
            stream = baos.toByteArray();
        } catch (IOException ex) {
            LOG.log(Logger.ERROR, ex.getMessage(), ex);
        }
        return stream;
    }

    public static ConfMat getConfmatFromStream(byte[] stream) {
        ShortBuffer shortBuffer = ByteBuffer.wrap(stream).order(ByteOrder.nativeOrder()).asShortBuffer();
        short[] sa = ConfMat.shortArrayfromShortBuffer(shortBuffer);
        return ConfMat.fromShortArray(sa);
    }

    public static byte[] getStreamFromConfmat(ConfMat confmat) {
        return getStreamFromConfmat(confmat, false, false);
    }

    public static byte[] getStreamFromConfmat(ConfMat cm, boolean toUpper, boolean triggerLeastCost) {
        byte[] stream = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            if (cm != null) {
                if (toUpper) {
                    cm = cm.toUpper();
                }
                if (triggerLeastCost) {
                    cm.getLeastCosts(""); //trigger creation of least cost vector to also put it into stream
                }
                cm.toShortStream(baos);
            }
            stream = baos.toByteArray();
        } catch (IOException ex) {
            LOG.log(Logger.ERROR, ex.getMessage(), ex);
        }
        return stream;
    }

}
