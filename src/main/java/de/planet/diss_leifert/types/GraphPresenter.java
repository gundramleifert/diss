package de.planet.diss_leifert.types;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.file.FileSinkImages;
import org.graphstream.ui.geom.Point2;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.view.Camera;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;

//http://graphstream-project.org/doc/Advanced-Concepts/GraphStream-CSS-Reference/

public class GraphPresenter {
    static {
//        System.setProperty("gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        System.setProperty("gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        System.setProperty("org.graphstream.ui", "javafx");
    }

    public Graph graph = null;
    public List<Builder> elements = new LinkedList<>();
    Viewer viewer = null;
    //    public Set<EdgeBuilder> edges = new LinkedHashSet<>();
    private FileSinkImages pic = null;
    private File out = null;
    private boolean screenshot = false;
    private boolean printVerticesFirst = true;
    private Rectangle bounds = null;
    public static double factorX = 1.0;
    public static double factorY = 0.5;
    public static double factorVisibile = 10;
    public static String GREY = "grey";
    public static String REDGREY = "rgb(255,128,128)";


//    public static Node setColor(Node node, String color) {
//        node.addAttribute("ui.style", "fill-color: " + color + ";");
//        return node;
//    }

//    public static Edge stroke(Edge edge) {
//        edge.addAttribute("ui.style", "stroke-mode: dots;");
//        return edge;
//    }

    public static void main(String[] args) throws InterruptedException {
        MultiGraph graph = new MultiGraph("graph");
        Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        final View view = viewer.addDefaultView(true);
        Node edges = graph.addNode("1");
        edges.addAttribute("xy", 1, 1);
        Node edges2 = graph.addNode("2");
        edges2.addAttribute("xy", 1, 2);

        Node edges3 = graph.addNode("3");
        edges3.addAttribute("xy", 1, 3);
        graph.addEdge("13", edges, edges3);
        graph.addEdge("12", edges, edges2);
        graph.addEdge("23", edges2, edges3);
        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.CLOSE_VIEWER);
        GraphPresenter presenter = new GraphPresenter();
        presenter.init();
        int y = 5;
        int x = 10;
        for (int i = 0; i < x; i++) {
            presenter.add(new GraphPresenter.VertexBuilder(-1, i).setText("" + (i + 1)).setTextSize(20));
            for (int j = 0; j < y; j++) {
                presenter.add(new GraphPresenter.VertexBuilder(j, i).setColor(Color.RED).setShape(Shape.DIAMOND));
                if (j < y - 1) {
                    presenter.add(new GraphPresenter.EdgeBuilder(j, i, j + 1, i).setColor(Color.GREEN).setText("test" + j).setColor(Color.BLUE));
                    if (i < x - 1) {
                        presenter.add(new GraphPresenter.EdgeBuilder(j, i, j + 1, i + 1).setColor(Color.BLUE));
                    }
                }
            }
        }
        presenter.update();
        presenter.add(new GraphPresenter.EdgeBuilder(2, 2, 2, 5).setColor(Color.BLUE));
        File outputDir = new File("/tmp/out.png");
        presenter.setPic(new FileSinkImages(FileSinkImages.OutputType.PNG, FileSinkImages.Resolutions.PAL), outputDir);
        presenter.update();
//        presenter.save(new File("/home/gundram/devel/projects/diss/out.png"));

    }

    public void setPic(FileSinkImages pic, File outputFileOrDir) {
        screenshot = !outputFileOrDir.isDirectory();
        if (screenshot) {
            File parentFile = outputFileOrDir.getParentFile();
            if (parentFile != null) {
                parentFile.mkdirs();
            }
        } else {
            outputFileOrDir.mkdirs();
        }
        this.out = outputFileOrDir;
        this.pic = pic;
    }

    public boolean add(EdgeBuilder edgeBuilder) {
        return elements.add(edgeBuilder);
    }

    public boolean add(VertexBuilder vertexBuilder) {
        return elements.add(vertexBuilder);
    }

    public void setViewBounds(Rectangle bounds) {
        this.bounds = bounds;
    }

    public void init() {
        graph = new MultiGraph("graph");
        graph.setAutoCreate(true);
        viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
//        viewer.enableAutoLayout();
        final View view = viewer.addDefaultView(true);
//        view.getCamera().setViewPercent(1);
        ((Component) view).addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                e.consume();
                int i = e.getWheelRotation();
                double factor = Math.pow(1.25, i);
                Camera cam = view.getCamera();
                double zoom = cam.getViewPercent() * factor;
                Point2 pxCenter = cam.transformGuToPx(cam.getViewCenter().x, cam.getViewCenter().y, 0);
                Point3 guClicked = cam.transformPxToGu(e.getX(), e.getY());
//                System.out.println("point of Curser:(orig): " + e.getX() + " x " + e.getY());
//                System.out.println("point of Curser ( GU ): " + guClicked.x + " x " + guClicked.y);
//                System.out.println("point of Curser:(orig): " + ((Component) view).getWidth() + " x " + ((Component) view).getHeight());
//                System.out.println("point of Curser ( GU ): " + cam.transformPxToGu(((Component) view).getWidth(), ((Component) view).getHeight()).x + " x " + cam.transformPxToGu(((Component) view).getWidth(), ((Component) view).getHeight()).y);
//                System.out.println("point of Curser:(orig): " + 0 + " x " + 0);
//                System.out.println("point of Curser ( GU ): " + cam.transformPxToGu(0, 0).x + " x " + cam.transformPxToGu(0, 0).y);
                double newRatioPx2Gu = cam.getMetrics().ratioPx2Gu / factor;
                double x = guClicked.x + (pxCenter.x - e.getX()) / newRatioPx2Gu;
                double y = guClicked.y - (pxCenter.y - e.getY()) / newRatioPx2Gu;
                cam.setViewCenter(x, y, 0);
                cam.setViewPercent(zoom);
//                System.out.println("view percent " + cam.getViewPercent());
//                System.out.println("view center " + cam.getViewCenter());
//                System.out.println("ratioPx2Gu:" + cam.getMetrics().ratioPx2Gu);
//                System.out.println("size: ");
//                ((Component) view).getSize();
//                System.out.println("stop");
                System.out.println("zoom is " + zoom);

            }
        });
        ((Component) view).addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
//                if (e.getKeyChar() == 'r') {
//                    Camera cam = view.getCamera();
//                    double zoom = cam.getViewPercent();
//
//                    Point2 pxCenter = cam.transformGuToPx(cam.getViewCenter().x, cam.getViewCenter().y, 0);
////                    Point3 guClicked = cam.transformPxToGu(e.getX(), e.getY());
////                    double newRatioPx2Gu = cam.getMetrics().ratioPx2Gu / factor;
////                    double x = guClicked.x + (pxCenter.x - e.getX()) / newRatioPx2Gu;
////                    double y = guClicked.y - (pxCenter.y - e.getY()) / newRatioPx2Gu;
////                    cam.setViewCenter(x, y, 0);
//                    cam.setViewPercent(zoom);
//                }
                if (e.getKeyChar() == 'p') {
                    File outTmp = out;
                    double zoom = view.getCamera().getViewPercent();
                    int f = (int) Math.round(Math.log(zoom) / Math.log(1.25));

//                    if (outTmp.exists()) {
                    File folder = out.getParentFile();
                    String name = out.getName();
                    int idx = 0;
                    int idxDot = name.lastIndexOf(".");
                    String prefix = name.substring(0, idxDot);
                    String suffix = name.substring(idxDot);
                    while (true) {
                        outTmp = new File(folder, String.format("%s_%01d_%02d%s", prefix, f, idx, suffix));
                        if (!outTmp.exists()) {
                            break;
                        }
                        idx++;
                    }
                    System.out.println("zoom is " + zoom + " / " + f);
                    graph.addAttribute("ui.screenshot", outTmp.getAbsolutePath());
                    System.out.println("save image to " + outTmp.getAbsolutePath());
                }
//                    try {
//                        Camera cam = view.getCamera();
//                        Point3 viewCenter = cam.getViewCenter();
//                        System.out.println("view center = "+viewCenter);
//                        pic.setViewCenter(((Component) view).getX(), ((Component) view).getY());
//                        pic.setViewPercent(cam.getViewPercent());
//                        pic.writeAll(graph, out.getAbsolutePath());
//                    } catch (IOException ex) {
//                        throw new RuntimeException(ex);
//                    }
//                    ((Component) view).
//                }
//                Camera camera = view.getCamera();
//                camera.setViewCenter(bounds.x + bounds.width * 0.5, bounds.y + bounds.height * 0.5, 0);
//                System.out.println("view percent " + camera.getViewPercent());
//                System.out.println("view center " + camera.getViewCenter());
//                ((Component) view).setSize(1000, 500);

            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
//        view.
//        ((Component) view).print();

    }

    public synchronized void update() {
//        graph.addAttribute("node", "text-size: 20;");
        int i = 0;
        if (printVerticesFirst) {
            elements.sort(new Comparator<Builder>() {
                @Override
                public int compare(Builder o1, Builder o2) {
                    boolean b1 = o1 instanceof VertexBuilder;
                    boolean b2 = o2 instanceof VertexBuilder;
                    int res = Boolean.compare(b2, b1);
                    if (res != 0 || b1) {
                        return res;
                    }
                    int abs1 = Math.abs(((EdgeBuilder) o1).pos[0] - ((EdgeBuilder) o1).posPrev[0] + 1);
                    int abs2 = Math.abs(((EdgeBuilder) o2).pos[0] - ((EdgeBuilder) o2).posPrev[0] + 1);
                    return Integer.compare(abs2, abs1);
                }
            });
        }
        for (Builder vertexOrEdge : elements) {
            boolean builded = vertexOrEdge.build(graph);
            if (pic != null && builded && !screenshot) {
                int len = (int) Math.log10(elements.size()) + 1;
                final String outName = "img_%0" + len + "d.png";
                try {
                    this.pic.writeAll(graph, new File(out, String.format(outName, i++)).getAbsolutePath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        elements.clear();
        if (pic != null && screenshot) {
            try {
                pic.writeAll(graph, out.getAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
//        Viewer display = graph.display(false);
//        if (bounds != null) {
//            viewer.getDefaultView().getCamera().setBounds(bounds.x, bounds.y, 0, bounds.x + bounds.width, bounds.y + bounds.width, 0);
//        }
        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.CLOSE_VIEWER);
    }

    public enum Stroke {
        SOLID("plain"),
        DASHES("dashes"),
        DOTTED("dots");
        private final String name;

        Stroke(String style) {
            this.name = style;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public enum TextAlignment {
//center (default): The text will be centered on the element center.
//left: The text will be aligned on the left of the element center.
//right: The text will be aligned on the right or the element center.
//at-left: The text will be aside the element at left.
//at-right: The text will be aside the element at right.
//under: The text will be under the element.
//above: The text will be above the element.
//along: This is useful only for edges, the text will centered on the edge and will have the same orientation as the edge.

        CENTER("center");
        private final String name;

        TextAlignment(String value) {
            this.name = value;
        }

        @Override
        public String toString() {
            return name;
        }

    }

    public enum Color {
        GREY(GraphPresenter.GREY),
        RED("red"),
        REDGREY(GraphPresenter.REDGREY),
        BLUE("blue"),
        BLACK("black"),
        GREEN("green"),
        //        INVISIBLE("white"),
        WHITE("white");
        private final String name;

        Color(String color) {
            name = color;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public enum Shape {
        BOX("box"),
        CIRCLE("circle"),
        DIAMOND("diamond"),
        CROSS("cross");
        private final String name;

        Shape(String shape) {
            name = shape;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public abstract static class Builder<Type> {
        HashMap<String, String> styles = new LinkedHashMap<>();
        String name;
        String text = null;

        public Builder(String name) {
            this.name = name;
        }

        public abstract boolean build(Graph graph);

        public Type setColor(Color color) {
            setStyle("fill-color", color.toString());
            setStyle("stroke-color", color.toString());
//            if(styles.containsKey())
            return (Type) this;
        }

        public Type setTextColor(Color color) {
            setStyle("text-color", color.toString());
//            if(styles.containsKey())
            return (Type) this;
        }

        public Type setStyle(String key, String value) {
            styles.put(key, value);
            return (Type) this;
        }

        public Type setIndex(int index) {
            styles.put("z-index", String.valueOf(index));
            return (Type) this;
        }

        public Type setInvisible() {
            setStyle("visibility-mode", "hidden");
            return (Type) this;
        }


        public Type setTextSize(int size) {
            return setStyle("text-size", String.valueOf(size));
        }

        public Type setVisiblility(double level) {
            setStyle("visibility-mode", "under-zoom");
            return setStyle("visibility", String.valueOf(level * factorVisibile));
//            return (Type) this;
        }

        protected String getStyle() {
            StringBuilder sb = new StringBuilder();
            for (String s : styles.keySet()) {
                sb.append(s).append(" : ").append(styles.get(s)).append(" ; ");
            }
            return sb.toString().trim();
        }
    }

    public static class VertexBuilder extends Builder<VertexBuilder> {
        int[] pos;

        public VertexBuilder(int y, int x) {
            this(new int[]{y, x});
        }

        public VertexBuilder(int[] pos) {
            super(String.format("%03d-%03d", pos[0], pos[1]));
            this.pos = pos;
        }

        public VertexBuilder setSize(int i) {
            return setStyle("size", String.valueOf(i));
        }

        public VertexBuilder setShape(Shape shape) {
            return setStyle("shape", shape.toString());
        }

        public VertexBuilder setText(String text) {
            this.text = text;
            setStyle("fill-color", Color.WHITE.toString());
            setStyle("text-size", String.valueOf(60));
            return this;
        }

        @Override
        public boolean build(Graph g) {
            if (g.getNode(name) != null) {
                System.out.println("node " + name + " already exists - skip build");
                return false;
            }
            Node node = g.addNode(name);
            node.addAttribute("xy", pos[1] * factorX, -pos[0] * factorY);
            // shape: box;
            if (text != null) {
                node.addAttribute("ui.label", text);
            }
            node.addAttribute("ui.style", getStyle());
            return true;
        }

    }

    public static class EdgeBuilder extends Builder<EdgeBuilder> {
        int[] posPrev;
        int[] pos;
        String text;
        int curved = 0;

        public EdgeBuilder(int yPRev, int xPrev, int y, int x) {
            this(new int[]{yPRev, xPrev}, new int[]{y, x});
        }

        public EdgeBuilder(int[] posPrev, int[] pos) {
            super(String.format("%03d-%03d_%03d-%03d", posPrev[0], posPrev[1], pos[0], pos[1]));
            setStyle("stroke-mode", "plain");
            setStyle("stroke-width", "2px");
            this.posPrev = posPrev;
            this.pos = pos;
        }

        public EdgeBuilder setText(String text) {
            this.text = text;
            setStyle("text-alignment", "along");
            setStyle("text-size", String.valueOf(20));
            return this;
        }

        public EdgeBuilder setCurvedLeft() {
            curved = -1;
            return this;
        }

        public EdgeBuilder setCurvedRight() {
            curved = 1;
            return this;
        }

        public EdgeBuilder setStroke(Stroke stroke) {
            return setStyle("stroke-mode", stroke.toString());
        }

        private static double[][] relpos = new double[][]{{0.2, 0.02}, {0.5, 0.03}, {0.8, 0.02}};

        private void addCurvedEdge(Graph graph) {
            Node start = graph.getNode(String.format("%03d-%03d", posPrev[0], posPrev[1]));
            Node end = graph.getNode(String.format("%03d-%03d", pos[0], pos[1]));
            Node last = start;
            double[] dir90 = new double[]{posPrev[1] - pos[1], posPrev[0] - pos[0]};
            double len = Math.sqrt(dir90[0] * dir90[0] + dir90[1] * dir90[1]);
            for (int i = 0; i < relpos.length; i++) {
                double c = relpos[i][1];
                double la = relpos[i][0];
                double la_ = 1 - la;
//                dir90[0] = abs;
//                dir90[1] /= abs;
                double[] posNew = new double[]{pos[0] * la + posPrev[0] * la_ + dir90[0] * c, pos[1] * la + posPrev[1] * la_ + dir90[1] * c};
//                double Y = pos[0] * relpos[i][0] + posPrev[0] * (1 - relpos[i][0]) + curved * (pos[1] - posPrev[1]) * relpos[i][1];
//                double X = pos[1] * relpos[i][0] + posPrev[1] * (1 - relpos[i][0]) + curved * (pos[0] - posPrev[0]) * relpos[i][1];
                Node tmp = graph.addNode(String.format("%s_%dv", name, i));
                tmp.addAttribute("xy", posNew[1] * factorX, -posNew[0] * factorY);
                tmp.addAttribute("ui.style", "visibility-mode: hidden;");
                Edge res = graph.addEdge(name + "_" + i, last, tmp, false);
                res.addAttribute("ui.style", getStyle());
                last = tmp;
            }
            Edge res = graph.addEdge(name + "_" + relpos.length, last, end, true);
            if (text != null) {
                res.addAttribute("ui.label", text);
            }
            res.addAttribute("ui.style", getStyle());
        }

        @Override
        public boolean build(Graph g) {
            if (g.getEdge(name) != null) {
                name += "_";
                System.out.println("edge " + name + " already exists build twice with name " + name);
            }
            if (curved != 0) {
                addCurvedEdge(g);
            } else {
                Node nodePrev = g.getNode(String.format("%03d-%03d", posPrev[0], posPrev[1]));
                Node node = g.getNode(String.format("%03d-%03d", pos[0], pos[1]));
                Edge res = g.addEdge(name, nodePrev, node, true);
                if (text != null) {
                    res.addAttribute("ui.label", text);
                }
                res.addAttribute("ui.style", getStyle());
            }
            return true;
        }

    }

}
