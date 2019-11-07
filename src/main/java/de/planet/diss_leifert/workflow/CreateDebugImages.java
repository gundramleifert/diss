package de.planet.diss_leifert.workflow;

import com.achteck.misc.exception.InvalidParameterException;
import com.achteck.misc.param.ParamSet;
import com.achteck.misc.types.ParamAnnotation;
import de.planet.diss_leifert.util.FileUtil;
import de.planet.diss_leifert.util.ImageUtil;
import de.planet.imaging.types.HybridImage;
import de.planet.trainset_util.util.IOOps;
import de.planet.xml_helper.JsonHelper;
import de.planet.xml_helper.types.XFile;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public class CreateDebugImages extends FolderOrganizer {

    @ParamAnnotation
    private boolean gt = false;

    @ParamAnnotation
    private String name = "";

    @ParamAnnotation(descr = "show regions")
    private boolean r = false;

    @ParamAnnotation(descr = "draw baselines")
    private double b = 0.0;

    @ParamAnnotation(descr = "draw sourrunding polygons")
    private boolean p = false;

    @ParamAnnotation(descr = "show text on same page")
    private boolean t = false;

    @ParamAnnotation(descr = "show confidences")
    private boolean c = false;

//    @ParamAnnotation
//    private String out = "";

    public CreateDebugImages() {
        addReflection(this, CreateDebugImages.class);
    }

    public void run() {
        File folderXFiles, folderOut;
        if (gt) {
            folderXFiles = getFolderXFilesGT();
            folderOut = getFolderDebugGT();
        } else {
            folderXFiles = getFolderResultFiles();
            folderOut = getFolderDebug();
        }
        File folderImages = getFolderImages();
        List<File> json = IOOps.listFiles(folderXFiles, "json", true);
        if (!name.isEmpty()) {
            json.removeIf(file -> !file.getName().contains(name));
        }
        for (File file : json) {
            String name = file.getName().substring(0, file.getName().lastIndexOf("."));
            XFile deserialize = JsonHelper.deserialize(file);
            if (deserialize.getPages().size() != 1) {
                throw new RuntimeException("expect only one page per xfile, not " + deserialize.getPages().size());
            }
            File fileImg = getTgtFile(folderXFiles, folderImages, file,"jpg");
            HybridImage hybridImage = HybridImage.newInstance(fileImg);

            BufferedImage debugImage = ImageUtil.getDebugImage(hybridImage.getAsBufferedImage(),
                    deserialize.getPages().get(0),
                    1.0,
                    t,
                    r,
                    b,
                    p,
                    !c);
//            if (out.isEmpty()) {
//                showImage(HybridImage.newInstance(debugImage));
//            } else {
//                HybridImage hi = HybridImage.newInstance(FileUtil.getTgtFile(folderXFiles, folderOut, file));
            File tgtFile1 = FileUtil.getTgtFile(folderXFiles, folderOut, file);
            HybridImage.newInstance(debugImage).save(tgtFile1.getPath().replace(".json", ".png"));
//            }
        }

    }

    private static Object lock = new Object();

    public void showImage(HybridImage img) {
//        if (img.getHeight() > 1000) {
//            img = ImageHelper.scale(1000.0 / img.getHeight(), img);
//        }
//        BufferedImage img= ImageIO.read(new File("f://images.jpg"));
        ImagePanel panel = new ImagePanel(img.getAsBufferedImage());
        ImageZoom zoom = new ImageZoom(panel);
        panel.setScale(1050.0 / img.getHeight());
        panel.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int notches = e.getWheelRotation();
                if (notches > 0) {
                    panel.setScale(panel.getScale() * 0.9);
                } else {
                    panel.setScale(panel.getScale() / 0.9);
                }
            }
        });
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.getContentPane().add(zoom.getUIPanel(), "North");
        f.getContentPane().add(new JScrollPane(panel));
        f.setSize(1200, 800);
        f.setLocation(100, 200);
        f.setVisible(true);
    }

    class ImagePanel extends JPanel {
        BufferedImage image;
        double scale;

        public ImagePanel(BufferedImage image) {
            this.image = image;
//            loadImage();
            scale = 1.0;
            setBackground(Color.black);
        }

        public double getScale() {
            return scale;
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            int w = getWidth();
            int h = getHeight();
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            double x = (w - scale * imageWidth) / 2;
            double y = (h - scale * imageHeight) / 2;
            AffineTransform at = AffineTransform.getTranslateInstance(x, y);
            at.scale(scale, scale);
            g2.drawRenderedImage(image, at);
        }

        /**
         * For the scroll pane.
         */
        public Dimension getPreferredSize() {
            int w = (int) (scale * image.getWidth());
            int h = (int) (scale * image.getHeight());
            return new Dimension(w, h);
        }

        public void setScale(double s) {
            scale = s;
            revalidate();      // update the scroll pane
            repaint();
        }

    }

    class ImageZoom {
        ImagePanel imagePanel;

        public ImageZoom(ImagePanel ip) {
            imagePanel = ip;
        }

        public JPanel getUIPanel() {
            SpinnerNumberModel model = new SpinnerNumberModel(1.0, 0.1, 1.4, .01);
            final JSpinner spinner = new JSpinner(model);
            spinner.setPreferredSize(new Dimension(45, spinner.getPreferredSize().height));
            spinner.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    float scale = ((Double) spinner.getValue()).floatValue();
                    imagePanel.setScale(scale);
                }
            });
            JPanel panel = new JPanel();
            panel.add(new JLabel("scale"));
            panel.add(spinner);
//            JButton button = new Button("next");
//            panel.add(button)
            return panel;
        }
    }

    public static void main(String[] args) throws InvalidParameterException, InterruptedException {
//        List<File> jpg = IOOps.listFiles(new File("/home/gundram/devel/projects/diss/data/bentham/valid/images/"), "jpg", false);
//        jpg = jpg.subList(40, jpg.size());
//        jpg.removeIf(file -> file.getName().equals("002_393_001.jpg"));
//        for (File file : jpg) {
//            String name = file.getName().substring(0, file.getName().lastIndexOf("."));
//        if (args.length == 0) {
        args = (""
                +"-mainset bentham "
                +"-subset valid "
                +"-net icdar_2017_20_0_planet "
                +"-config conf-0.0_dist-false_skipW-3.2_skipB-4.0_anyC-4.0_jumpB-4.0_hyp-null_hypProp-null_cert-MAX_size-3 "
//                    +"-img /home/gundram/devel/projects/diss/data/bentham/valid/images/" + name + ".jpg " +
//                    "-xml /home/gundram/devel/projects/diss/data/bentham/valid/xfiles_gt/" + name + ".json "
//                    "-xml /home/gundram/devel/projects/diss/results/bentham/icdar_2017_20_0_planet/valid/icdar_2017_20_0_planet/conf-0.0_dist-false_skipW-1.6_skipB-4.0_anyC-8.0_jumpB-null_hyp-null_hypProp-null_cert-MAX_size-3/" + name + ".json "
//                    + "-out /home/gundram/devel/projects/diss/debug/bentham/icdar_2017_20_0_planet/valid/conf-0.0_dist-false_skipW-1.6_skipB-4.0_anyC-8.0_jumpB-null_hyp-null_hypProp-null_cert-MAX_size-3/" + name + ".png "
//                    + "-atr /home/gundram/devel/projects/diss/models/icdar_2017_20_0_planet "
                + "-c true "
                + "-b 0.001 "
//                    + "--help"
                + "").split("\\s+");
//        }
        CreateDebugImages te = new CreateDebugImages();
        ParamSet ps = new ParamSet();
        ps.setCommandLineArgs(args);    // allow early parsing
        ps = te.getDefaultParamSet(ps);
        ps = ParamSet.parse(ps, args, ParamSet.ParseMode.STRICT); // be strict, don't accept generic parameter
        te.setParamSet(ps);
        te.init();
        te.run();
//            Thread.sleep(2000);
//        }
    }

}
