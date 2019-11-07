package de.planet.diss_leifert.workflow;

import de.planet.diss_leifert.util.FileUtil;
import de.planet.trainset_util.util.IOOps;
import de.planet.xml_helper.JsonHelper;
import de.planet.xml_helper.types.XFile;
import de.planet.xml_helper.types.XLine;
import de.planet.xml_helper.types.XPage;
import de.planet.xml_helper.types.XRegion;
import de.planet.xml_helper.util.XmlDomHelper;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Xml2XFile {
    public static void main(String[] args) throws IOException {


        File folderIn = new File("/home/gundram/devel/projects/diss/data/bentham/valid/images");
        File folderOut = new File("/home/gundram/devel/projects/diss/data/bentham/valid/xfiles_gt");
        boolean exportTranscripts = true;

        boolean exportCoords = true;//false is not implemented correctly
        List<File> xml = IOOps.listFiles(folderIn, "xml", true);
        xml.removeIf(file -> file.getName().equals("mets.xml") || file.getName().equals("metadata.xml") || file.getName().equals("doc.xml"));
        folderOut.mkdirs();
        System.out.println("number of files in gt :" + xml.size());
        for (File infile : xml) {
            System.out.println(infile);
            XmlDomHelper.Doc doc = XmlDomHelper.loadXML(infile);
            List<Node> pages = XmlDomHelper.getElementsByName(doc, "Page");
            if (pages.size() != 1) {
                throw new RuntimeException(" wrong number of pages " + pages.size());
            }
            XmlDomHelper.getAttr(pages.get(0), "imageFilename");
            XFile xFile = new XFile(XmlDomHelper.getAttr(pages.get(0), "imageFilename"));
            XPage page = new XPage();
            xFile.addPage(page);
            for (Node textRegion : XmlDomHelper.getElementsByName(doc, "TextRegion")) {
                Node coordsr = XmlDomHelper.getChild(textRegion, "Coords");
                XRegion xRegion = null;
                try {
                    xRegion = new XRegion(XmlDomHelper.string2Polygon2DInt(XmlDomHelper.getAttr(coordsr, "points")));
                } catch (IllegalStateException ex) {
                    xRegion = new XRegion(null);
                }
                page.addRegion(xRegion);
                for (Node textLine : XmlDomHelper.getChildren(textRegion, "TextLine")) {
                    Node coords = exportCoords ? XmlDomHelper.getChild(textLine, "Coords") : null;
                    XLine xLine = new XLine(XmlDomHelper.string2Polygon2DInt(XmlDomHelper.getAttr(coords, "points")));
                    xRegion.addLine(xLine);
                    Node baseline = XmlDomHelper.getChild(textLine, "Baseline");
                    xLine.setBaseLine(XmlDomHelper.string2Polygon2DInt(XmlDomHelper.getAttr(baseline, "points")));
                    Node textEquiv = XmlDomHelper.getChild(textLine, "TextEquiv");
                    if (textEquiv != null && exportTranscripts) {
                        Node unicode = XmlDomHelper.getChild(textEquiv, "Unicode");
                        String s = unicode.getTextContent();
                        s = s.replace("&amp;", "&");
                        s = s.replace("&apos;", "'");
                        s = s.replace("&lt;", "<");
                        s = s.replace("&gt;", ">");
                        s = s.replace("&quot;", "\"");

                        s = s.replace("<gap/>", "");
                        s = s.replace("<INS>", "");
                        s = s.replaceAll("\\s+", " ");
                        s=s.trim();
                        xLine.setText(s);
                        if (XmlDomHelper.hasAttr(textEquiv, "conf")) {
                            xLine.setTextConf(Double.parseDouble(XmlDomHelper.getAttr(textEquiv, "conf")));
                        } else {
                            xLine.setTextConf(1.0);
                        }
                    }
                }
            }
            File tgtFile = FileUtil.getTgtFile(folderIn, folderOut, infile);
            File out = new File(tgtFile.getPath().replace(".xml", ".json").replace("/page", ""));
            System.out.println(out);
            JsonHelper.serialize(out, xFile);
        }


    }
}
