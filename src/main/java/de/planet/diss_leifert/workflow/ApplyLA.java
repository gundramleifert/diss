package de.planet.diss_leifert.workflow;

import com.achteck.misc.log.Logger;
import com.achteck.misc.param.ParamSet;
import com.achteck.misc.types.ParamAnnotation;
import de.planet.citech.trainer.loader.IImageLoader;
import de.planet.diss_leifert.util.FileUtil;
import de.planet.imaging.types.ByteImage;
import de.planet.imaging.types.HybridImage;
import de.planet.imaging.types.StdFrameAppender;
import de.planet.imaging.util.ImageHelper;
import de.planet.itrtech.roifinder.IROIFinder;
import de.planet.itrtech.textfinder.ITextFinder;
import de.planet.itrtech.types.ImagePropertyIDs;
import de.planet.math.geom2d.types.Polygon2DInt;
import de.planet.math.types.IFPolygon;
import de.planet.math.util.PolygonHelper;
import de.planet.roi_core.util.RotateHelper;
import de.planet.roi_neural.finder.ITRNeuralFinder;
import de.planet.trainset_util.util.IOOps;
import de.planet.util.LoaderIO;
import de.planet.xml_helper.JsonHelper;
import de.planet.xml_helper.types.XFile;
import de.planet.xml_helper.types.XLine;
import de.planet.xml_helper.types.XPage;
import de.planet.xml_helper.types.XRegion;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

public class ApplyLA extends FolderOrganizer {
////////////////////////////////////////////////
/// File:       run_it.java
/// Created:    09.08.2018  13:40:36
/// Encoding:   UTF-8
///
/// Planet Artificial Intelligence GmbH CONFIDENTIAL
/// __________________
///
/// [2018] Planet Artificial Intelligence GmbH
/// All Rights Reserved.
///
/// NOTICE:  All information contained herein is, and remains
/// the property of Planet Artificial Intelligence GmbH and its suppliers,
/// if any.  The intellectual and technical concepts contained
/// herein are proprietary to Planet Artificial Intelligence GmbH
/// and its suppliers and may be covered patents,
/// patents in process, and are protected by trade secret or copyright law.
/// Dissemination of this information or reproduction of this material
/// is strictly forbidden unless prior written permission is obtained
/// from Planet Artificial Intelligence GmbH.
////////////////////////////////////////////////

    /**
     * Desciption of run_it
     * <p>
     * <p>
     * Since 09.08.2018
     *
     * @author Tobi G. <tobias.gruening@planet.de>
     */

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(ApplyLA.class.getName());

    @ParamAnnotation(name = "itrFi", descr = "Name of NeuralFinder Class", member = "itrFi", addMemberParam = true)
    protected String finderClass = ITRNeuralFinder.class.getName();
    private ITextFinder itrFi;

//    @ParamAnnotation(descr = "Folder to source images")
//    private String in = "";

//    @ParamAnnotation(descr = "target folder for xfiles")
//    private String outX = "";
//    @ParamAnnotation(descr = "target folder for text lines")
//    private String outL = "";
//    @ParamAnnotation(descr = "target folder snippets")
//    private String out2 = "";
//    @ParamAnnotation(descr = "target folder textfile")
//    private String out3 = "";

    public ApplyLA() {
        addReflection(this, ApplyLA.class);
    }

    @Override
    public void init() {
//        NativeLibLoader.loadTensorflowLibs(false, false, true);
//        NativeLibLoader.loadOpenCvLibs();
//        NativeLibLoader.loadPlanetCvLibs();
        super.init();
    }

    public void runTest() throws FileNotFoundException, IOException {
        System.out.println("Working Directory = "
                + System.getProperty("user.dir"));
        File inFolder = getFolderImages();
        List<File> images = IOOps.listFiles(inFolder, "jpg", true);
        File outFolderXFiles = getFolderXFilesLA();
        File outFileL = getFolderLines();
//        File outFile2 = new File(out2);
//        File outFile3 = new File(out3);
        if (!outFolderXFiles.exists()) {
            outFolderXFiles.mkdirs();
        }
        if (!outFileL.exists()) {
            outFileL.mkdirs();
        }
        int imgNo = 0;
        double sTime = 0.0;
        for (File img : images) {
            imgNo++;
            System.out.println("Image No: " + imgNo + " / " + images.size() + " Path: " + img);
            XFile xFile = null;
            String name = img.getName();
            name = name.substring(0, name.lastIndexOf("."));
            File fileJson = getTgtFile(inFolder, outFolderXFiles, img, ".json");
            if (!fileJson.exists()) {
                HybridImage testImg = HybridImage.newInstance(img);

                long aT = System.currentTimeMillis();

                List<IROIFinder.IROIMask> incl = new ArrayList<>();

                List<IFPolygon> plotPoly = new ArrayList<>();
                if (LOG.isDebugEnabled()) {
                    LOG.log(Logger.DEBUG, new StdFrameAppender.AppenderContent(testImg, "InImg with all Lines", plotPoly));
                }
                List<IROIFinder.IROIFinderResult> rois = itrFi.findROIs(testImg, incl, null);
                List<Polygon2DInt> allLines = new ArrayList<>();
                for (IROIFinder.IROIFinderResult aRoi : rois) {
                    allLines.addAll(aRoi.getDetailedPolygons());
                }

                if (LOG.isDebugEnabled()) {
                    plotPoly.addAll(PolygonHelper.getIFPolygonList(allLines));
                }

                int roiCnt = 0;
                for (IROIFinder.IROIFinderResult aRoi : rois) {
                    roiCnt++;
                    LOG.log(Logger.WARN, new StdFrameAppender.AppenderContent(testImg, "ROI " + roiCnt + " in angle: " + aRoi.getAngle(), PolygonHelper.getIFPolygonList(aRoi.getDetailedPolygons())));
                    List<Polygon2DInt> detailedPolygons = aRoi.getDetailedPolygons();

                    List<HybridImage> detailedROIs = aRoi.getDetailedROIs(testImg);
                    for (int i = 0; i < detailedROIs.size(); i++) {
                        HybridImage aRoiImg = detailedROIs.get(i);
                        Polygon2DInt aRoiPoly = reducePoints(PolygonHelper.copy(detailedPolygons.get(i)));
                        PolygonHelper.transform(aRoiPoly, aRoiImg.getTrafo());
                    }
                }
                xFile = getXFile(rois, img.getName());
                String serialize = JsonHelper.serialize(xFile);
                File folderOutL = getTgtFile(inFolder, outFileL, img, "");
                extractLines(folderOutL, xFile, img);
//                File fileTxt = new File(img.getParentFile(), name + ".txt");
//                if (!fileTxt.exists()) {
//                    System.out.println("text file " + fileTxt + " does not exist");
//                    List<File> txt = IOOps.listFiles(img.getParentFile(), "txt", false);
//                    if (txt.size() > 1) {
//                        System.out.println("found text" + txt.size() + " text files - take the first one");
//                        fileTxt = txt.get(0);
//                        File fileTxtOut = new File(folderOutX, name + ".txt");
//                        IOOps.copyFile(fileTxt, fileTxtOut);
//                    } else if (!txt.isEmpty()) {
//                        System.out.println("found text file " + fileTxt);
//                        fileTxt = txt.get(0);
//                        File fileTxtOut = new File(folderOutX, name + ".txt");
//                        IOOps.copyFile(fileTxt, fileTxtOut);
//                    } else {
//                        LOG.log(Logger.WARN, fileTxt.getPath() + " not found, so no text files");
//                    }
//                }
                IOOps.writeString(fileJson, serialize);
                testImg.clear();
                long t = (System.currentTimeMillis() - aT);
                sTime += t;
                System.out.println("Time Used: " + t + "ms");
                LOG.log(Logger.DEBUG, new StdFrameAppender.AppenderContent(true));
            }
        }
        double avgT = sTime / imgNo;
        System.out.println("Avg Time: " + avgT);
    }

    private void extractLines(File folder, XFile xFile, File aImage) {
        ByteImage asByteImage = HybridImage.newInstance(aImage, true).getAsByteImage();
        for (XPage page : xFile.getPages()) {
            for (XRegion region : page.getRegions()) {
                for (XLine xline : region.getLines()) {
                    Polygon2DInt coords = xline.getCoords();
                    ByteImage aB = RotateHelper.rotate(asByteImage, coords, -region.getOrientation(), 0, 0);
                    HybridImage subI = HybridImage.newInstance(aB);
                    if (xline.isInverted()) {
                        subI = ImageHelper.invert(subI);
                    }
                    Polygon2DInt cPoly = PolygonHelper.copy(coords);
                    PolygonHelper.transform(cPoly, subI.getTrafo());
                    subI.setProperty(ImagePropertyIDs.MASK.toString(), cPoly);
                    Polygon2DInt baseLine = reducePoints(xline.getBaseLine());
                    HashMap<String, Object> map = new LinkedHashMap<>();
                    map.put("bl", PolygonHelper.asString(baseLine));
                    IImageLoader.ImageHolderDft iH = new IImageLoader.ImageHolderDft(subI, null, map);
                    LoaderIO.saveImageHolder(new File(folder, xline.getId() + ".png").getAbsolutePath(), iH);
                }
            }

        }
    }

    public static XFile getXFile(List<IROIFinder.IROIFinderResult> rois, String imgName) {
        XFile file = new XFile(imgName);
        XPage page = new XPage();
        file.addPage(page);
        for (IROIFinder.IROIFinderResult regionStruct : rois) {
            XRegion region = new XRegion(reducePoints(regionStruct.getPolygon()));
            page.addRegion(region);
            region.setOrientation(regionStruct.getAngle());
            List<Polygon2DInt> bls = ((ITRNeuralFinder.DftFinderResult) regionStruct).getBLs();
            List<Polygon2DInt> polys = regionStruct.getDetailedPolygons();
            for (int i = 0; i < bls.size(); i++) {
                XLine line = new XLine(reducePoints(polys.get(i)));
                line.setBaseLine(bls.get(i));
                region.addLine(line);
            }
        }
        return file;
    }

    public static Polygon2DInt reducePoints(Polygon2DInt polygon) {
        if (polygon.npoints < 3) {
            return polygon;
        }
        Polygon2DInt res = new Polygon2DInt();
        Point startPoint = new Point(polygon.xpoints[0], polygon.ypoints[0]);
        Point lastPoint = new Point(polygon.xpoints[1], polygon.ypoints[1]);
        res.addPoint(startPoint.x, startPoint.y);
        Point direction = substract(startPoint, lastPoint);
        for (int i = 2; i < polygon.npoints; i++) {
            Point currentPoint = new Point(polygon.xpoints[i], polygon.ypoints[i]);
            Point directionNew = substract(startPoint, currentPoint);
            if (directionNew.x * direction.y == direction.x * directionNew.y) {//same direction
                lastPoint = currentPoint;
            } else {
                res.addPoint(lastPoint.x, lastPoint.y);
                startPoint = lastPoint;
                lastPoint = currentPoint;
                direction = substract(startPoint, lastPoint);
            }
        }
        res.addPoint(lastPoint.x, lastPoint.y);
        return res;
    }

    private static Point substract(Point first, Point second) {
        return new Point(first.x - second.x, first.y - second.y);
    }

    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            args = (""
                    + "-mainset icdar "
                    + "-subset large "
                    + "--help "
                    + "-outX /home/gundram/devel/projects/diss/data/icdar/large/xfiles_la "
                    + "-outL /home/gundram/devel/projects/diss/data/icdar/large/lines_la").split("\\s+");
        }
        String[] args2 = (""
                + "-itrFi/finder/imgPrep/imgPrepRot/netName /home/gundram/devel/projects/diss/data/finder_v1.3/rot_v1.3b/ROT_190917d/export/ROT_190917d_2019-09-18.pb "
                + "-itrFi/finder/spInit/netName /home/gundram/devel/projects/diss/data/finder_v1.3/la_v1.3b/LA_190626c/export/LA_190626c_2019-07-02.pb "

                + "-itrFi/finder de.planet.roi_neural.finder.NeuralFinder "
                + "-itrFi/finder/imgPrep de.planet.roi_neural.prepare.PrepareBasic "
                + "-itrFi/finder/imgPrep/imgPrepRot/GPU 0 "
                + "-itrFi/finder/spInit de.planet.roi_neural.superpixelcalc.SuperPixelCalcNN_2 "
                + "-itrFi/finder/spInit/GPU 0 "
                + "-itrFi/finder/spState de.planet.roi_neural.stateestimation.StateEstimationHistDFT "
                + "-itrFi/finder/eRemoval de.planet.roi_neural.edgeremoval.EdgeRemoval_BL_Sep "
                + "-itrFi/finder/clusterer de.planet.roi_neural.clusterer.ClustererEdgeGrowing "
                + "-itrFi/finder/bcl de.planet.roi_neural.baselineclusterer.BaselineClustererMod90 "
                + "-itrFi/finder/b2p de.planet.roi_neural.baseline2polygon.Baseline2PolygonTube "
                + "-itrFi/finder/b2p/addX 0 "
                + "-itrFi/finder/adjRem de.planet.roi_neural.clusteringadjust.ClusteringAdjustRemoval "

//                                                + "--help "
        ).trim().split("\\s+");
        String[] argsMerge = new String[args.length + args2.length];
        System.arraycopy(args, 0, argsMerge, 0, args.length);
        System.arraycopy(args2, 0, argsMerge, args.length, args2.length);
        args = argsMerge;
        ApplyLA te = new ApplyLA();
        ParamSet ps = new ParamSet();
        ps.setCommandLineArgs(args);    // allow early parsing
        ps = te.getDefaultParamSet(ps);
        ps = ParamSet.parse(ps, args, ParamSet.ParseMode.STRICT); // be strict, don't accept generic parameter
        te.setParamSet(ps);
        te.init();
        System.out.println(te.getParamSet());

        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        Calendar c = df.getCalendar();
        c.setTimeInMillis(System.currentTimeMillis());
//        ConfigHelper.saveTreeConfig(ps, "roi_neural_" + c.get(Calendar.YEAR) + (c.get(Calendar.MONTH) + 1) + c.get(Calendar.DAY_OF_MONTH) + ".bin", "itrFi/", "de.planet.roi_neural.finder.ITRNeuralFinder");

        te.runTest();
    }

}


