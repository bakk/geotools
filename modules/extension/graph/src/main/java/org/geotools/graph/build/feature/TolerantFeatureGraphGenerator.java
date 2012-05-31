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
 * User: b543674
 * Date: 5/31/12
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

    public Graphable add( Object obj ) {
        SimpleFeature feature = (SimpleFeature) obj;
        Graphable g = decorated.add( feature.getDefaultGeometry() );
        //the graphgenerator may have altered the geom when snapping.
        Geometry geom = (Geometry)g.getObject();
        feature.setDefaultGeometry(geom);
        return g;
    }

    public Graphable remove( Object obj ) {
        SimpleFeature feature = (SimpleFeature) obj;
        return decorated.remove( feature.getDefaultGeometry() );
    }

    public Graphable get(Object obj) {
        SimpleFeature feature = (SimpleFeature) obj;
        return decorated.get( feature.getDefaultGeometry() );
    }
}
