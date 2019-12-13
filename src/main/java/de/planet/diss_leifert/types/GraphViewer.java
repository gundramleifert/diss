package de.planet.diss_leifert.types;

import de.uros.citlab.confmat.CharMap;
import de.uros.citlab.errorrate.types.PathCalculatorGraph;
import de.uros.citlab.textalignment.costcalculator.CostCalculatorJumpConfMat;
import de.uros.citlab.textalignment.costcalculator.CostCalculatorSkipConfMat;
import de.uros.citlab.textalignment.costcalculator.CostCalculatorSkipWord;
import de.uros.citlab.textalignment.types.ConfMatVector;
import de.uros.citlab.textalignment.types.NormalizedCharacter;
import org.graphstream.stream.file.FileSinkImages;

import java.io.File;

import static de.planet.diss_leifert.types.GraphPresenter.Color;

public class GraphViewer implements PathCalculatorGraph.PathFilter<ConfMatVector, NormalizedCharacter> {
    GraphPresenter presenter;
    int counter = 0;
    int minx, miny, maxx, maxy;
    boolean forced = false;
    private ConfMatVector[] cms;
    private NormalizedCharacter[] chars;
    File out;
    public static boolean printXNumbers = false;
    public static Boolean invisible = null;
//    FileSourceDGS dgs = null;
//    FileSinkImages fsi = null;

    @Override
    public void init(ConfMatVector[] cms, NormalizedCharacter[] chars) {
        this.cms = cms;
        this.chars = chars;
        presenter = new GraphPresenter();
//        presenter.setViewBounds(new Rectangle(minx, miny, maxx - minx, maxy - miny));
        presenter.init();
        if (!forced) {
            minx = chars.length;
            maxx = 0;
            miny = cms.length;
            maxy = 0;
        }
//        fsi = new FileSinkImages(
//                "./outDir/",
//                FileSinkImages.OutputType.PNG,
//                FileSinkImages.Resolutions.HD1080,
//                FileSinkImages.OutputPolicy.BY_EDGE_ADDED_REMOVED
//        );
//        dgs = new FileSourceDGS();
//        dgs.addSink(fsi);
//        dgs.
        if (out != null) {
            presenter.setPic(new FileSinkImages(FileSinkImages.OutputType.PNG, FileSinkImages.Resolutions.UHD_4K),
                    out);
        }

    }

    public void setOut(File out) {
        this.out = out;
    }

    public void setStyle(int[] pos, GraphPresenter.VertexBuilder vertex) {
        vertex.setStyle("stroke-width", "3");
        NormalizedCharacter aChar = chars[pos[1]];
        ConfMatVector cm = cms[pos[0]];
        if (aChar == null || cm == null) {
            vertex.setColor(Color.WHITE);
            return;
        }
        if (cm.isReturn) {
            vertex.setSize(20);
        } else if (aChar.isNaC) {
            vertex.setSize(10);
        } else {
            vertex.setSize(15);
        }
        switch (aChar.type) {
            case SpaceLineBreak:
                if (cm.isReturn) {
                    vertex.setColor(Color.GREEN);
                } else {
                    setVisibility(vertex.setColor(Color.BLACK), 0.5);
                }
                break;
            case ReturnLineBreak:
                if (cm.isReturn) {
                    vertex.setColor(Color.RED);
                } else {
                    setVisibility(vertex.setColor(Color.BLACK), 0.5);
                }
                break;
            case HyphenLineBreak:
                vertex.setColor(Color.BLUE); //probably overwritten, because aChar.isHyphen== true => grey
                break;
            default:
                setVisibility(vertex, 0.5);
        }
        if (aChar.isHyphen) {
            vertex.setShape(GraphPresenter.Shape.DIAMOND).setColor(Color.GREY);
        }

    }

    public void setStyle(PathCalculatorGraph.DistanceSmall distance, GraphPresenter.EdgeBuilder edge) {
        Object costCalculator = distance.costCalculator;
//        double costs = costCalculator instanceof PathCalculatorGraph.ICostCalculator ?
//                ((PathCalculatorGraph.ICostCalculator) costCalculator).getNeighbour(distance).getCosts() :
//                ((PathCalculatorGraph.ICostCalculatorMulti) costCalculator).getNeighbour(distance).getCosts();
//        edge.setText(distance.costCalculator.getClass().getSimpleName().replace("CostCalculator", ""));
//        edge.setText(String.format("%.3f", costs));
        edge.setIndex(-10);
        NormalizedCharacter charStart = chars[distance.pointPrevious[1]];
        NormalizedCharacter charEnd = chars[distance.point[1]];
        if (costCalculator instanceof CostCalculatorSkipWord) {
            if (charStart.isHyphen || charEnd.isHyphen) {
                edge.setColor(Color.REDGREY);
            } else {
                edge.setColor(Color.RED);
            }
            if (!chars[distance.point[1]].type.equals(NormalizedCharacter.Type.SpaceLineBreak)) {
                edge.setCurvedLeft();
            } else {
                edge.setCurvedRight();
            }
        } else if (costCalculator instanceof CostCalculatorSkipConfMat) {
            if (charStart.isHyphen || charEnd.isHyphen) {
                edge.setColor(Color.REDGREY);
            } else {
                edge.setColor(Color.RED);
            }
        } else if (costCalculator instanceof CostCalculatorJumpConfMat) {
            edge.setColor(Color.BLUE).setCurvedLeft();
        } else {
            setVisibility(edge, 0.5);
            if (charStart.isHyphen || charEnd.isHyphen) {
                edge.setIndex(-2);
                edge.setColor(Color.GREY);
            } else {
                edge.setIndex(-1);
                edge.setColor(Color.BLACK);
            }
        }
    }

    public static void setVisibility(GraphPresenter.Builder builder, double zoom) {
        if (invisible == null) {
            builder.setVisiblility(zoom);
            return;
        }
        if (invisible) {
            builder.setInvisible();
        }

    }

    public void setView(int miny, int minx, int maxy, int maxx) {
        this.minx = minx;
        this.miny = miny;
        this.maxx = maxx;
        this.maxy = maxy;
        forced = true;
    }

    private void initAxis() {
        int idxCM = 0;
        int idxLine = 0;
        for (int j = Math.max(1, miny); j < Math.min(maxy + 1, cms.length); j++) {
            ConfMatVector cm = cms[j];
            String name = null;
            if (cm.isReturn) {
                idxCM++;
                idxLine = 0;
                name = CharMap.Return + "";
            } else {
                idxLine++;
                name = idxCM + "-" + idxLine;
            }
            GraphPresenter.VertexBuilder vertexBuilder = new GraphPresenter.VertexBuilder(j, minx).setText(name).setTextSize(25);
            GraphPresenter.VertexBuilder vertexBuilder2 = new GraphPresenter.VertexBuilder(j, maxx + 1).setText(name).setTextSize(25);
            presenter.add(vertexBuilder);
            presenter.add(vertexBuilder2);
        }
        for (int i = Math.max(1, minx); i < Math.min(maxx + 1, chars.length); i++) {

            NormalizedCharacter aChar = chars[i];//23B5
            String s = aChar.orig == '\n' ? "" + CharMap.Return : aChar.isNaC ? "\u2205" : aChar.orig == ' ' ? "\u23B5" : String.valueOf(aChar.orig);
            if (printXNumbers) {
                GraphPresenter.VertexBuilder vertexBuilder = new GraphPresenter.VertexBuilder(miny - 1, i).setText("" + i).setTextSize(25);
                GraphPresenter.VertexBuilder vertexBuilder4 = new GraphPresenter.VertexBuilder(maxy + 2, i).setText("" + i).setTextSize(25);
                if (s.equals("\u2205")) {
                    setVisibility(vertexBuilder, 0.5);
                    setVisibility(vertexBuilder4, 0.5);
                }
                presenter.add(vertexBuilder);
                presenter.add(vertexBuilder4);
            }
            GraphPresenter.VertexBuilder vertexBuilder2 = new GraphPresenter.VertexBuilder(maxy + 1, i).setText(s).setTextSize(40);
            GraphPresenter.VertexBuilder vertexBuilder3 = new GraphPresenter.VertexBuilder(miny - 0, i).setText(s).setTextSize(40);
            if (aChar.isHyphen) {
                vertexBuilder2.setTextColor(Color.GREY);
                vertexBuilder3.setTextColor(Color.GREY);
            }
            if (s.equals("\u2205")) {
                setVisibility(vertexBuilder2, 0.5);
                setVisibility(vertexBuilder3, 0.5);
            }
            presenter.add(vertexBuilder2);
            presenter.add(vertexBuilder3);
        }
//        presenter.setBounds(new Rectangle(minx, miny, maxx - minx, maxy - miny));
//        presenter.add(new GraphPresenter.VertexBuilder(0, 0));

    }

    boolean pass = false;

    @Override
    public boolean addNewEdge(PathCalculatorGraph.DistanceSmall distanceSmall) {
        if (pass) {
            return true;
        }
        if (!forced) {
            miny = Math.min(miny, distanceSmall.pointPrevious[0]);
            miny = Math.min(miny, distanceSmall.point[0]);
            maxy = Math.max(maxy, distanceSmall.pointPrevious[0]);
            maxy = Math.max(maxy, distanceSmall.point[0]);
            minx = Math.min(minx, distanceSmall.pointPrevious[1]);
            minx = Math.min(minx, distanceSmall.point[1]);
            maxx = Math.max(maxx, distanceSmall.pointPrevious[1]);
            maxx = Math.max(maxx, distanceSmall.point[1]);
        } else {
            if (miny > distanceSmall.pointPrevious[0] || miny > distanceSmall.point[0] ||
                    maxy < distanceSmall.pointPrevious[0] || maxy < distanceSmall.point[0] ||
                    minx > distanceSmall.pointPrevious[1] || minx > distanceSmall.point[1] ||
                    maxx < distanceSmall.pointPrevious[1] || maxx < distanceSmall.point[1]
            ) {
                return true;
            }
        }
        if (distanceSmall.pointPrevious != null) {
            GraphPresenter.VertexBuilder vPrev = new GraphPresenter.VertexBuilder(distanceSmall.pointPrevious);
            setStyle(distanceSmall.pointPrevious, vPrev);
            presenter.add(vPrev);
        }

        GraphPresenter.VertexBuilder v = new GraphPresenter.VertexBuilder(distanceSmall.point);
        setStyle(distanceSmall.point, v);
        presenter.add(v);

        if (chars[distanceSmall.pointPrevious[1]] != null) {
            GraphPresenter.EdgeBuilder edgeBuilder = new GraphPresenter.EdgeBuilder(distanceSmall.pointPrevious, distanceSmall.point);
            setStyle(distanceSmall, edgeBuilder);
            presenter.add(edgeBuilder);
        }
//        try {
//            pic.writeAll(presenter.graph, "out.png");
//        } catch (IOException e) {
//            throw new RuntimeException("cannot save image", e);
//
//        }
        //        counter++;
//        if (counter > 1000) {
//            pass = true;
//            System.out.println("start update....");
//            initAxis();
//            presenter.update();
//            System.out.println("update done.");
//        }
        return true;
    }

    @Override
    public boolean followPathsFromBestEdge(PathCalculatorGraph.DistanceSmall distanceSmall) {
        return true;
    }

    @Override
    public void close() {
        pass = true;
        System.out.println("start update....");
        initAxis();
        presenter.update();
        System.out.println("update done.");
    }
}
