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
package org.geotools.graph.build.feature;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.graph.build.GraphBuilder;
import org.geotools.graph.build.GraphGenerator;
import org.geotools.graph.build.basic.BasicGraphGenerator;
import org.geotools.graph.build.line.TolerantLineStringGraphGenerator;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Graphable;
import org.opengis.feature.simple.SimpleFeature;

/**
 * Generates a graph where coordinates near each other are snapped to the same node.
 *
 * This can be used in combination with TolerantLineStringGraphGenerator.
 * Since TolerantLineStringGraphGenerator may change the linestring, this class
 * preserves that change, by altering the Feature´s geometry.
 *
 * @author Anders Bakkevold
 */
public class TolerantFeatureGraphGenerator extends BasicGraphGenerator {

    /**
     * The underling "geometry" building graph generator
     */
    private GraphGenerator decorated;

    public TolerantFeatureGraphGenerator(TolerantLineStringGraphGenerator decorated) {
        this.decorated = decorated;
    }

    public Graph getGraph() {
        return decorated.getGraph();
    }

    public GraphBuilder getGraphBuilder() {
        return decorated.getGraphBuilder();
    }

    public Graphable add(Object obj) {
        SimpleFeature feature = (SimpleFeature) obj;
        Graphable g = decorated.add(feature.getDefaultGeometry());
        // the graphgenerator may have altered the geom when snapping.
        Geometry geom = (Geometry) g.getObject();
        feature.setDefaultGeometry(geom);
        return g;
    }

    public Graphable remove(Object obj) {
        SimpleFeature feature = (SimpleFeature) obj;
        return decorated.remove(feature.getDefaultGeometry());
    }

    public Graphable get(Object obj) {
        SimpleFeature feature = (SimpleFeature) obj;
        return decorated.get(feature.getDefaultGeometry());
    }
}
