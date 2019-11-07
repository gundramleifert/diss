package de.planet.diss_leifert.util;

import de.planet.math.geom2d.types.Polygon2DInt;
import de.planet.math.geom2d.types.Rectangle2DInt;
import de.planet.xml_helper.types.XLine;
import de.planet.xml_helper.types.XPage;
import de.planet.xml_helper.types.XRegion;
import de.uros.citlab.errorrate.util.HeatMapUtil;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

//import java.awt.*;

public class ImageUtil {
    public static BufferedImage getDebugImage(BufferedImage pageImg, XPage pageResults, double fontHeight, boolean overlay, boolean drawRegion, boolean drawBaseline, boolean drawPolygon, boolean skipConfidences) {
        return getDebugImage(pageImg, pageResults, fontHeight, overlay, drawRegion, drawBaseline ? 0.0 : -1.0, drawPolygon, skipConfidences);
    }

    private static List<XLine> getLines(XPage page) {
        List<XLine> res = new LinkedList<>();
        for (XRegion region : page.getRegions()) {
            res.addAll(region.getLines());
        }

        return res;
    }

    public static BufferedImage getDebugImage(BufferedImage pageImg, XPage pageResults, double fontHeight, boolean overlay, boolean drawRegion, double drawBaseline, boolean drawPolygon, boolean skipConfidences) {
        BufferedImage bi_res = new BufferedImage(pageImg.getWidth() * (overlay || fontHeight <= 0 ? 1 : 2), pageImg.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics graphics = bi_res.getGraphics();
        graphics.drawImage(pageImg, 0, 0, Color.WHITE, null);
//        bi_res.flush();
        printPolygons(bi_res, pageResults, drawRegion, drawBaseline, drawPolygon);
        if (fontHeight > 0) {
            double sum = 0.0, factor = 0.0;
            List<XLine> textLines = getLines(pageResults);
            for (XLine textLine : textLines) {
//                List<PropertyType> property = textLine.getProperty();
                if (textLine.getText() != null && !textLine.getText().isEmpty()) {
                    factor += textLine.getText().length();
                    sum += textLine.getBaseLine().getBounds().w;
                }
            }
            sum /= factor;
            int meanLineHeight = (int) Math.round(1.7 * sum * fontHeight);
            Font stringFont = new Font("SansSerif", Font.PLAIN, meanLineHeight);
            graphics.setFont(stringFont);
            graphics.setColor(Color.WHITE);
            int offset = overlay ? 0 : pageImg.getWidth();
            for (XLine textLine : textLines) {
                String result = textLine.getText();
                if (result == null || result.isEmpty()) {
                    continue;
                }
                String confidence = String.format("%.3f", textLine.getTextConf());
//                if(drawBaseline>0.0){
//                    if(confidence.equals("?")||Double.valueOf(confidence)<drawBaseline){
//                        continue;
//                    }
//                }
                Rectangle2DInt bounds = textLine.getCoords().getBounds();
                Rectangle2D rect = new Rectangle(bounds.x, bounds.y, bounds.w, bounds.h);
//            result = result.replace("\u017f", "s");
                graphics.drawString(textLine.getText() + (skipConfidences ? "" : "(" + confidence + ")"),
                        (int) (offset + rect.getMinX()),
                        (int) (rect.getMinY() + Math.round(0.8 * rect.getHeight())));
            }
        }
        return bi_res;
    }

    public static void printPolygons(BufferedImage image, XPage page, boolean drawRegion, boolean drawBaseline, boolean drawPolygon) {
        printPolygons(image, page, drawRegion, drawBaseline ? 0.0 : -1.0, drawPolygon);
    }

    public static void printPolygons(BufferedImage image, XPage page, boolean drawRegion, double drawBaseline, boolean drawPolygon) {
        if (image == null) {
            throw new RuntimeException("image is null");
        }
        if (page == null) {
            throw new RuntimeException("page is null");
        }
//        BufferedImage imageBufferedImage = copy(image);
        Graphics graphics = image.getGraphics();
        Graphics2D g2 = (Graphics2D) graphics.create();
        for (XRegion reg : page.getRegions()) {
            if (drawRegion) {
                g2.setStroke(new BasicStroke(8));
                g2.setColor(Color.yellow);
                Polygon2DInt coords = reg.getCoords();
                g2.drawPolygon(coords.xpoints, coords.ypoints, coords.npoints);
            }
            g2.setStroke(new BasicStroke(5));
            for (XLine line : reg.getLines()) {
                if (drawPolygon && line.getCoords() != null) {
                    g2.setColor(Color.blue);
                    try {
                        Polygon2DInt coords = line.getCoords();
                        g2.drawPolygon(coords.xpoints, coords.ypoints, coords.npoints);
                    } catch (RuntimeException ex) {
//                        LOG.info("cannot draw polygon of line {} because coords are {}", line.getId(), line.getCoords().getPoints());
                    }
                }
                if (line.getBaseLine() != null) {
                    g2.setColor(drawBaseline >= 0.0 && line.getText() != null && line.getTextConf() >= drawBaseline ? Color.GREEN : Color.RED);
//                    g2.setColor(Color.red);
                    Polygon2DInt coords = line.getBaseLine();
                    g2.drawPolyline(coords.xpoints, coords.ypoints, coords.npoints);
                }
            }
        }
    }

}
