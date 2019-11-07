package de.planet.diss_leifert.workflow;

import com.achteck.misc.param.ParamSet;
import com.achteck.misc.types.ParamAnnotation;
import de.planet.trainset_util.util.IOOps;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApplyATRandT2IMulti extends ApplyATRandT2I {


    @ParamAnnotation(descr = "number of threads")
    int t = 1;

    public ApplyATRandT2IMulti() {
        addReflection(this, ApplyATRandT2IMulti.class);
    }

    public void run() throws Exception {
        List<ApplyATRandT2I> runnables = new LinkedList<>();
        List<String> configs = new LinkedList<>();
        List<String> nets = new LinkedList<>();
        if (getConfig().isEmpty()) {
            List<File> json = IOOps.listFiles(getFolderConfigs(), "json", true);
            for (File file : json) {
                configs.add(file.getName().replace(".json", ""));
            }

        } else {
            configs.add(getConfig());
        }
        if (getNet().isEmpty()) {
            List<File> folders = Arrays.asList(getFolderModels().listFiles());
            for (File file : folders) {
                nets.add(file.getName());
            }

        } else {
            nets.add(getNet());
        }
        for (String conf : configs) {
            for (String net : nets) {
                String[] args = (""
//                        + "-net " + net + " "
//                        + "-config " + conf + " "
                        + "-batch " + batch + " "
                        + "-overwrite " + overwrite + " "
                        + "-debug " + debug + " "
                        + "-gpu " + gpu + " "
//                    + "-cm true "
//                + "--help"
                        + "").split("\\s+");
//                }
                ApplyATRandT2I te = new ApplyATRandT2I(getMainset(), getSubset(), net, conf);
                ParamSet ps = new ParamSet();
                ps.setCommandLineArgs(args);    // allow early parsing
                ps = te.getDefaultParamSet(ps);
                ps = ParamSet.parse(ps, args, ParamSet.ParseMode.STRICT); // be strict, don't accept generic parameter
                te.setParamSet(ps);
                te.init();
                runnables.add(te);
//                te.call();

            }

        }

        ExecutorService executorService = Executors.newFixedThreadPool(t);
        System.out.println("start with " + runnables.size() + " tasks");
        executorService.invokeAll(runnables);
        executorService.shutdown();

    }

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
        if (args.length == 0) {
            args = (""
//                + "-in /home/gundram/devel/projects/diss/data/bentham/valid "
//                        + "-out /home/gundram/devel/projects/bentham/cms/ "
//                + "-out /home/gundram/devel/projects/diss/results/valid "
//                    + "-net NO_RO_HYP_net7 "
//                + "-net icdar_2017_40_0_planet "
//                + "-net icdar_2017_20_0_planet "
                    + "-mainset bentham "
                    + "-subset valid "
//                + "-debug /home/gundram/devel/projects/debug/ "
//                + "-config conf-0.0_dist-false_skipW-0.4_skipB-4.0_anyC-4.0_jumpB-null_hyp-null_hypProp-null_cert-MAX_size-3.json "
//                + "-config "
                    + "-t 4 "
//                + "-batch false "
                    + "-overwrite true "
//                + "-debug true "
//                    + "-cm true "
//                + "--help"
                    + "").split("\\s+");
        }
        ApplyATRandT2IMulti te = new ApplyATRandT2IMulti();
        ParamSet ps = new ParamSet();
        ps.setCommandLineArgs(args);    // allow early parsing
        ps = te.getDefaultParamSet(ps);
        ps = ParamSet.parse(ps, args, ParamSet.ParseMode.STRICT); // be strict, don't accept generic parameter
        te.setParamSet(ps);
        te.init();
        te.run();
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
