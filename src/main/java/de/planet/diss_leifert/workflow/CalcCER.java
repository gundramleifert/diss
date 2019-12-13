package de.planet.diss_leifert.workflow;

import com.achteck.misc.log.Logger;
import com.achteck.misc.param.ParamSet;
import de.planet.math.geom2d.types.Polygon2DInt;
import de.planet.math.types.IFPolygon;
import de.planet.math.util.PolygonHelper;
import de.planet.trainset_util.util.IOOps;
import de.planet.xml_helper.JsonHelper;
import de.planet.xml_helper.types.XFile;
import de.planet.xml_helper.types.XLine;
import de.planet.xml_helper.types.XPage;
import de.planet.xml_helper.types.XRegion;
import de.uros.citlab.errorrate.htr.end2end.ErrorModuleEnd2End;
import de.uros.citlab.errorrate.interfaces.ILine;
import de.uros.citlab.errorrate.types.Metric;
import de.uros.citlab.errorrate.types.PathCalculatorGraph;
import de.uros.citlab.errorrate.util.HeatMapUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class CalcCER extends FolderOrganizer {

//    @ParamAnnotation(descr = "folder with gt XFiles")
//    String gt;
//
//    @ParamAnnotation(descr = "folder with hyp XFiles")
//    String hyp;
//    @ParamAnnotation(descr = "folder to save pr-curve")
//    String res;

    private static final Logger LOG = Logger.getLogger(CalcCER.class);

    public CalcCER() {
        addReflection(this, CalcCER.class);
    }

    @Override
    public void init() {
        super.init();
    }

    public void run() {
//        File f = getFilePRCurve();
//        if (f.exists()) {
//            return;
//        }
        File folderGT = getFolderXFilesGT();
        File folderHYP = getFolderResultModelTranscript();
        List<File> gts = IOOps.listFiles(folderGT, "json", true);
        List<File> hyps = IOOps.listFiles(folderHYP, "json", true);
        double rAt2 = 0;
        double rAt1 = 0;
        double rAt0d5 = 0;
        double rAt0d2 = 0;
        if (gts.size() != hyps.size()) {
            throw new RuntimeException("number of json files do not match " + gts.size() + " vs. " + hyps.size());
        }
        Collections.sort(gts, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        Collections.sort(hyps, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        List<List<Line>> linesesGT = new LinkedList<>();
        List<List<Line>> linesesHYP = new LinkedList<>();
        for (int i = 0; i < gts.size(); i++) {
            linesesGT.add(getLines(gts.get(i)));
            linesesHYP.add(getLines(hyps.get(i)));
        }
//        o_prec[0]=1;
//        double gACER = 0;
//        double gALER = 0;
        ErrorModuleEnd2End module = new ErrorModuleEnd2End(false, true, true, false);
//            addDynProgExtractor(module, new File("/tmp/test.png"));
//            filter(linesesHYP, c);
        for (int j = 0; j < linesesGT.size(); j++) {
            module.calculateWithSegmentation(linesesHYP.get(j), linesesGT.get(j));
        }
        Map<Metric, Double> metrics = module.getMetrics();
//        double aRec = metrics.get(Metric.REC);
//        double aCER = metrics.get(Metric.ERR);
//        double aLER = metrics.get(Metric.PREC);
//            if (i > 0) {
//                gACER += aCER + outs.getFirst()[1];
//                gALER += aLER + outs.getFirst()[2];
//            }
        System.out.println(metrics);
//        }
    }

    private static class DynMatImage implements PathCalculatorGraph.DynMatViewer {
        private final File file;

        public DynMatImage(File file) {
            this.file = file;
        }

        @Override
        public boolean callbackEnd(float[][] mat) {
            try {
                ImageIO.write(HeatMapUtil.getHeatMap(mat, 7), "png", file);
            } catch (IOException e) {
                Logger.getLogger(ApplyATRandT2I.class).log(Logger.WARN, "cannot save image " + file, e);
                return false;
            }
            return true;
        }

        @Override
        public boolean callbackUpdate(double d, float[][] mat) {
            return false;
        }

        @Override
        public int[] getSize(int[] actual) {
            return new int[]{Math.min(actual[0], 10000), Math.min(actual[1], 10000)};
        }
    }

    private DynMatImage dynMatImage = null;

//    public void addDynProgExtractor(ErrorModuleT2I module, final File file) {
//        if (dynMatImage == null) {
//            dynMatImage = new DynMatImage(new File("/tmp/img.png"));
//        }
//        module.setDynMatViewer(dynMatImage);
//    }

    public static void filter(List<List<Line>> lineses, double confidence) {
        for (List<Line> linese : lineses) {
            linese.removeIf(line -> line.getText().isEmpty() || line.getConfidence() < confidence);
        }
    }

    private List<IFPolygon> getPolys(List<Line> lines) {
        List<Polygon2DInt> res = new LinkedList<>();
        for (Line xline : lines) {
            res.add(convert(xline.getBaseline()));
        }
        return PolygonHelper.getIFPolygonList(res);
    }

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

    List<Double> getConfidences(List<List<Line>> hypeses, int size) {
        List<Double> res = new LinkedList<>();
        for (List<Line> hypese : hypeses) {
            for (Line line : hypese) {
                if (line.confidence > 0.0) {
                    res.add(line.confidence);
                }
            }
        }
        if (res.isEmpty()) {
            return null;
        }
        Collections.sort(res);
        List<Double> res2 = new LinkedList<>();
        for (int i = size; i > 0; i--) {
            res2.add(res.get((res.size() - 1) * (i - 1) / size) - Double.MIN_NORMAL);
        }
        Collections.reverse(res2);
        return res2;
    }

    private static class Line implements ILine {
        private final String text;
        private final double confidence;
        private final Polygon polygon;

        public Line(String text, double confidence, Polygon polygon) {
            this.text = text == null ? "" : text;
            this.confidence = confidence;
            this.polygon = polygon;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public Polygon getBaseline() {
            return polygon;
        }

        @Override
        public Polygon getPolygon() {
            return null;
        }

        @Override
        public String getId() {
            return null;
        }

        public double getConfidence() {
            return confidence;
        }
    }

    private List<Line> getLines(File fileJson) {
        XFile deserialize = JsonHelper.deserialize(fileJson);
        List<Line> res = new LinkedList<>();
        for (XPage page : deserialize.getPages()) {
            for (XRegion region : page.getRegions()) {
                for (XLine line : region.getLines()) {
                    res.add(new Line(line.getText(), line.getTextConf(), convert(line.getBaseLine())));
                }
            }

        }
        return res;
    }

    public static Polygon convert(Polygon2DInt p) {
        return new Polygon(p.xpoints, p.ypoints, p.npoints);
    }

    public static Polygon2DInt convert(Polygon p) {
        return new Polygon2DInt(p.xpoints, p.ypoints, p.npoints);
    }

    public static void main(String[] args) throws Exception {
//        List<File> jpg = IOOps.listFiles(new File("/home/gundram/devel/projects/bentham/data/105011/val_orig/"), "jpg", true);
//        jpg.removeIf(file -> !file.getName().contains(name));
//        HybridImage img = HybridImage.newInstance(jpg.get(0));
//        LOG.log(Logger.DEBUG, new StdFrameAppender.AppenderContent(img, "HYP"));
//                    LOG.log(Logger.DEBUG, new StdFrameAppender.AppenderContent(img, "GT", getPolys(linesesGT.get(j)),true));
//        LOG.log(Logger.DEBUG, new StdFrameAppender.AppenderContent(true));

//        File HomeDir = new File("/home/gundram/devel/projects/diss");
//        File[] results = new File(HomeDir, "results/val").listFiles();
//        List<File> files = new ArrayList<>(Arrays.asList(results));
//        files.removeIf(file -> !file.getName().contains("conf=0.0_skipW=1.6_skipB=4.0_anyC=16.0_cert="));

        //        files.removeIf(file -> !file.getName().contains("DFT"));
//        files = Arrays.asList(new File(HomeDir, "results/la_val_10/conf=0.0_skipW=1.6_skipB=4.0_anyC=16.0_cert=MAX_100"));
//        for (File result : files) {

//        if (args.length == 0) {
        List<String> configs = new LinkedList<>();
//        File folderConfigs = new File(FolderOrganizer.getHomeDir(), "results/bentham/valid/");
//        String net = args[0];
//        String net = "icdar_2017_10_0_planet";
        String net = "icdar_2017_20_0_planet";
//        String net = "icdar_2017_40_0_planet";
//        folderConfigs = new File(folderConfigs, net);
//        File[] files = folderConfigs.listFiles();
//        for (File file : files) {
        try {
//            if (file.getName().startsWith("conf"))
//                configs.add(file.getName());
            args = (""
                    + "-mainset bentham "
                    + "-subset valid "
//                + "-net NO_RO_HYP_net7 "
//                + "-net icdar_2017_40_0_planet "
                    + "-net " + net + " "
//                        + "--help"
                    + "").split("\\s+");
//        }
            System.out.println(Arrays.toString(args));
            CalcCER te = new CalcCER();
            ParamSet ps = new ParamSet();
            ps.setCommandLineArgs(args);    // allow early parsing
            ps = te.getDefaultParamSet(ps);
            ps = ParamSet.parse(ps, args, ParamSet.ParseMode.STRICT); // be strict, don't accept generic parameter
            te.setParamSet(ps);
            te.init();
            te.run();
        } catch (RuntimeException e) {
            System.out.println("error with exception " + e);
            LOG.log(Logger.ERROR, e);
            e.printStackTrace();
        }
    }
}
//    }

//}
