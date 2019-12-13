package de.planet.diss_leifert.workflow;

import com.achteck.misc.types.charmap.IntCharMap;
import com.achteck.misc.types.charmap.IntCharMaps;
import com.google.gson.Gson;
import de.planet.diss_leifert.types.GraphViewer;
import de.planet.diss_leifert.types.T2IConfig;
import de.planet.diss_leifert.util.ConfMatUtil;
import de.planet.util.types.ConfMatGenerator;
import de.planet.xml_helper.JsonHelper;
import de.uros.citlab.errorrate.types.PathCalculatorGraph;
import de.uros.citlab.textalignment.HyphenationProperty;
import de.uros.citlab.textalignment.Hyphenator;
import de.uros.citlab.textalignment.TextAligner;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class OutputGraphs {

    public static IntCharMap getCharmap() {
        IntCharMap.Builder b = IntCharMaps.newBuilder().put(0, '\u0000').put(1, ' ').put(2, '-');
        for (char i = 'a'; i < 'z' + 1; i++) {
            b.put((int) (i - 'a' + 3), i);
        }
        return b.build();
    }

    public static TextAligner getTextAligner(T2IConfig config) {
        TextAligner textAligner = new TextAligner(" ", config.skipWord, config.skipBl, config.jumpBl);
        textAligner.setThreshold(config.conf);
//        textAligner.setUpdateScheme(de.uros.citlab.errPathCalculatorGraph.UpdateScheme.LAZY);
        textAligner.setHp(config.hyp == null ? null : new HyphenationProperty(false, true, ":".toCharArray(), "-".toCharArray(), 0.0, Hyphenator.HyphenationPattern.DE));
        textAligner.setCert(config.certMethod);
        textAligner.setBorderSize(config.certSize);
        textAligner.setCalcDist(config.calcDist);
        textAligner.setCostAnyChar(config.anyChar);
        textAligner.setUpdateScheme(PathCalculatorGraph.UpdateScheme.ALL);
        return textAligner;
    }

    public static void runHyphenation() {
        IntCharMap charmap = getCharmap();
        List<de.uros.citlab.confmat.ConfMat> cms = new LinkedList<>();
        cms.add(ConfMatUtil.convert(ConfMatGenerator.getConfMat("la-", 1.0, charmap)));
        cms.add(ConfMatUtil.convert(ConfMatGenerator.getConfMat("ter", 1.0, charmap)));
        T2IConfig config = new T2IConfig(0.0, 0.0, 0.0, 0.0, null, null, null, 3, "MAX");
        {
            TextAligner textAligner = getTextAligner(config);
            List<String> refs = Arrays.asList("later");
            GraphViewer graphViewer = new GraphViewer();
            graphViewer.setOut(new File("hyp_orig.png"));
            textAligner.setFilter(graphViewer);
            textAligner.setUpdateScheme(PathCalculatorGraph.UpdateScheme.ALL);
            textAligner.getAlignmentResult(refs, cms);
        }
        {
            TextAligner textAligner = getTextAligner(config);
            List<String> refs = Arrays.asList("la-", "-ter");
            GraphViewer graphViewer = new GraphViewer();
            graphViewer.setOut(new File("hyp_sep1.png"));
            textAligner.setFilter(graphViewer);
            textAligner.setUpdateScheme(PathCalculatorGraph.UpdateScheme.ALL);
            textAligner.getAlignmentResult(refs, cms);
        }
        {
            TextAligner textAligner = getTextAligner(config);
            List<String> refs = Arrays.asList("la-", "ter");
            GraphViewer graphViewer = new GraphViewer();
            graphViewer.setOut(new File("hyp_sep2.png"));
            textAligner.setFilter(graphViewer);
            textAligner.setUpdateScheme(PathCalculatorGraph.UpdateScheme.ALL);
            textAligner.getAlignmentResult(refs, cms);
        }
        {
            TextAligner textAligner = getTextAligner(config);
            HyphenationProperty hyphenationProperty = new HyphenationProperty(false, true, ":".toCharArray(), "-".toCharArray(), 0.0, Hyphenator.HyphenationPattern.DE);
            config.setHypProperty(new Gson().toJson(hyphenationProperty));
            textAligner.setHp(hyphenationProperty);
            List<String> refs = Arrays.asList("later");
            GraphViewer graphViewer = new GraphViewer();
            graphViewer.setOut(new File("hyp_complete.png"));
            textAligner.setFilter(graphViewer);
            textAligner.getAlignmentResult(refs, cms);
        }
    }

    public static void main(String[] args) {
        runHyphenation();
        if (true)
            return;
//        File configFile = new File("/home/gundram/devel/projects/diss/configs/conf-0.0_dist-false_skipW-1.2_skipB-4.0_anyC-4.0_jumpB-null_hyp-0.4_hypProp-null_cert-MAX_size-3.json");
        File configFile = new File("/home/gundram/devel/projects/diss/configs/conf-0.0_dist-false_skipW-null_skipB-null_anyC-null_jumpB-null_hyp-null_hypProp-null_cert-MAX_size-3.json");
        T2IConfig config = JsonHelper.deserialize(configFile, T2IConfig.class);
        config.setHyp(0.0);
        config.setSkipBl(0.0);
        config.setSkipWord(0.0);
        config.setJumpBl(0.0);
//        GraphPresenter.GREY = "white";
//        GraphPresenter.REDGREY = "white";

        config.setAnyChar(0.0);
        TextAligner textAligner = new TextAligner(" ", config.skipWord, config.skipBl, config.jumpBl);
        textAligner.setThreshold(config.conf);
//        textAligner.setUpdateScheme(de.uros.citlab.errPathCalculatorGraph.UpdateScheme.LAZY);
        textAligner.setHp(config.hyp == null ? null : new HyphenationProperty(false, true, ":".toCharArray(), "-".toCharArray(), 0.0, Hyphenator.HyphenationPattern.DE));
        textAligner.setCert(config.certMethod);
        textAligner.setBorderSize(config.certSize);
        textAligner.setCalcDist(config.calcDist);
        textAligner.setCostAnyChar(config.anyChar);
//        if (config.hypProperty != null && !config.hypProperty.isEmpty()) {
//            throw new RuntimeException("Hyphenation pattern not implemented so far");
//        }

        IntCharMap.Builder b = IntCharMaps.newBuilder().put(0, '\u0000').put(1, ' ').put(2, '-');
        for (char i = 'a'; i < 'z' + 1; i++) {
            b.put((int) (i - 'a' + 3), i);
        }
        IntCharMap charmap = b.build();
        List<de.uros.citlab.confmat.ConfMat> cms = new LinkedList<>();
        cms.add(ConfMatUtil.convert(ConfMatGenerator.getConfMat("la-", 1.0, charmap)));
        cms.add(ConfMatUtil.convert(ConfMatGenerator.getConfMat("ter", 1.0, charmap)));
        List<String> refs = Arrays.asList("later");
        GraphViewer graphViewer = new GraphViewer();
//        graphViewer.setView(1, 1, 30, 50);
        graphViewer.setOut(new File("/tmp/OUT2.png"));
        textAligner.setFilter(graphViewer);
        textAligner.setUpdateScheme(PathCalculatorGraph.UpdateScheme.ALL);
        textAligner.getAlignmentResult(refs, cms);

    }

}
