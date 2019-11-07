package de.planet.diss_leifert.util;

import com.achteck.misc.log.Logger;
import com.achteck.misc.util.StringIO2;
import de.planet.imaging.types.HybridImage;
import de.planet.imaging.types.StdFrameAppender;
import de.planet.math.geom2d.types.Polygon2DInt;
import de.planet.math.util.PolygonHelper;
import de.planet.trainset_util.util.IOOps;
import de.planet.trainset_util.util.Misc;
import de.planet.xml_helper.JsonHelper;
import de.planet.xml_helper.types.XFile;
import de.planet.xml_helper.types.XLine;
import de.planet.xml_helper.types.XPage;
import de.planet.xml_helper.types.XRegion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ShowImages {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(de.planet.trainset_util.util.Visualization.class.getName());


    private int angleVisMode = 0; // 0-show all angles, 1-show NOT 0.0 angles
    private double minBLConf = 0.0;
    private double minATRConf = 0.0;

    private boolean enforcePresentPolys = false;


    private Polygon2DInt getPlotPoly(Polygon2DInt inPoly) {
        Polygon2DInt outPoly = new Polygon2DInt();
        for (int i = 0; i < inPoly.npoints; i++) {
            outPoly.addPoint(inPoly.xpoints[i], inPoly.ypoints[i]);
        }
        for (int i = inPoly.npoints - 1; i >= 0; i--) {
            outPoly.addPoint(inPoly.xpoints[i], inPoly.ypoints[i]);
        }
        return outPoly;
    }


    private void addPlotPolys(XLine xline, List<Polygon2DInt> plotPolys) {
        boolean added = false;
//        for (XLine xline : xreg.getLines()) {
        if (xline.getTextConf() >= minATRConf) {
            if (xline.getBaseLine() != null) {
                plotPolys.add(getPlotPoly(xline.getBaseLine()));
                added = true;
            }
            if (xline.getCoords() != null) {
                plotPolys.add(xline.getCoords());
                added = true;
            }
        }
//        }
//        if ((added || xreg.getOrientationConf() == 1.0) && xreg.getCoords() != null) {
//            plotPolys.add(xreg.getCoords());
//        }
//        for (XRegion nreg : xreg.getRegions()) {
//            addPlotPolys(nreg, plotPolys);
//        }


    }


    private void run(Collection<File> listOfFiles) throws IOException {

//        ArrayList<String> aa = IO.loadLineArrayList("/home/tobi/tmp/angleChange.lst");
//        listOfFiles = new ArrayList<>();
//        for (String aAA : aa) {
//            listOfFiles.add(new File(aAA));
//        }
//
////        Collections.shuffle(listOfFiles);
//
        for (File aFile : listOfFiles) {
            String absPath = aFile.getAbsolutePath();
            String aJsonFULL = StringIO2.readString(absPath);
            System.out.println("Processing: " + absPath);
            XFile fFULL = JsonHelper.deserialize(aJsonFULL);
            if (fFULL.getPages() == null || fFULL.getPages().isEmpty()) {
                continue;
            }
            int pageCnt = -1;
            boolean showDocument = false;
            for (XPage aPage : fFULL.getPages()) {
                pageCnt++;
                List<Polygon2DInt> plotPolys = new ArrayList<>();
                double angle = Misc.getMainAngle(aPage);
                if (angle == 0.0 && angleVisMode == 1) continue;
                double baselineConf = aPage.getBaselineConf();
                if (baselineConf < minBLConf) continue;
                HybridImage aImage = Misc.loadPageNumber(aFile.getParent() + "/" + fFULL.getSrcImgPath(), pageCnt);
                for (XRegion xreg : aPage.getRegions()) {
                    for (XLine line : xreg.getLines()) {
                        LOG.log(Logger.WARN, new StdFrameAppender.AppenderContent(aImage, line.getText() + " (" + line.getTextConf() + ")", PolygonHelper.getIFPolygonList(getPlotPoly(line.getCoords()))));
                    }
                }
                if (enforcePresentPolys && plotPolys.isEmpty()) continue;
                LOG.log(Logger.WARN, new StdFrameAppender.AppenderContent(aImage, "PageNum: " + pageCnt + ", Main TextOrientation: " + angle, PolygonHelper.getIFPolygonList(plotPolys)));
                showDocument = true;
            }
            if (showDocument) {
                LOG.log(Logger.WARN, new StdFrameAppender.AppenderContent(true));
            }
        }

    }


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        if (args == null || args.length == 0) {
            File homeDir = new File("/home/gundram/devel/projects/maudour");
            args = new File(homeDir, "data/sep").toString().split(" ");
//            args[0] = "/home/tobi/devel/data_nas/imagesets_v2/FULL/COMPETITION/MAURDOR/";
//            args[0] = "/home/tobi/devel/data_nas/imagesets_v2/FULL/SYNTH/chyn2d_samples_20190919/";
//            args[0] = "/home/tobi/devel/data/tmp/Planet_D020_D274_D295/";
        }

        String folder = args[0];

        ShowImages s = new ShowImages();
        s.run(IOOps.listFiles(new File(folder), "json", true));

    }

}