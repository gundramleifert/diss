package de.planet.diss_leifert.workflow;

import com.achteck.misc.log.Logger;
import com.achteck.misc.param.ParamSet;
import com.achteck.misc.types.ConfMat;
import com.achteck.misc.types.ParamAnnotation;
import com.achteck.misc.util.IO;
import com.achteck.misc.util.StopWatch;
import com.achteck.misc.util.StringIO2;
import de.planet.citech.trainer.loader.IImageLoader;
import de.planet.diss_leifert.types.ConfMatStreamUtil;
import de.planet.diss_leifert.types.GraphViewer;
import de.planet.diss_leifert.types.T2IConfig;
import de.planet.diss_leifert.util.ConfMatUtil;
import de.planet.imaging.types.IWDImage;
import de.planet.itrtech.reco.IImagePreProcess;
import de.planet.itrtech.reco.ISNetwork;
import de.planet.tensorflow.types.SNetworkTF;
import de.planet.tensorflow.types.SNetworkTFMulti;
import de.planet.trainset_util.util.IOOps;
import de.planet.util.LoaderIO;
import de.planet.xml_helper.JsonHelper;
import de.planet.xml_helper.types.XFile;
import de.planet.xml_helper.types.XLine;
import de.planet.xml_helper.types.XPage;
import de.planet.xml_helper.types.XRegion;
import de.uros.citlab.errorrate.types.PathCalculatorGraph;
import de.uros.citlab.errorrate.util.HeatMapUtil;
import de.uros.citlab.textalignment.HyphenationProperty;
import de.uros.citlab.textalignment.TextAligner;
import de.uros.citlab.textalignment.types.LineMatch;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import static de.planet.trainset_util.util.IOOps.listFilesAndDirs;


public class ApplyATRandT2I extends FolderOrganizer implements Callable<Boolean> {

    //    @ParamAnnotation
//    private String mainset = "bentham";
//
//    @ParamAnnotation
//    private String subset = "valid";
//
//    @ParamAnnotation
//    private String net = "icdar_2017_20_0_planet";
//
//    @ParamAnnotation(name = "config")
//    private String configName = "";
//
//    @ParamAnnotation(descr = "cache confmats")
//    boolean cm = false;

    @ParamAnnotation(descr = "name of image (if empty, all images)")
    String name = "";

    @ParamAnnotation(descr = "gpu thread (-1=CPU)")
    int gpu = -1;

    @ParamAnnotation(descr = "use batchmode")
    boolean batch = false;

    @ParamAnnotation(descr = "save debug images in folder")
    boolean debug = false;

    @ParamAnnotation(descr = "overwrite already existing xfiles")
    boolean overwrite = false;

    ISNetwork iNet = null;
    T2IConfig config = null;
    TextAligner textAligner = null;
    private static Logger LOG = Logger.getLogger(ApplyATRandT2I.class);

    public ApplyATRandT2I(String mainset, String subset, String net, String configName) {
        super(mainset, subset, net, configName);
        addReflection(this, ApplyATRandT2I.class);
    }

    public ApplyATRandT2I() {
        this("", "", "", "");
    }

    public static File getCharMap(File folderHtr) {
        File exportFolder = new File(folderHtr, "export");
        if (exportFolder.exists()) {
            List<File> files = IOOps.listFiles(exportFolder, "txt", false);
            if (files.size() == 1) {
                System.out.println("use charmap " + files.get(0).getAbsolutePath());
                return files.get(0);
            }
        }
        return null;
    }

    public static IImagePreProcess loadPreProc(File folderHtr) {
        List<File> files = IOOps.listFiles(new File(folderHtr, "export"), "bin", false);
        if (files.size() != 1) {
            throw new RuntimeException("cannot find preprocess at " + new File(folderHtr, "export") + " and found " + files.size() + " preprocs (*.bin) in export folder");
        }
        IImagePreProcess pp = null;
        try {
            pp = (IImagePreProcess) IO.load(files.get(0).getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        pp.setParamSet(pp.getDefaultParamSet(null));
        pp.init();
        System.out.println("use preproc " + files.get(0).getAbsolutePath());
        return pp;
    }

    private static int getImgHeight(File htrFolder) {
        File export = new File(htrFolder, "export");
        if (!export.exists()) {
            throw new RuntimeException("cannot find folder " + export);
        }
        List<String> strings = null;
        try {
            strings = StringIO2.loadLineList(new File(export, "netconfig.info"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (strings.size() != 1) {
            throw new RuntimeException("expect file " + new File(export, "netconfig.info") + " to have only one line");
        }
        String line = strings.get(0);
        int idx = line.indexOf("inHeight");
        idx = line.indexOf("inImg", idx);
        int from = line.indexOf(":", idx);
        int to = line.indexOf("}", idx);
        System.out.println("use image height " + line.substring(from + 1, to).trim());
        return Integer.parseInt(line.substring(from + 1, to).trim());
    }

    private static File getFrozenModel(File htrFolder, boolean gpu) {
        File export = new File(htrFolder, "export");
        if (!export.exists()) {
            throw new RuntimeException("cannot find folder " + export);
        }
        List<File> pb = IOOps.listFiles(export, "pb", false);
        if (pb.size() != 1) {
            if (pb.size() > 2) {
                throw new RuntimeException("found " + pb.size() + "pb-files in folder " + export + " but expect 1");
            }
            for (File file : pb) {
                if (gpu == file.getName().endsWith("_gpu.pb")) {
                    System.out.println("use pb-graph " + file.getAbsolutePath());
                    return file;
                }
            }

        }
        System.out.println("use pb-graph " + pb.get(0).getAbsolutePath());
        return pb.get(0);
    }

    public void init() {
        super.init();
    }

    public void lateInit() {
        File configFile = getFileConfig();
        try {
            config = JsonHelper.deserialize(configFile, T2IConfig.class);
            textAligner = new TextAligner(" ", config.skipWord, config.skipBl, config.jumpBl);
            textAligner.setThreshold(config.conf);
//        textAligner.setUpdateScheme(de.uros.citlab.errPathCalculatorGraph.UpdateScheme.LAZY);
            textAligner.setHp(config.hyp == null ? null : new HyphenationProperty(config.hyp, null));
            textAligner.setCert(config.certMethod);
            textAligner.setBorderSize(config.certSize);
            textAligner.setCalcDist(config.calcDist);
            textAligner.setCostAnyChar(config.anyChar);
            if (config.hypProperty != null && !config.hypProperty.isEmpty()) {
                throw new RuntimeException("Hyphenation pattern not implemented so far");
            }
//        NativeLibLoader.loadTensorflowLibs(false, false, true);
//        NativeLibLoader.loadOpenCvLibs();
//        NativeLibLoader.loadPlanetCvLibs();
            File fileAtr = getModel();
            if (fileAtr.isDirectory()) {
                System.out.println("atr as folder = " + fileAtr.getAbsolutePath());
                iNet = new SNetworkTF(loadPreProc(fileAtr),
                        getFrozenModel(fileAtr, gpu > -1).getAbsolutePath(),
                        getCharMap(fileAtr).getAbsolutePath(),
                        getImgHeight(fileAtr));
            } else {
                System.out.println("atr as file = " + fileAtr.getAbsolutePath());
                try {
                    iNet = (ISNetwork) IO.load(fileAtr.getAbsolutePath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            iNet.setParamSet(iNet.getDefaultParamSet(new ParamSet()));
            if (iNet instanceof SNetworkTF) {
                ((SNetworkTF) iNet).init(true);
                if (gpu > -1) {
                    System.out.println("set gpu = " + gpu);
                    ((SNetworkTF) iNet).setGpuMode(true);
                    ((SNetworkTF) iNet).setThreadId(String.valueOf(gpu));
                }
            }
            if (iNet instanceof SNetworkTFMulti) {
                ((SNetworkTFMulti) iNet).init(true);
                if (gpu > -1) {
                    System.out.println("set gpu = " + gpu);
                    ((SNetworkTFMulti) iNet).setGpuMode(true);
                    ((SNetworkTFMulti) iNet).setThreadId(String.valueOf(gpu));
                }
            }
            iNet.init();
        } catch (RuntimeException t) {
            System.out.println(t.getMessage());
            t.printStackTrace();
            throw t;
        }
    }


    public void addDynProgExtractor(TextAligner textAligner, final File file) {
        textAligner.setDynMatViewer(new PathCalculatorGraph.DynMatViewer() {
            @Override
            public boolean callbackEnd(float[][] mat) {
                try {
                    file.getParentFile().mkdirs();
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
        });
        GraphViewer graphViewer = new GraphViewer();
        graphViewer.setView(1, 1, 30, 50);
        graphViewer.setOut(new File(file.getParentFile(), file.getName() + "_out.png/"));
        textAligner.setFilter(graphViewer);

    }

    private static void sort(List<File> list) {
        list.sort(new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                String[] s1 = o1.getName().split("[_.]");
                String[] s2 = o2.getName().split("[_.]");
                if (s1.length != s2.length) {
                    return o1.compareTo(o2);
                }
                for (int i = 0; i < s1.length; i++) {
                    if (s1[i].equals(s2[i])) {
                        continue;
                    }
                    if (s1[i].matches("[0-9]+") && s2[i].matches("[0-9]+")) {
                        int r = Integer.compare(Integer.parseInt(s1[i]), Integer.parseInt(s2[i]));
                        if (r != 0) {
                            return r;
                        }
                    }
                }
                return o1.compareTo(o2);
            }
        });
    }

    public static List<File> getFoldersLeave(File parentFolder) {
        Collection<File> foldersExec = listFilesAndDirs(parentFolder, DirectoryFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        foldersExec.removeIf(new Predicate<File>() {
            public boolean test(File file) {
                File[] var2 = file.listFiles();
                int var3 = var2.length;

                for (int var4 = 0; var4 < var3; ++var4) {
                    File listFile = var2[var4];
                    if (listFile.isDirectory()) {
                        return true;
                    }
                }

                return false;
            }
        });
        LinkedList<File> files = new LinkedList(foldersExec);
        Collections.sort(files);
        return files;
    }


    public void run() throws Exception {
        System.out.println("start run");
        lateInit();
        System.out.println("start for config " + getConfig());
        File folderLines = getFolderLines();
        File folderXfiles = getFolderXFilesLA();
        File folderTextFiles = getFolderTextfiles();
        List<File> foldersLeave = getFoldersLeave(folderLines);
        Collections.sort(foldersLeave);
        if (!name.isEmpty()) {
            foldersLeave.removeIf(file -> !file.getName().contains(name));
        }
//        String nameATR = new File(atr).getName();

        File folderOut = getFolderResultFiles();
        folderOut.mkdirs();
        JsonHelper.serialize(new File(folderOut, "config.txt"), config);
        int idx = 0;
        StopWatch sw = new StopWatch("ATR");
        StopWatch sw2 = new StopWatch("T2I");
        int cntall = 0;
        int cntfound = 0;
        for (File folder : foldersLeave) {
            File tgtFileTxt = getTgtFile(folderLines, folderTextFiles, folder, ".txt");
            File fileJson = getTgtFile(folderLines, folderXfiles, folder, ".json");
            File fileJsonOut = getTgtFile(folderLines, folderOut, folder, ".json");
            if (fileJsonOut.exists() && !overwrite) {
                LOG.log(Logger.WARN, "file " + fileJsonOut.getAbsolutePath() + " exists, skip file.");
                System.out.println("file " + fileJsonOut.getAbsolutePath() + " exists, skip file.");
                continue;
            }
            XFile file = JsonHelper.deserialize(fileJson);
            try {
                if (debug) {
                    File debugImage = getTgtFile(folderLines, getFolderDebugAlignment(), folder, ".png");
                    addDynProgExtractor(textAligner, debugImage);
                }
                idx++;
                List<ConfMat> confMats = new LinkedList<>();
                File fileConfMats = new File(folderOut, folder.getName() + ".bin");
                if (fileConfMats.exists()) {
                    confMats = ConfMatStreamUtil.readConfmatsFromStream(fileConfMats);
                } else {
                    List<File> images = IOOps.listFiles(folder, "png", true);
                    sort(images);
                    List<IWDImage> his = new LinkedList<>();
                    for (File image : images) {
                        IImageLoader.IImageHolder h = LoaderIO.loadImageHolder(image.getAbsolutePath(), true, false);
                        his.add(h);
                    }
                    sw.start();
                    if (batch) {
                        iNet.setInputs(his);
                        iNet.update();
                        confMats = iNet.getConfMats();
                    } else {
                        for (IWDImage hi : his) {
                            iNet.setInput(hi.getImage());
                            iNet.update();
                            confMats.add(new ConfMat(iNet.getConfMat()));
                        }
                    }
                    sw.stop();
                    System.out.println(idx + " / " + foldersLeave.size() + " and " + confMats.size() + " lines time = " + sw.toString());
                }
//                if (cm) {
//                    ConfMatStreamUtil.writeStreamFromConfmats(confMats, fileConfMats);
//                    System.out.println("save confmats");
//                }
                sw2.start();
                List<de.uros.citlab.confmat.ConfMat> convert = ConfMatUtil.convert(confMats);
                System.out.println("search in folder " + folder);
//                File fileText = IOOps.listFiles(folder, "txt", false).get(0);
                StringBuilder sb = new StringBuilder();
                for (String readLine : IOOps.readLines(tgtFileTxt)) {
                    sb.append(readLine).append(" ");
                }
                String text = sb.toString().replaceAll("\\s+", " ").trim();
                List<LineMatch> alignmentResult = textAligner.getAlignmentResult(Arrays.asList(text), convert);
//            for (LineMatch lineMatch : alignmentResult) {
//                System.out.println(lineMatch);
//            }
                int idxLine = 0;
                int found = 0;
                for (XPage page : file.getPages()) {
                    for (XRegion region : page.getRegions()) {
                        for (XLine line : region.getLines()) {
                            LineMatch lineMatch = alignmentResult.get(idxLine);
                            if (lineMatch != null && lineMatch.getConfidence() > config.conf) {
                                line.setTextConf(lineMatch.getConfidence());
                                line.setText(lineMatch.getReference());
                                found++;
                            }
                            idxLine++;
                        }
                    }
                }
                sw2.stop();
                cntall += idxLine;
                cntfound += found;
                JsonHelper.serialize(fileJsonOut, file);
                System.out.println("found " + found + " / " + idxLine + " - total = " + (cntfound * 100 / cntall) + "%" + " time = " + sw2.toString());
            } catch (RuntimeException ex) {
//                JsonHelper.serialize(new File(outFolder, folder.getName() + ".json"), file);
//                throw ex;
                System.out.println("error: " + ex);
                LOG.log(Logger.ERROR, "T2I fails with error:", ex);
            }
        }
        System.out.println("end for config " + config);
    }

    /**
     * @param args the command line arguments
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

//        List<File> txt = IOOps.listFiles(new File("/home/gundram/devel/projects/diss/text/val_orig_manual"), "txt", true);
//        HashMap<String,File> map = new LinkedHashMap<>();
//        for (File file : txt) {
//            String name = file.getName();
//            name = name.substring(0, name.lastIndexOf("."));
//            System.out.println(file);
//            System.out.println(name);
//            map.put(name, file);
//        }
//        List<File> jsos = IOOps.listFiles(new File("/home/gundram/devel/projects/diss/la/val_orig"), "json", true);
//        for (File file : jsos) {
//            String name = file.getName();
//            name = name.substring(0, name.lastIndexOf("."));
//            File file1 = map.get(name);
//            IOOps.copyFile(file1, new File(file.getParentFile(),file1.getName()));
//            System.out.println("did" +file1+ " to "+new File(file.getParentFile(),file1.getName()));
//        }
//        System.exit(-1);
//        List<File> json1 = IOOps.listFiles(new File("/home/gundram/devel/projects/diss/configs/"), "json", false);
//        System.out.println("found files:");
//        for (File file : json1) {
//            System.out.println(file);
//        }

//        json1.removeIf(file -> !file.getName().contains("conf-0.0_dist-false_skipW-1.6_skipB-4.0_anyC-8.0_jumpB-null_hyp-null_hypProp-null_cert-MAX_size-3"));
//        Collections.shuffle(json1, new Random(1236));
//        List<ApplyATRandT2I> runables = new LinkedList<>();
//        for (File file : json1) {
//            System.out.println(file.getName());
//            try {
//                if (args.length == 0) {
        args = (""
//                + "-in /home/gundram/devel/projects/diss/data/bentham/valid "
//                        + "-out /home/gundram/devel/projects/bentham/cms/ "
//                + "-out /home/gundram/devel/projects/diss/results/valid "
//                + "-net NO_RO_HYP_net7 "
//                + "-net icdar_2017_40_0_planet "
                + " -mainset bentham "
                + " -subset valid "
                + "-net icdar_2017_20_0_planet "
//                + "-debug /home/gundram/devel/projects/debug/ "
//                + "-config conf-0.0_dist-false_skipW-0.4_skipB-4.0_anyC-4.0_jumpB-null_hyp-null_hypProp-null_cert-MAX_size-3.json "
                + "-config conf-0.0_dist-false_skipW-1.2_skipB-4.0_anyC-4.0_jumpB-null_hyp-0.4_hypProp-null_cert-MAX_size-3 "
                + "-batch false "
                + "-overwrite true "
                + "-debug true "
//                    + "-cm true "
//                + "--help"
                + "").trim().split("\\s+");
//                }
        ApplyATRandT2I te = new ApplyATRandT2I();
        ParamSet ps = new ParamSet();
        ps.setCommandLineArgs(args);    // allow early parsing
        ps = te.getDefaultParamSet(ps);
        ps = ParamSet.parse(ps, args, ParamSet.ParseMode.STRICT); // be strict, don't accept generic parameter
        te.setParamSet(ps);
        te.init();
        te.call();
//                runables.add(te);
//            } catch (Throwable e) {
//            }
//        }
//        ExecutorService executorService = Executors.newFixedThreadPool(runables.get(0).t);
//        System.out.println("start with " + runables.size() + " tasks");
//        executorService.invokeAll(runables);
//        executorService.shutdown();
    }


    @Override
    public Boolean call() throws Exception {
        run();
        return Boolean.TRUE;
    }
}
