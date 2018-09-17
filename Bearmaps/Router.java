import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides a <code>shortestPath</code> method and <code>routeDirections</code> for
 * finding routes between two points on the map.
 */
public class Router {
    /**
     * Return a <code>List</code> of vertex IDs corresponding to the shortest path from a given
     * starting coordinate and destination coordinate.
     *
     * @param g       <code>GraphDB</code> data source.
     * @param stlon   The longitude of the starting coordinate.
     * @param stlat   The latitude of the starting coordinate.
     * @param destlon The longitude of the destination coordinate.
     * @param destlat The latitude of the destination coordinate.
     * @return The <code>List</code> of vertex IDs corresponding to the shortest path.
     */

    public static List<Long> shortestPath(GraphDB g,
                                          double stlon, double stlat,
                                          double destlon, double destlat) {

        List<Long> result = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        Map<Long, Double> verDis = new HashMap<>();
        Map<Long, Long> verPre = new HashMap<>();
        PriorityQueue<Long> fringe = new PriorityQueue<>(g.getAllVertices().size(),
                new Comparator<Long>() {
                @Override
                public int compare(Long o1, Long o2) {
                    double o1Pri = verDis.get(o1) + heuristic(g, o1, destlon, destlat);
                    double o2Pri = verDis.get(o2) + heuristic(g, o2, destlon, destlat);
                    if (o1Pri > o2Pri) {
                        return 1;
                    }
                    if (o1Pri < o2Pri) {
                        return -1;
                    }
                    return 0;
                }
            });

        long startID = g.closest(stlon, stlat);
        long endID = g.closest(destlon, destlat);
        for (long v : g.vertices()) {
            if (v == startID) {
                verDis.put(v, 0.0);
                verPre.put(v, null);
            } else {
                verDis.put(v, Double.MAX_VALUE);
                verPre.put(v, null);
            }
        }

        for (long neighbor : g.adjacent(startID)) {
            verDis.put(neighbor, g.distance(startID, neighbor));
            verPre.put(neighbor, startID);
        }
        fringe.add(startID);
        while (!fringe.isEmpty()) {
            long v = fringe.poll();
            if (!visited.contains(v)) {
                visited.add(v);
                if (v == endID) {
                    long next = endID;
                    while (next != startID) {
                        result.add(next);
                        next = verPre.get(next);
                    }
                    result.add(startID);
                    Collections.reverse(result);
                    return result;
                }
                for (long w : g.adjacent(v)) {
                    if (visited.contains(w)) {
                        continue;
                    }
                    if (!fringe.contains(w)) {
                        verDis.put(w, verDis.get(v) + g.distance(w, v));
                        verPre.put(w, v);
                        fringe.add(w);
                    } else {
                        double curDis = verDis.get(w);
                        double newDis = verDis.get(v) + g.distance(w, v);
                        if (newDis < curDis) {
                            verDis.put(w, newDis);
                            verPre.put(w, v);
                            fringe.add(w);
                        }

                    }
                }
            }
        }
        return Collections.emptyList();
    }

    public static double heuristic(GraphDB g, long v, double destlon, double destlat) {
        return g.distance(v, destlon, destlat);
    }


    /**
     * Given a <code>route</code> of vertex IDs, return a <code>List</code> of
     * <code>NavigationDirection</code> objects representing the travel directions in order.
     *
     * @param g     <code>GraphDB</code> data source.
     * @param route The shortest-path route of vertex IDs.
     * @return A new <code>List</code> of <code>NavigationDirection</code> objects.
     */
    public static List<NavigationDirection> routeDirections(GraphDB g, List<Long> route) {
        return Collections.emptyList();
    }

    /**
     * Class to represent a navigation direction, which consists of 3 attributes:
     * a direction to go, a way, and the distance to travel for.
     */
    public static class NavigationDirection {

        /**
         * Integer constants representing directions.
         */
        public static final int START = 0, STRAIGHT = 1, SLIGHT_LEFT = 2, SLIGHT_RIGHT = 3,
                RIGHT = 4, LEFT = 5, SHARP_LEFT = 6, SHARP_RIGHT = 7;

        /**
         * Number of directions supported.
         */
        public static final int NUM_DIRECTIONS = 8;

        /**
         * A mapping of integer values to directions.
         */
        public static final String[] DIRECTIONS = new String[NUM_DIRECTIONS];

        static {
            DIRECTIONS[START] = "Start";
            DIRECTIONS[STRAIGHT] = "Go straight";
            DIRECTIONS[SLIGHT_LEFT] = "Slight left";
            DIRECTIONS[SLIGHT_RIGHT] = "Slight right";
            DIRECTIONS[RIGHT] = "Turn right";
            DIRECTIONS[LEFT] = "Turn left";
            DIRECTIONS[SHARP_LEFT] = "Sharp left";
            DIRECTIONS[SHARP_RIGHT] = "Sharp right";
        }

        /**
         * The direction represented.
         */
        int direction;
        /**
         * The name of this way.
         */
        String way;
        /**
         * The distance along this way.
         */
        double distance = 0.0;

        public String toString() {
            return String.format("%s on %s and continue for %.3f miles.",
                    DIRECTIONS[direction], way, distance);
        }

        /**
         * Returns a new <code>NavigationDirection</code> from a string representation.
         *
         * @param dirAsString <code>String</code> instructions for a navigation direction.
         * @return A new <code>NavigationDirection</code> based on the string, or <code>null</code>
         * if unable to parse.
         */
        public static NavigationDirection fromString(String dirAsString) {
            String regex = "([a-zA-Z\\s]+) on ([\\w\\s]*) and continue for ([0-9\\.]+) miles\\.";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(dirAsString);
            NavigationDirection nd = new NavigationDirection();
            if (m.matches()) {
                String direction = m.group(1);
                if (direction.equals("Start")) {
                    nd.direction = NavigationDirection.START;
                } else if (direction.equals("Go straight")) {
                    nd.direction = NavigationDirection.STRAIGHT;
                } else if (direction.equals("Slight left")) {
                    nd.direction = NavigationDirection.SLIGHT_LEFT;
                } else if (direction.equals("Slight right")) {
                    nd.direction = NavigationDirection.SLIGHT_RIGHT;
                } else if (direction.equals("Turn right")) {
                    nd.direction = NavigationDirection.RIGHT;
                } else if (direction.equals("Turn left")) {
                    nd.direction = NavigationDirection.LEFT;
                } else if (direction.equals("Sharp left")) {
                    nd.direction = NavigationDirection.SHARP_LEFT;
                } else if (direction.equals("Sharp right")) {
                    nd.direction = NavigationDirection.SHARP_RIGHT;
                } else {
                    return null;
                }

                nd.way = m.group(2);
                try {
                    nd.distance = Double.parseDouble(m.group(3));
                } catch (NumberFormatException e) {
                    return null;
                }
                return nd;
            } else {
                // Not a valid nd
                return null;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof NavigationDirection) {
                return direction == ((NavigationDirection) o).direction
                        && way.equals(((NavigationDirection) o).way)
                        && distance == ((NavigationDirection) o).distance;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(direction, way, distance);
        }
    }
}
