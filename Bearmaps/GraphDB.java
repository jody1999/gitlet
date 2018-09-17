
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addlong and addEdge).
 *
 * @author Kevin Lowe, Antares Chen, Kevin Lin
 */
public class GraphDB {
    /**
     * This constructor creates and starts an XML parser, cleans the nodes, and prepares the
     * data structures for processing. Modify this constructor to initialize your data structures.
     * @param dbPath Path to the XML file to be parsed.
     */

    /**
     * David
     */

    KdTree myKdTree;

    public class Edge implements Comparable<Edge> {
        private long src;
        private long dest;
        private int weight;

        private Edge(long src, long dest, int weight) {
            this.src = src;
            this.dest = dest;
            this.weight = weight;
        }

        public long getSource() {
            return src;
        }

        public long getDest() {
            return dest;
        }

        public int getWeight() {
            return weight;
        }

        public int compareTo(Edge other) {
            int cmp = weight - other.weight;
            return cmp == 0 ? 1 : cmp;
        }

//        public boolean equals(Object o) {
//            if (this == o) {
//                return true;
//            } else if (o == null || getClass() != o.getClass()) {
//                return false;
//            }
//            Edge e = (Edge) o;
//            return (src == e.src && dest == e.dest && weight == e.weight)
//                    || (src == e.dest && dest == e.src && weight == e.weight);
//        }
    }

    //public HashMap<Long, Double> verX= new HashMap<>();
    //public HashMap<Long, Double> verY= new HashMap<>();
    HashMap<Long, Double> verLon = new HashMap<>();
    HashMap<Long, Double> verLat = new HashMap<>();
    private HashMap<Long, Set<Long>> neighbors = new HashMap<>();
    private HashMap<Long, Set<Edge>> edges = new HashMap<>();
    private TreeSet<Edge> allEdges = new TreeSet<>();

    public TreeSet<Long> getNeighbors(long v) {
        return new TreeSet<Long>(neighbors.get(v));
    }

    public TreeSet<Edge> getEdges(long v) {
        return new TreeSet<Edge>(edges.get(v));
    }

    public TreeSet<Long> getAllVertices() {
        return new TreeSet<Long>(neighbors.keySet());
    }

    public ArrayList<Long> getAllVerticesList() {
        ArrayList<Long> getAllVerticesList = new ArrayList<>();
        getAllVerticesList.addAll(getAllVertices());
        return getAllVerticesList;
    }

    public TreeSet<Edge> getAllEdges() {
        return new TreeSet<Edge>(allEdges);
    }

    public void addVertex(long v, double lon, double lat) {
        if (neighbors.get(v) == null) {
            neighbors.put(v, new HashSet<Long>());
            edges.put(v, new HashSet<Edge>());
        }
        verLon.put(v, lon);
        verLat.put(v, lat);
        //verX.put(v, projectToX(lon, lat));
        //verY.put(v, projectToY(lon, lat));
    }

    public void addEdge(Edge e) {
        addEdgeHelper(e.getSource(), e.getDest(), e.getWeight());
    }

    public void addEdge(long v1, long v2) {
        addEdgeHelper(v1, v2, 0);
    }

    public void addEdge(long v1, long v2, int weight) {
        addEdgeHelper(v1, v2, weight);
    }

    public boolean isNeighbor(long v1, long v2) {
        return neighbors.get(v1).contains(v2) && neighbors.get(v2).contains(v1);
    }

    public boolean containsVertex(long v) {
        return neighbors.get(v) != null;
    }

    public boolean containsEdge(Edge e) {
        return allEdges.contains(e);
    }

//    public boolean equals(Object o) {
//        if (!(o instanceof GraphDB)) {
//            return false;
//        }
//        GraphDB other = (GraphDB) o;
//        return neighbors.equals(other.neighbors) && edges.equals(other.edges);
//    }

    private void addEdgeHelper(long v1, long v2, int weight) {
        addVertex(v1, lon(v1), lat(v1));
        addVertex(v2, lon(v2), lat(v2));
        neighbors.get(v1).add(v2);
        neighbors.get(v2).add(v1);
        Edge e1 = new Edge(v1, v2, weight);
        Edge e2 = new Edge(v2, v1, weight);
        edges.get(v1).add(e1);
        edges.get(v2).add(e2);
        allEdges.add(e1);
    }


    public GraphDB(String dbPath) {
        File inputFile = new File(dbPath);
        try (FileInputStream inputStream = new FileInputStream(inputFile)) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(inputStream, new GraphBuildingHandler(this));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
        myKdTree = new KdTree(kdTreeSetUp(getAllVerticesList(), true));
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     *
     * @param s Input string.
     * @return Cleaned string.
     */
    private static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     * Remove nodes with no connections from the graph.
     * While this does not guarantee that any two nodes in the remaining graph are connected,
     * we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        // TODO
        HashSet<Long> toRemove = new HashSet<>();
        for (long itr : getAllVertices()) {
            if (getNeighbors(itr).isEmpty()) {
                neighbors.remove(itr);
                edges.remove(itr);
                verLon.remove(itr);
                verLat.remove(itr);
                //verX.remove(itr);
                //verY.remove(itr);
            }
        }
    }

    /**
     * Returns the longitude of vertex <code>v</code>.
     *
     * @param v The ID of a vertex in the graph.
     * @return The longitude of that vertex, or 0.0 if the vertex is not in the graph.
     */
    double lon(long v) {
        // TODO
        return verLon.get(v);
    }

    /**
     * Returns the latitude of vertex <code>v</code>.
     *
     * @param v The ID of a vertex in the graph.
     * @return The latitude of that vertex, or 0.0 if the vertex is not in the graph.
     */
    double lat(long v) {
        // TODO
        return verLat.get(v);
    }

    double x(long v) {
        return projectToX(lon(v), lat(v));
    }

    double y(long v) {
        return projectToY(lon(v), lat(v));
    }

    /**
     * Returns an iterable of all vertex IDs in the graph.
     *
     * @return An iterable of all vertex IDs in the graph.
     */
    Iterable<Long> vertices() {
        // TODO
        //return Collections.emptySet();
        return getAllVertices();
    }

    /**
     * Returns an iterable over the IDs of all vertices adjacent to <code>v</code>.
     *
     * @param v The ID for any vertex in the graph.
     * @return An iterable over the IDs of all vertices adjacent to <code>v</code>, or an empty
     * iterable if the vertex is not in the graph.
     */
    Iterable<Long> adjacent(long v) {
        // TODO
        return getNeighbors(v);
    }

    static double euclidean(double x1, double x2, double y1, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    /**
     * Returns the great-circle distance between two vertices, v and w, in miles.
     * Assumes the lon/lat methods are implemented properly.
     *
     * @param v The ID for the first vertex.
     * @param w The ID for the second vertex.
     * @return The great-circle distance between vertices and w.
     * @source https://www.movable-type.co.uk/scripts/latlong.html
     */
    public double distance(long v, long w) {
        double phi1 = Math.toRadians(lat(v));
        double phi2 = Math.toRadians(lat(w));
        double dphi = Math.toRadians(lat(w) - lat(v));
        double dlambda = Math.toRadians(lon(w) - lon(v));
        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public double distance(long v, double lon, double lat) {
        double phi1 = Math.toRadians(lat(v));
        double phi2 = Math.toRadians(lat);
        double dphi = Math.toRadians(lat - lat(v));
        double dlambda = Math.toRadians(lon - lon(v));
        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }


    /**
     * Returns the ID of the vertex closest to the given longitude and latitude.
     *
     * @param lon The given longitude.
     * @param lat The given latitude.
     * @return The ID for the vertex closest to the <code>lon</code> and <code>lat</code>.
     */
    public long closest(double lon, double lat) {
        // TODO
        double srcX = projectToX(lon, lat);
        double srcY = projectToY(lon, lat);
        verDisC = new HashMap<>();
        long minID = getMinID(myKdTree.root, srcX, srcY);
        return minID;
    }

    public double min(double o1, double o2) {
        if (o1 < o2) {
            return o1;
        }
        return o2;
    }

    HashMap<Long, Double> verDisC = new HashMap<>();

    long tempDisID;
    double tempDis;
    double lineDis;
    public long getMinID(KdNode node, double x, double y) {
        if (node == null) {
            verDisC.put(Long.MAX_VALUE, Double.MAX_VALUE);
            return Long.MAX_VALUE;
        }
        double minDis = euclidean(x, node.x, y, node.y);
        long minDisID = node.id;
        verDisC.put(minDisID, minDis);
        //Real Code
        if (node.isLR) {
            if (x < node.x) {
                tempDisID = getMinID(node.left, x, y);
                tempDis = verDisC.get(tempDisID);
                if (tempDis < minDis) {
                    minDis = tempDis;
                    minDisID = tempDisID;
                }
                lineDis = euclidean(node.x, x, y, y);
                if (lineDis <= minDis) {
                    tempDisID = getMinID(node.right, x, y);
                    tempDis = verDisC.get(tempDisID);
                    if (tempDis < minDis) {
                        minDis = tempDis;
                        minDisID = tempDisID;
                    }
                }
            } else {
                tempDisID = getMinID(node.right, x, y);
                tempDis = verDisC.get(tempDisID);
                if (tempDis < minDis) {
                    minDis = tempDis;
                    minDisID = tempDisID;
                }
                lineDis = euclidean(node.x, x, y, y);
                if (lineDis <= minDis) {
                    tempDisID = getMinID(node.left, x, y);
                    tempDis = verDisC.get(tempDisID);
                    if (tempDis < minDis) {
                        minDis = tempDis;
                        minDisID = tempDisID;
                    }
                }
            }
        } else {
            if (y < node.y) {
                tempDisID = getMinID(node.left, x, y);
                tempDis = verDisC.get(tempDisID);
                if (tempDis < minDis) {
                    minDis = tempDis;
                    minDisID = tempDisID;
                }
                lineDis = euclidean(x, x, node.y, y);
                if (lineDis <= minDis) {
                    tempDisID = getMinID(node.right, x, y);
                    tempDis = verDisC.get(tempDisID);
                    if (tempDis < minDis) {
                        minDis = tempDis;
                        minDisID = tempDisID;
                    }
                }
            } else {
                tempDisID = getMinID(node.right, x, y);
                tempDis = verDisC.get(tempDisID);
                if (tempDis < minDis) {
                    minDis = tempDis;
                    minDisID = tempDisID;
                }
                lineDis = euclidean(x, x, node.y, y);
                if (lineDis <= minDis) {
                    tempDisID = getMinID(node.left, x, y);
                    tempDis = verDisC.get(tempDisID);
                    if (tempDis < minDis) {
                        minDis = tempDis;
                        minDisID = tempDisID;
                    }
                }
            }
        }
        return minDisID;
    }

    public class KdTree {
        KdNode root;

        public KdTree(KdNode root) {
            this.root = root;
        }
    }

    public static class KdNode {
        private long id;
        private double x;
        private double y;
        private KdNode left;
        private KdNode right;
        private boolean isLR;

        KdNode(long id, double x, double y, KdNode left, KdNode right, boolean isLR) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.left = left;
            this.right = right;
            this.isLR = isLR;
        }
    }

    public KdNode kdTreeSetUp(List<Long> verList, boolean isLR) {
        if (verList.size() == 1) {
            long v = verList.get(0);
            return new KdNode(v, x(v), y(v), null, null, isLR);
        } else if (verList.size() == 0) {
            return null;
        } else {
            if (isLR) {
                Collections.sort(verList, new Comparator<Long>() {
                    @Override
                    public int compare(Long o1, Long o2) {
                        if (x(o1) > x(o2)) {
                            return 1;
                        }
                        if (x(o1) < x(o2)) {
                            return -1;
                        }
                        return 0;
                    }
                });
            } else {
                Collections.sort(verList, new Comparator<Long>() {
                    @Override
                    public int compare(Long o1, Long o2) {
                        if (y(o1) > y(o2)) {
                            return 1;
                        }
                        if (y(o1) < y(o2)) {
                            return -1;
                        }
                        return 0;
                    }
                });
            }
            int medianIndex = (verList.size() - 1) / 2;
            long median = verList.get(medianIndex);
            List<Long> leftVerList = verList.subList(0, medianIndex);
            List<Long> rightVerList = verList.subList(medianIndex + 1, verList.size());
            KdNode left = kdTreeSetUp(leftVerList, !isLR);
            KdNode right = kdTreeSetUp(rightVerList, !isLR);
            return new KdNode(median, x(median), y(median), left, right, isLR);
        }
    }

    /**
     * Return the Euclidean x-value for some point, p, in Berkeley. Found by computing the
     * Transverse Mercator projection centered at Berkeley.
     *
     * @param lon The longitude for p.
     * @param lat The latitude for p.
     * @return The flattened, Euclidean x-value for p.
     * @source https://en.wikipedia.org/wiki/Transverse_Mercator_projection
     */
    static double projectToX(double lon, double lat) {
        double dlon = Math.toRadians(lon - ROOT_LON);
        double phi = Math.toRadians(lat);
        double b = Math.sin(dlon) * Math.cos(phi);
        return (K0 / 2) * Math.log((1 + b) / (1 - b));
    }

    /**
     * Return the Euclidean y-value for some point, p, in Berkeley. Found by computing the
     * Transverse Mercator projection centered at Berkeley.
     *
     * @param lon The longitude for p.
     * @param lat The latitude for p.
     * @return The flattened, Euclidean y-value for p.
     * @source https://en.wikipedia.org/wiki/Transverse_Mercator_projection
     */
    static double projectToY(double lon, double lat) {
        double dlon = Math.toRadians(lon - ROOT_LON);
        double phi = Math.toRadians(lat);
        double con = Math.atan(Math.tan(phi) / Math.cos(dlon));
        return K0 * (con - Math.toRadians(ROOT_LAT));
    }

    /**
     * In linear time, collect all the names of OSM locations that prefix-match the query string.
     *
     * @param prefix Prefix string to be searched for. Could be any case, with our without
     *               punctuation.
     * @return A <code>List</code> of the full names of locations whose cleaned name matches the
     * cleaned <code>prefix</code>.
     */
    public List<String> getLocationsByPrefix(String prefix) {
        return Collections.emptyList();
    }

    /**
     * Collect all locations that match a cleaned <code>locationName</code>, and return
     * information about each node that matches.
     *
     * @param locationName A full name of a location searched for.
     * @return A <code>List</code> of <code>LocationParams</code> whose cleaned name matches the
     * cleaned <code>locationName</code>
     */
    public List<LocationParams> getLocations(String locationName) {
        return Collections.emptyList();
    }

    /**
     * Returns the initial bearing between vertices <code>v</code> and <code>w</code> in degrees.
     * The initial bearing is the angle that, if followed in a straight line along a great-circle
     * arc from the starting point, would take you to the end point.
     * Assumes the lon/lat methods are implemented properly.
     *
     * @param v The ID for the first vertex.
     * @param w The ID for the second vertex.
     * @return The bearing between <code>v</code> and <code>w</code> in degrees.
     * @source https://www.movable-type.co.uk/scripts/latlong.html
     */
    double bearing(long v, long w) {
        double phi1 = Math.toRadians(lat(v));
        double phi2 = Math.toRadians(lat(w));
        double lambda1 = Math.toRadians(lon(v));
        double lambda2 = Math.toRadians(lon(w));

        double y = Math.sin(lambda2 - lambda1) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2);
        x -= Math.sin(phi1) * Math.cos(phi2) * Math.cos(lambda2 - lambda1);
        return Math.toDegrees(Math.atan2(y, x));
    }

    /**
     * Radius of the Earth in miles.
     */
    private static final int R = 3963;
    /**
     * Latitude centered on Berkeley.
     */
    private static final double ROOT_LAT = (MapServer.ROOT_ULLAT + MapServer.ROOT_LRLAT) / 2;
    /**
     * Longitude centered on Berkeley.
     */
    private static final double ROOT_LON = (MapServer.ROOT_ULLON + MapServer.ROOT_LRLON) / 2;
    /**
     * Scale factor at the natural origin, Berkeley. Prefer to use 1 instead of 0.9996 as in UTM.
     *
     * @source https://gis.stackexchange.com/a/7298
     */
    private static final double K0 = 1.0;
}


