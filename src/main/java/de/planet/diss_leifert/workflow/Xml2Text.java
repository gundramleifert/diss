package de.planet.diss_leifert.workflow;

import com.achteck.misc.param.ParamSet;
import com.achteck.misc.types.ParamAnnotation;
import com.achteck.misc.types.ParamTreeOrganizer;
import de.planet.diss_leifert.util.FileUtil;
import de.planet.trainset_util.util.IOOps;
import de.planet.xml_helper.util.XmlDomHelper;
import org.w3c.dom.Node;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class Xml2Text extends ParamTreeOrganizer {
    @ParamAnnotation
    String in = "";

    @ParamAnnotation
    String out = "";

    public Xml2Text() {
        addReflection(this, Xml2Text.class);
    }

    @Override
    public void init() {
        super.init();
    }

    public void run() {
        File folderIn = new File(in);
        List<File> xml = IOOps.listFiles(folderIn, "xml", true);
        File folderOut = new File(out);
        xml.removeIf(new Predicate<File>() {
            @Override
            public boolean test(File file) {
                return file.getName().contains("metadata") || file.getName().contains("mets") || file.getName().contains("doc");
            }
        });
        int count = 0;
        int countAll = 0;
        for (File file : xml) {
            XmlDomHelper.Doc doc = XmlDomHelper.loadXML(file);
            List<Node> textLine = XmlDomHelper.getElementsByName(doc, "TextLine");
            StringBuilder sb = new StringBuilder();
            List<String> lines = new LinkedList<>();
            for (Node node : textLine) {
                Node textEquiv = XmlDomHelper.getChild(node, "TextEquiv");
                if (textEquiv == null) {
                    continue;
                }
                String s = XmlDomHelper.getChild(textEquiv, "Unicode").getTextContent();
                s = s.replace("&amp;", "&");
                s = s.replace("&apos;", "'");
                s = s.replace("&lt;", "<");
                s = s.replace("&gt;", ">");
                s = s.replace("&quot;", "\"");
                s = s.replace("<INS>", "");
                s = s.replace("<gap/>", "");
                s = s.replaceAll("\\s+", " ");
                s = s.trim();
                lines.add(s);
                sb.append("###").append(s);
            }
            String out = sb.toString();
            count += out.split("=?[-¬] ?### ?-?").length - 1;
            String s = out.replaceAll("=?-###-", "");
            s = out;
            countAll += s.split("###").length - 1;
            int i = 0;
            while (s.indexOf("-", i + 1) > 0 || s.indexOf("¬", i + 1) > 0) {
                int j = Math.max(s.indexOf("-", i + 1), s.indexOf("¬", i + 1));
                System.out.println(s.substring(Math.max(0, j - 10), Math.min(s.length(), j + 10)));
                i = j + 1;
            }
            File tgtFile = FileUtil.getTgtFile(folderIn, folderOut, file);
            File fileOut = new File(tgtFile.getPath().replace(".xml", ".txt").replace("/page", ""));

            IOOps.writeString(
                    fileOut,
                    String.join("\n", lines)
            );


        }
        System.out.println("found " + count + " hyphenations of " + countAll + " line breaks which is " + ((count * 2000) / Math.max(1, countAll)) + " per mille");

    }

    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
//        List<File> in = IOOps.listFiles(new File("//home/gundram/devel/projects/bentham/text/val_orig_manual"), "txt", true);
//        List<File> out = IOOps.listFiles(new File("/home/gundram/devel/projects/diss/data/bentham/valid/texts/"), "txt", true);
//        for (int i = 0; i < in.size(); i++) {
//            File filein = in.get(i);
//            File fileout = out.get(i);
//            List<String> strings = IOOps.readLines(filein);
//            List<String> strings1 = IOOps.readLines(fileout);
//            for (int j = 0; j < strings.size(); j++) {
//                String sbegin = strings.get(j);
//                String s = strings.get(j);
//                s = s.replaceAll("\u00AD", "");
//                s = s.replaceAll("<gap/>", "");
//                s = s.replaceAll("<INS>", "");
//                s = s.replaceAll("  ", " ");
//                s = s.replaceAll("  ", " ");
//                s = s.replaceAll(" $", "");
//                s = s.replaceAll("^ ", "");
//                if (s.isEmpty()) {
//                    strings.remove(j--);
//                } else {
//                    if(!sbegin.equals(s)){
//                        System.out.println(sbegin);
//                        System.out.println(s);
//                        System.out.println();
//                    }
//                    strings.set(j, s);
//                }
//            }
//
//            for (int j = 0; j < Math.min(strings.size(), strings1.size()); j++) {
//                System.out.println(String.format("%50s \n %50s", strings.get(j), strings1.get(j)));
//                System.out.println();
//            }
//            System.out.println();
//            System.out.println();
//            IOOps.writeString(fileout, String.join("\n", strings));
//            IOOps.copyFile(filein, fileout);
//        }
//        System.exit(-1);
        if (args.length == 0) {
            args = ("-in /home/gundram/devel/projects/diss/data/icdar/valid/images "
                    + "-out /home/gundram/devel/projects/diss/data/icdar/valid/texts "
//                    + "-atr /home/gundram/devel/projects/diss/models/icdar_2017_20_0_planet "
//                    + "--help"
                    + "").split("\\s+");
        }
        Xml2Text te = new Xml2Text();
        ParamSet ps = new ParamSet();
        ps.setCommandLineArgs(args);    // allow early parsing
        ps = te.getDefaultParamSet(ps);
        ps = ParamSet.parse(ps, args, ParamSet.ParseMode.STRICT); // be strict, don't accept generic parameter
        te.setParamSet(ps);
        te.init();
        te.run();
    }


}
