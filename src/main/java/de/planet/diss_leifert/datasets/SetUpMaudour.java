//package de.planet.diss_leifert.datasets;
//
//import de.planet.trainset_util.util.IOOps;
//import de.planet.xml_helper.JsonHelper;
//import de.planet.xml_helper.maudour.XmlHelperMaudour;
//import de.planet.xml_helper.types.XFile;
//import de.planet.xml_helper.types.XPage;
//
//import java.io.File;
//import java.util.Collection;
//import java.util.LinkedList;
//import java.util.List;
//
//public class SetUpMaudour {
//
//    private static List<File> getImages(Collection<File> images, String name) {
//        List<File> res = new LinkedList<>();
//        for (File image : images) {
//            if (image.getName().startsWith(name)) {
//                String name2 = image.getName().substring(name.length());
//                if (name2.charAt(0) == '.' || Integer.parseInt(name2.substring(1, name2.lastIndexOf('.'))) >= 0) {
//                    res.add(image);
//                }
//            }
//        }
//        return res;
//    }
//
//    public static void main(String[] args) {
//        File homeDir = new File("/home/gundram/devel/projects/maudour");
//        File out = new File(homeDir, "data/sep");
//        Collection<File> filesXml = IOOps.listFiles(new File(homeDir, "data/MAURDOR/ADDITIONAL_DATA/E0045/MAURDOR_00/DATA/SOURCEDATA/DEV/XML/xml-m5"), "xml", true);
//        Collection<File> filesImg = IOOps.listFiles(new File(homeDir, "data/MAURDOR/DEV/TIFF/"), "jpg", true);
//        System.out.println(filesXml.size());
//        System.out.println(filesImg.size());
//        for (File file : filesXml) {
//            String name = file.getName();
//            name = name.substring(0, name.lastIndexOf('.'));
//            XFile xFileFromFile = XmlHelperMaudour.getXFileFromFile(file);
//            List<File> images = getImages(filesImg, name);
//            List<XPage> pages = xFileFromFile.getPages();
//            if (images.size() != pages.size()) {
//                xFileFromFile = XmlHelperMaudour.getXFileFromFile(file);
//                throw new RuntimeException("size has to be the same");
//            }
//            for (int i = 0; i < images.size(); i++) {
//                String nameOut = name + "-" + i;
//                File folderOut = new File(out, name);
//                folderOut.mkdirs();
//                nameOut += ".jpg";
//                File file1 = new File(folderOut, nameOut);
//                IOOps.symbolicLink(file1, images.get(i));
//                XFile xFileOut = new XFile(nameOut);
//                xFileOut.addPage(pages.get(i));
//                IOOps.writeString(new File(folderOut, nameOut + ".json"), JsonHelper.serialize(xFileOut));
//            }
//            System.out.println(images.size());
//        }
//
////        System.out.println(xFileFromFile);
////        IOOps.writeString(new File(homeDir,"test.json"), JsonHelper.serialize(xFileFromFile));
//    }
//
//}
