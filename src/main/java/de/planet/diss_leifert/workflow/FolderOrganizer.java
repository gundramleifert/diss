package de.planet.diss_leifert.workflow;

import com.achteck.misc.types.ParamAnnotation;
import com.achteck.misc.types.ParamTreeOrganizer;
import de.planet.diss_leifert.util.FileUtil;

import java.io.File;

public class FolderOrganizer extends ParamTreeOrganizer {
    @ParamAnnotation
    private String mainset = "";

    @ParamAnnotation
    private String subset = "";

    @ParamAnnotation
    private String net = "";

    @ParamAnnotation(name = "config")
    private String configName = "";

    public FolderOrganizer(String mainset, String subset, String net, String configName) {
        this.mainset = mainset;
        this.subset = subset;
        this.net = net;
        this.configName = configName;
        addReflection(this, FolderOrganizer.class);
    }

    @Override
    public void init() {
        super.init();
        if (configName.endsWith(".json")) {
            configName = configName.substring(0, configName.lastIndexOf(".json"));
        }
    }

    public FolderOrganizer() {
        addReflection(this, FolderOrganizer.class);
    }

    public String getMainset() {
        return mainset;
    }

    public String getSubset() {
        return subset;
    }

    public String getNet() {
        return net;
    }

    public String getConfig() {
        return configName;
    }

    public static File getHomeDir() {
        return new File("/home/gundram/devel/projects/diss");
    }

    public File getModel() {
        return getNet().isEmpty()
                ? null
                : new File(getFolderModels(), getNet());
    }

    public static File getFolderModels() {
        return new File(getHomeDir(), "models");
    }

    public File getFolderMainSubset() {
        return new File(getHomeDir(), "data/" + getMainset() + "/" + getSubset());
    }

    public File getFolderLines() {
        return getMainset().isEmpty() || getSubset().isEmpty()
                ? null
                : new File(getFolderMainSubset(), "lines_la");
    }

    public File getFolderXFilesLA() {
        return getMainset().isEmpty() || getSubset().isEmpty()
                ? null
                : new File(getFolderMainSubset(), "xfiles_la");
    }

    public File getFolderXFilesGT() {
        return getMainset().isEmpty() || getSubset().isEmpty()
                ? null
                : new File(getFolderMainSubset(), "xfiles_gt");
    }

    public File getFolderImages() {
        return getMainset().isEmpty() || getSubset().isEmpty()
                ? null
                : new File(getFolderMainSubset(), "images");
    }

    public File getFolderTextfiles() {
        return getMainset().isEmpty() || getSubset().isEmpty()
                ? null
                : new File(getFolderMainSubset(), "texts");
    }

    public File getFolderResultFiles() {
        return getMainset().isEmpty() || getSubset().isEmpty() || getNet().isEmpty() || getConfig().isEmpty()
                ? null
                : new File(getHomeDir(), "results/" + getMainset() + "/" + getSubset() + "/" + getNet() + "/" + getConfig());
    }
    public File getFolderResultModelTranscript() {
        return getMainset().isEmpty() || getSubset().isEmpty() || getNet().isEmpty()
                ? null
                : new File(getHomeDir(), "results/" + getMainset() + "/" + getSubset() + "/" + getNet() + "/hyp");
    }

    public File getFolderDebug() {
        return getMainset().isEmpty() || getSubset().isEmpty() || getNet().isEmpty() || getConfig().isEmpty()
                ? null
                : new File(getHomeDir(), "debug/" + getMainset() + "/" + getSubset() + "/" + getNet() + "/" + getConfig());
    }

    public File getFolderDebugAlignment() {
        return getMainset().isEmpty() || getSubset().isEmpty() || getNet().isEmpty() || getConfig().isEmpty()
                ? null
                : new File(getHomeDir(), "debug_alignment/" + getMainset() + "/" + getSubset() + "/" + getNet() + "/" + getConfig());
    }

    public File getFilePRCurve() {
        return getMainset().isEmpty() || getSubset().isEmpty() || getNet().isEmpty() || getConfig().isEmpty()
                ? null
                : new File(getHomeDir(), "pr_curves/" + getMainset() + "/" + getSubset() + "/" + getNet() + "/" + getConfig() + ".png");
    }

    public File getFolderDebugGT() {
        return getMainset().isEmpty() || getSubset().isEmpty()
                ? null
                : new File(getHomeDir(), "debug/" + getMainset() + "/" + getSubset() + "/gt");
    }

    public File getFileConfig() {
        String config = getConfig();
        return new File(getFolderConfigs(), config + ".json");
    }

    public File getFolderConfigs() {
        return new File(getHomeDir(), "configs");
    }


    public File getTgtFile(File srcFolder, File tgtFolder, File srcFile, String suffix) {
        String path = FileUtil.getTgtFile(srcFolder, tgtFolder, srcFile).getPath();
        int i = path.lastIndexOf(".");
        if (i > path.length() - 6 && i > 0) {
            path = path.substring(0, i);
        }
        if (suffix != null && !suffix.isEmpty()) {
            if (suffix.startsWith(".")) {
                path += suffix;
            } else {
                path += "." + suffix;
            }
        }
        return new File(path);
    }
}
