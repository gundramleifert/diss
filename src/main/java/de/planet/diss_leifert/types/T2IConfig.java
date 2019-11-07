package de.planet.diss_leifert.types;

import de.planet.xml_helper.JsonHelper;

import java.io.File;

public class T2IConfig {
    public T2IConfig(double conf, Double skipWord, Double skipBl, Double anyChar, Double jumpBl, Double hyp, String hypProperty, int certSize, String certMethod) {
        this.conf = conf;
        this.skipWord = skipWord;
        this.skipBl = skipBl;
        this.anyChar = anyChar;
        this.jumpBl = jumpBl;
        this.hyp = hyp;
        this.hypProperty = hypProperty;
        this.certSize = certSize;
        this.certMethod = certMethod;
    }

    public T2IConfig(double conf, Double skipWord, Double skipBl, Double anyChar, Double jumpBl, Double hyp, String hypProperty, int certSize, String certMethod, boolean calcDist) {
        this(conf, skipWord, skipBl, anyChar, jumpBl, hyp, hypProperty, certSize, certMethod);
        this.calcDist = calcDist;
    }

    public T2IConfig copy() {
        return new T2IConfig(conf, skipWord, skipBl, anyChar, jumpBl, hyp, hypProperty, certSize, certMethod, calcDist);
    }

    public void setConf(double conf) {
        this.conf = conf;
    }

    public void setSkipWord(Double skipWord) {
        this.skipWord = skipWord;
    }

    public void setSkipBl(Double skipBl) {
        this.skipBl = skipBl;
    }

    public void setAnyChar(Double anyChar) {
        this.anyChar = anyChar;
    }

    public void setJumpBl(Double jumpBl) {
        this.jumpBl = jumpBl;
    }

    public void setHyp(Double hyp) {
        this.hyp = hyp;
    }

    public void setHypProperty(String hypProperty) {
        this.hypProperty = hypProperty;
    }

    public void setCertSize(int certSize) {
        this.certSize = certSize;
    }

    public void setCertMethod(String certMethod) {
        this.certMethod = certMethod;
    }

    public void setCalcDist(boolean calcDist) {
        this.calcDist = calcDist;
    }

    public double conf;

    public Double skipWord;

    public Double skipBl;

    public Double anyChar;

    public Double jumpBl;

    public Double hyp;

    public String hypProperty;

    public int certSize;

    public String certMethod;

    public boolean calcDist = false;

    @Override
    public String toString() {
        return ("conf-" + conf +
                "_dist-" + calcDist +
                "_skipW-" + skipWord +
                "_skipB-" + skipBl +
                "_anyC-" + anyChar +
                "_jumpB-" + jumpBl +
                "_hyp-" + hyp +
                "_hypProp-" + hypProperty +
                "_cert-" + certMethod +
                "_size-" + certSize).replace(" ", "");
    }

    public static void main(String[] args) {
        T2IConfig configDft = new T2IConfig(0.0,
                1.6,
                4.0,
                4.0,
                null,
                0.4,
                null,
                3,
                "SOFTMAX", false);
//        for (Double anyChar : new Double[]{null, 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 12.0, 14.0, 16.0}) {
//            T2IConfig instance = configDft.copy();
//            instance.setAnyChar(anyChar);
//            JsonHelper.serialize(new File("/home/gundram/devel/projects/diss/configs/" + instance.toString() + ".json"), instance);
//        }
//        for (int certSize : new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 100}) {
//            T2IConfig instance = configDft.copy();
//            instance.setCertSize(certSize);
//            JsonHelper.serialize(new File("/home/gundram/devel/projects/diss/configs/" + instance.toString() + ".json"), instance);
//        }
//        for (Double anyChar : new Double[]{null, 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 12.0, 14.0, 16.0}) {
//            T2IConfig instance = configDft.copy();
//            instance.setAnyChar(anyChar);
//            JsonHelper.serialize(new File("/home/gundram/devel/projects/diss/configs/" + instance.toString() + ".json"), instance);
//        }
        for (Double skipWord : new Double[]{null, 0.1, 0.15, 0.2, 0.3, 0.4, 0.6, 0.8, 1.2, 1.6, 2.4, 3.2, 4.8, 6.4}) {
            T2IConfig instance = configDft.copy();
            instance.setSkipWord(skipWord);
            JsonHelper.serialize(new File("/home/gundram/devel/projects/diss/configs/" + instance.toString() + ".json"), instance);
        }
        for (Double skipBl : new Double[]{null, 0.5, 0.75, 1.0, 1.5, 2.0, 3.0, 4.0, 6.0, 8.0, 12.0, 16.0, 24.0, 32.0}) {
            T2IConfig instance = configDft.copy();
            instance.setSkipBl(skipBl);
            JsonHelper.serialize(new File("/home/gundram/devel/projects/diss/configs/" + instance.toString() + ".json"), instance);
        }
        for (Double jumpBL : new Double[]{null, 4.0, 6.0, 8.0, 12.0, 16.0, 24.0, 32.0, 48.0, 64.0, 96.0, 128.0}) {
            T2IConfig instance = configDft.copy();
            instance.setJumpBl(jumpBL);
            JsonHelper.serialize(new File("/home/gundram/devel/projects/diss/configs/" + instance.toString() + ".json"), instance);
        }
    }
}