/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2012, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.graph.build.line;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.basic.BasicNode;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

/**
 * @author Anders Bakkevold
 */
public class TolerantLineStringGraphGeneratorTest extends TestCase {

    private Coordinate c1, c2, c3, c4, c5, c6, c7;

    private LineString ls;

    private GeometryFactory gf;

    private TolerantLineStringGraphGenerator gen;

    public void setUp() throws Exception {
        c1 = new Coordinate(1, 1);
        c2 = new Coordinate(2, 2);
        c3 = new Coordinate(3, 3);
        c4 = new Coordinate(4, 4);
        c5 = new Coordinate(5, 5);
        c6 = new Coordinate(2.01, 2.0007); // within tolerance (0.02) of c2
        c7 = new Coordinate(2.0, 1.75); // outsite tolerance (0.02) of c2

        Coordinate[] coordinates = new Coordinate[] { c2, c3, c4 };
        gf = new GeometryFactory();
        ls = gf.createLineString(coordinates);
        gen = new TolerantLineStringGraphGenerator();
    }

    public void testThatAddingCoordinateToStartOfLineStringWorks() {
        LineString result = gen.extendLineString(ls, c1, true);
        assertEquals(c1, result.getCoordinateN(0));
        assertEquals(4, result.getNumPoints());

        result = gen.extendLineString(result, c1, true);
        assertEquals(5, result.getNumPoints());
    }

    public void testThatAddingCoordinateToEndOfLineStringWorks() {
        LineString lineString = gen.extendLineString(ls, c5, false);
        assertEquals(c5, lineString.getCoordinateN(lineString.getNumPoints() - 1));
        assertEquals(4, lineString.getNumPoints());
    }

    public void testThatCoordinatesNearbySnapToSameNode() {
        LineString lineString = gf.createLineString(new Coordinate[] { c1, c2 });
        LineString lineString2 = gf.createLineString(new Coordinate[] { c6, c3 });
        LineString lineString3 = gf.createLineString(new Coordinate[] { c7, c4 });
        gen.setTolerance(0.02);
        gen.add(lineString);
        gen.add(lineString2);
        gen.add(lineString3);

        Graph graph = gen.getGraph();
        Collection graphNodes = graph.getNodes();
        assertEquals(5, graphNodes.size());
        Collection<Coordinate> graphNodeCoordinates = getCoordinates(graphNodes);
        assertTrue(graphNodeCoordinates.contains(c2));
        assertFalse(graphNodeCoordinates.contains(c6)); // should be snapped to c2
        assertTrue(graphNodeCoordinates.contains(c7)); // should not have been snapped to c2 - distance bigger than tolerance
        assertEquals(3, graph.getEdges().size());
    }

    private Collection<Coordinate> getCoordinates(Collection<BasicNode> graphNodes) {
        Collection<Coordinate> coordinates = new ArrayList<Coordinate>();
        for (BasicNode node : graphNodes) {
            coordinates.add((Coordinate) node.getObject());
        }
        return coordinates;
    }

}
