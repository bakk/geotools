package org.geotools.graph.build.line;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.index.bintree.Bintree;
import com.vividsolutions.jts.index.bintree.Interval;
import org.geotools.graph.build.GraphBuilder;
import org.geotools.graph.build.basic.BasicGraphBuilder;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;

import java.util.*;

public class TolerantLineStringGraphGenerator implements LineGraphGenerator {

    private double tolerance = 0.01;
    private GraphBuilder builder;
    private static GeometryFactory gf = new GeometryFactory();

    private Bintree spatialIndex;
    private Map<Coordinate, Node> coordToNode;

    public TolerantLineStringGraphGenerator() {
        spatialIndex = new Bintree(); //Uses less memory than a 2D index
        coordToNode = new HashMap<Coordinate,Node>();
        setGraphBuilder(new BasicGraphBuilder());
    }

    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    @Override
    public Graphable add(Object obj) {
        LineString ls = (LineString) obj;
        Coordinate c1 = ls.getCoordinateN(0);
        Node node = retrieveNode(c1);

        Coordinate c2 = ls.getCoordinateN(ls.getNumPoints()-1);
        Node node1 = retrieveNode(c2);

        Edge edge = getGraphBuilder().buildEdge(node,node1);
        getGraphBuilder().addEdge(edge);
        Coordinate firstCoordinate = ((Coordinate)node.getObject());
        Coordinate lastCoordinate = ((Coordinate)node1.getObject());
        if (!firstCoordinate.equals2D(c1)) {
            ls = extendLineString(ls, firstCoordinate, true);
        }
        if (!lastCoordinate.equals2D(c2)){
            ls = extendLineString(ls, lastCoordinate, false);
        }
        edge.setObject(ls);
        return edge;
    }

    protected LineString extendLineString(LineString ls, Coordinate c, boolean isFirst) {
        Coordinate[] coordinates = ls.getCoordinates();
        List<Coordinate> coordinateList = Arrays.asList(coordinates);
        //list from asList does not support add(index,object), must make an arraylist
        List<Coordinate> nCoordinateList = new ArrayList<Coordinate>(coordinateList);
        if(isFirst) {
            nCoordinateList.add(0, c);
        } else {
            nCoordinateList.add(c);
        }
        Coordinate[] newCoordinates = nCoordinateList.toArray(new Coordinate[nCoordinateList.size()]);
        return gf.createLineString(newCoordinates);
    }

    @Override
    public Graphable get(Object o) {
        LineString ls = (LineString) o;
        Coordinate firstCoordinate = ls.getCoordinateN(0);
        Coordinate lastCoordinate = ls.getCoordinateN(ls.getNumPoints() - 1);
        Node node1 = getNode(firstCoordinate);
        Node node2 = getNode(lastCoordinate);
        if (node1 == null || node2 == null) {
            return null;
        }
        return node1.getEdge(node2);
    }

    public Node getNode(Coordinate firstCoordinate) {
        return coordToNode.get(firstCoordinate);
    }

    @Override
    public Graphable remove(Object o) {
        LineString ls = (LineString) o;
        Node node1 = getNode(ls.getCoordinateN(0));
        Node node2 = getNode(ls.getCoordinateN(ls.getNumPoints()-1));
        if (node1 == null || node2 == null) {
            return null;
        }
        Edge edge = node1.getEdge(node2);
        getGraphBuilder().removeEdge(edge);
        return edge;
    }

    @Override
    public void setGraphBuilder(GraphBuilder graphBuilder) {
        builder = graphBuilder;
    }

    @Override
    public GraphBuilder getGraphBuilder() {
        return builder;
    }

    @Override
    public Graph getGraph() {
        return getGraphBuilder().getGraph();
    }

    private Node retrieveNode(Coordinate c) {
        Node node = getNode(c);
        if (node == null) {
            //spatial search with tolerance
            node = findClosestNodeWithinTolerance(c);
        }
        if(node == null) {
            node = getGraphBuilder().buildNode();
            setObject(node, c);
            getGraphBuilder().addNode(node);
            coordToNode.put(c,node);
            spatialIndex.insert(new Interval(c.y,c.y), c);
        }
        return node;
    }

    private Node findClosestNodeWithinTolerance(Coordinate inCoord) {
        double closestDistance = Double.MAX_VALUE;
        Coordinate closestCoordinate = null;
        List<Coordinate> list = spatialIndex.query(new Interval(inCoord.y - tolerance, inCoord.y + tolerance));
        for(Coordinate c : list) {
            double distance = inCoord.distance(c);
            if(distance < closestDistance) {
                closestDistance = distance;
                closestCoordinate = c;
            }
        }
        if (closestCoordinate != null && closestCoordinate.distance(inCoord) < tolerance) {
            return coordToNode.get(closestCoordinate);
        }
        return null;
    }

    protected void setObject(Node n, Coordinate c) {
        n.setObject(c);
    }

    @Override
    public Edge getEdge(Coordinate coordinate, Coordinate coordinate1) {
        Node node1 = getNode(coordinate);
        Node node2 = getNode(coordinate1);
        return node1.getEdge(node2);
    }
}

