import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * Generates a random field of <b>convex</b> polygons.
 *
 * Coordinates come in the form of <code>int[]{x, y}</code>.
 * Each vertex of a shape has an array of x and y. Each shape has
 * an array of vertices (<code>int[][]</code>). The whole field
 * which is returned by <code>generator.getCoordinates()</code>
 * is an array of shapes (<code>int[][][]</code>).
 *
 * I recommend using this triple array to create objects (object
 * shape, object vertex, object edge, etc), but the array is
 * usable in its raw form.
 * <br>
 * How to use:
 * <ol>
 * <li><code>Polygen generator = new Polygen();</code></li>
 * <li><code>generator.render();</code></li>
 * <li><code>int[][][] shapeCoordinates = generator.getCoordinates();</code>
 * </li>
 * <li><code>int[] startCoor = generator.getStart();</code></li>
 * <li><code>int[] goalCoor = generator.getGoal();</code></li>
 * </ol>
 * <br>
 * Authored on vim
 * (vim &gt; IDE).
 *
 * @author Michael C. Davis
 */
public class Polygen {

    private int width, height, maxr, minr;
    private int[][][] coordinates = new int[0][][];
    private int[] start = new int[0], goal = new int[0];

    /**
     * Creates a generator with default 600, 350 dimensions.
     */
    public Polygen () {
        this(600, 350);
    }

    /**
     * Creates a generator with width x width
     * dimensions.
     * @param   width   width and height of the 2D plane
     */
    public Polygen (int width) {
        this(width, width);
    }

    /**
     * Creates a generator with width x height dimensions.
     * @param   width   width of the 2D plane
     * @param   height  height of the 2D plane
     */
    public Polygen (int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Renders a new 2D plane filled with polygons up to a density
     * of 38% polygon. This is a healthy default and should probably
     * not be messed with.
     * @return true when the render is complete.
     */
    public boolean render () {
        return render(0.5);
    }

    /**
     * Renders a new 2D plane filled with polygons up to a specified
     * density.
     * @param targetDensity the goal density to take as minimum.
     * @return  true when complete.
     * start, and goal properties.
     */
    public boolean render (double targetDensity) {

        List<Poly> shapes = new ArrayList<Poly>(51);

        maxr = width / 5;
        minr = width / 45;

        do {
            //  start over if the building is too slow
            if (shapes.size() > 35)
                shapes.clear();
            Poly p;
            //  create a new point until it's free from other circles
            do {
                p = new Poly(ri(5, width - 5), 
                        ri(5, height - 5), 
                        ri(minr, maxr));
                populateVertices(p);
            } while (isStrongContained(shapes, p));
            shapes.add(p);
        } while (density(shapes) < targetDensity);

        System.out.println("Shapes generated: " + shapes.size());

        //  perform a tight fit of all polygons, expanding the radii
        for (Poly s : shapes) {
            boolean grown = false;
            s.grow(2 * s.radius);
            for (double setr = s.radius; isStrongContained(shapes, s) ||
                    !strongIsOnMap(s); setr -= 2) {
                s.grow(setr);
                grown = true;
            }
            //  shrink one more
            if (grown) s.grow(s.radius - 2);
        }

        //  prep the data for export
        Converter c = new Converter(shapes);
        //  export to fields
        coordinates = c.getCoordinates();
        start = c.getStartCoordinates();
        goal = c.getGoalCoordinates();
        //  exit
        return true;
    }

    /**
     * Gets the rendered coordinate field. If render has
     * not been called yet, it will call render first.
     * @return the field of polygon coordinates
     */
    public int[][][] getCoordinates() {
        if (coordinates.length == 0)
            render();
        return coordinates;
    }

    /**
     * Gets the start coordinates. If render has not been called
     * yet, this will call it.
     * @return an [x,y] coordinate pair of the start
     */
    public int[] getStart() {
        if (start.length == 0)
            render();
        return start;
    }

    /**
     * Gets the goal coordinates. If render has not been called
     * yet, this will call it.
     * @return an [x,y] coordinate pair of the goal
     */
    public int[] getGoal() {
        if (start.length == 0)
            render();
        return goal;
    }

    /** Generates a random integer between [min, max] inclusive.
     * @param   min minimum bound
     * @param   max maximum bound, which can be produced
     * @return a random integer between [min, max] inclusive.
     */
    public static int randomInt (int min, int max) {
        min = min;
        max = max;
        return (int) (Math.random() * (max - min + 1) + min);
    }

    /**
     * Checks if two doubles are approximately equal.
     * This is done with the standard |a - b| &lt; epsillon.
     * @param a the first double
     * @param b the second dobule
     * @return true if they're approximately equal
     */
    public static boolean doubleEquals(double a, double b) {
        return Math.abs(a - b) < 0.01;
    }

    /**
     * Checks if two line segments intersect, including the endpoints.
     * Use this instead of <code>Line2D.linesIntersect</code>.
     * @param x1 the first x coordinate of the first line
     * @param y1 the first y coordinate of the first line
     * @param x2 the second x coordinate of the first line
     * @param y2 the second y coordinate of the first line
     * @param x3 the first x coordinate of the second line
     * @param y3 the first y coordinate of the second line
     * @param x4 the second x coordinate of the second line
     * @param y4 the second y coordinate of the second line
     * @return true iff the line segments intersect
     */
    public static boolean segmentsIntersect(double x1,
            double y1, double x2, double y2, double x3, double y3,
            double x4, double y4) {

        double a1, b1, a2, b2, p;

        //  step one: check vertical lines
        if (isVertical(x1, x2) && isVertical(x3, x4)) {

            if (!isVertical(x1, x3))
                return false;
            //  check if their heights overlap
            return !(Math.max(y1, y2) < Math.min(y3, y4) ||
                    Math.max(y3, y4) < Math.min(y1, y2));
        } else if (isVertical(x1, x2)) {

            a2 = (y4 - y3) / (x4 - x3);
            b2 = y3 - a2 * x3;
            p = a2 * x1 + b2;
            return isOnLine(p, y1, y2) && isOnLine(p, y3, y4);
        } else if (isVertical(x3, x4)) {

            a1 = (y2 - y1) / (x2 - x1);
            b1 = y1 - a1 * x1;
            p = a1 * x3 + b1;
            return isOnLine(p, y1, y2) && isOnLine(p, y3, y4);
        }

        //  step two: build equations.
        a1 = (y2 - y1) / (x2 - x1);
        b1 = y1 - a1 * x1;
        a2 = (y4 - y3) / (x4 - x3);
        b2 = y3 - a2 * x3;

        //  step three: check if lines are parallel
        if (doubleEquals(a1, a2)) {
            if (doubleEquals(b1, b2))
                return !(Math.max(y1, y2) < Math.min(y3, y4) ||
                    Math.max(y3, y4) < Math.min(y1, y2));
            return false;
        }

        //  step four: find the intersection
        p = - (b1 - b2) / (a1 - a2);

        //  step five: return if that intersection is on the lines
        return isOnLine(p, x1, x2) && isOnLine(p, x3, x4);
    }

    /**
     * Checks if a point is on a line. This must be a linear path (non-
     * quadratic, cubic, etc.).
     * @param p0 the point
     * @param x1 the left segment endpoint
     * @param x2 the right segment endpoint
     */
    private static boolean isOnLine (double p0, double x1, double x2) {
        return Math.min(x1, x2) <= p0 && p0 <= Math.max(x1, x2);
    }

    /**
     * Give the polygon a set of valid vertices.
     * @param c the polygon to populate
     */
    private void populateVertices (Poly c) {
        //  for each side (or vertex, it's the same)
        for (int i = 0; i < c.sides; i++) {
            //  provide a way to break out of solutions
            int whilecount = 0;
            do {
                double angle = Math.random() * Math.PI * 2,
                       x = Math.cos(angle) * c.radius,
                       y = Math.sin(angle) * c.radius;
                c.vertices[i] = new Vertex(x + c.x, y + c.y, c.radius, angle);
                //  break out if the min distance is unsatisfiable
                if (whilecount++ > 100 &&
                        !pointIsntOnMap(c.vertices[i].x, c.vertices[i].y))
                    break;
                //  repeat while the point isn't on the map or
                //  it's too close to another vertex
            } while (pointIsntOnMap(c.vertices[i].x, c.vertices[i].y) ||
                    isContained(c.vertices, i));
        }
        //  apply angle property
        Double startang = null;
        for (int i = 0; i < c.vertices.length; i++) {
            Vertex e = c.vertices[i];
            double angle = Math.atan2(e.y - c.y, e.x - c.x);
            if (startang == null)
                startang = angle;
            else if (angle < startang)
                angle += Math.PI * 2;
            e.angle = angle;
        }

        //  sort the data
        Arrays.sort(c.vertices);
    }

    /** Checks if a vertex is too close to another vertex
     * in a vertex array. This is inexpensive for small
     * arrays, but is Theta(n)
     * @param   arr the vertex array
     * @param   index   the index of the node to test
     */
    private boolean isContained (Vertex[] arr, int index) {
        for (int i = 0; i < index; i++)
            if (arr[i].overlaps(arr[index]))
                return true;
        return false;
    }

    /** Checks if a circle is overlapped by any other circle
     * in a body array. This is inexpensive for small arrays,
     * but is Theta(n)
     * @param   arr the body array
     * @param   index   the index of the node to test
     */
    private boolean isContained (List<Poly> list, Poly p) {
        for (Poly q : list)
            if (!p.equals(q) && q.overlaps(p))
                return true;
        return false;
    }

    /**
     * Checks if the polygon is overlapped by the other polygon.
     * @param list the list of polygons
     * @param p the current polygon
     * @return true if the polygon is overlapped
     */
    private boolean isStrongContained (List<Poly> list, Poly p) {
        for (Poly q : list)
            if (!p.equals(q) && q.strongOverlap(p))
                return true;
        return false;
    }

    /** Checks if a point is on the 2D plane.
     * @param   x   x coordinate
     * @param   y   y coordinate
     */
    private boolean pointIsntOnMap(double x, double y) {
        return x < 5D || x > (double) (width - 5) || 
            y < 5D || y > (double) (height - 5);
    }

    /** Check if a poly is fully on the map.
     * @return true if the polygon doesn't have a point off the map.
     */
    private boolean strongIsOnMap(Poly p) {
        double[][] reduction = p.reduction();
        //  when it is on the map
        if ((reduction[0][0] > 5 && reduction[0][1] < width - 5) &&
            (reduction[1][0] > 5 && reduction[1][1] < height - 5))
            return true;
        return false;
    }

    /**
     * Find the density of the map from the current list of polygons.
     * @param  list the current list of polygons.
     * @return the density of the map.
     */
    private double density (List<Poly> list) {
        //  the area of the map, sub borders
        double mapArea = (width - 10) * (height - 10);
        double polyArea = 0d;
        for (Poly p : list)
            polyArea += p.area();
        return polyArea / mapArea;
    }

    /** Generates a random integer between [min, max] inclusive.
     * @param   min minimum bound
     * @param   max maximum bound, which can be produced
     */
    private int ri (int min, int max) {
        min = (int) min;
        max = (int) max;
        return (int) (Math.random() * (max - min + 1) + min);
    }

    /**
     * Checks if a line is vertical.
     * @param x1 the first x coordinate of the segment.
     * @param x2 the second x coordinate of the segment.
     * @return ture if the line is vertical.
     */
    private static boolean isVertical (double x1, double x2) {
        return Math.abs(x2 - x1) < 2d;
    }

    /** Point on a circle. */
    private class Vertex implements Comparable<Vertex> {

        public double x, y, radius;
        public double angle, setangle;

        /**
         * Creates a new Vertex.
         * @param   x   x coordinate
         * @param   y   y coordinate
         * @param   r   minimum distance to other vertices
         */
        public Vertex (double x, double y, double r) {
            this.x = x;
            this.y = y;
            this.radius = r;
        }

        public Vertex (double x, double y, double r, double angle) {
            this(x, y, r);
            this.setangle = angle;
        }

        /**
         * Checks if one Vertex is too close to the other
         * @param   o   the other vertex instance
         * @return  true if they are too close
         */
        public boolean overlaps (Vertex o) {
            return Math.hypot(o.x - this.x, o.y - this.y)
                <= (this.radius - 1D);
        }

        /**
         * Compares this Vertex to another by angle.
         * @param   o   vertex to compare to
         * @return  0 if equal, negative if this vertex
         * comes before the other, positive else
         */
        public int compareTo (Vertex o) {
            return (int) (100*this.angle - 100*o.angle);
        }

    }

    /** Circle. */
    private class Poly {

        public int sides;
        public Vertex[] vertices;
        public double x, y, radius;

        /**
         * Creates a new circle.
         * @param   x   x coordinate
         * @param   y   y coordinate
         * @param   r   radius of the circle
         */
        public Poly (double x, double y, double r) {
            this.x = x;
            this.y = y;
            this.radius = r;
            this.sides = (int) (Math.random() * (7 - 3)) + 3;
            this.vertices = new Vertex[this.sides];
        }

        /**
         * Set the radius to a new value. The vertices will follow suit.
         * @param   r  the new radius to set to
         */
        public void grow (double r) {
            this.radius = r;
            for (int i = 0; i < vertices.length; i++) {
                vertices[i].x = Math.cos(vertices[i].setangle) * r + x;
                vertices[i].y = Math.sin(vertices[i].setangle) * r + y;
            }
        }

        /**
         * Checks if this circle overlaps another.
         * @param   o   the other circle
         * @return true if they intersect
         */
        public boolean overlaps (Poly o) {
            return Math.hypot(o.x - this.x, o.y - this.y)
                <= (o.radius + this.radius);
        }

        /**
         * Checks equality of two Poly instances. This is a comparison of array
         * references.
         * @param   o   the other circle
         * @return  true if they have the same vertex array
         */
        public boolean equals (Poly o) {
            return this.vertices == o.vertices;
        }

        /** Check the overlap of two polygons, not circles.
         * @param   o   the other polygon
         * @return true if there is any containment or overlapping
         * i.e. intersection.
         */
        public boolean strongOverlap (Poly o) {
            //  rule out those too far apart from one another
            if (!this.overlaps(o))
                return false;

            //  if there are any intersections, they overlap
            int len = this.vertices.length;
            for (int i = 0; i < len; i++)
                for (int j = 0; j < o.vertices.length; j++)
                    if (Polygen.segmentsIntersect(
                                vertices[i].x, vertices[i].y,
                                vertices[(i + 1) % len].x,
                                vertices[(i + 1) % len].y,
                                o.vertices[j].x, o.vertices[j].y,
                                o.vertices[(j + 1) % o.vertices.length].x,
                                o.vertices[(j + 1) % o.vertices.length].y))
                        return true;

            if (this.innerCircled(o) || o.innerCircled(this))
                return true;

            return !(this.rayIntersections(o) == 0 && 
                    o.rayIntersections(this) == 0);
        }

        /**
         * Checks if Poly b is inside this Poly by counting raytraces out
         * from o.
         * @param o the alleged surrounder.
         * @return the number of intersections of the line from o's center
         * to a point in the opposite direction of this's center and all of 
         * this's edges.
         */
        private int rayIntersections (Poly o) {
            int intersections = 0;
            int len = this.vertices.length;

            double angle = Math.atan2(this.y - o.y, this.x - o.x);
            angle += Math.PI;
            double targetX = (width / 2) * Math.cos(angle) + o.x;
            double targetY = (width / 2) * Math.sin(angle) + o.y;

            for (int i = 0; i < len; i++)
                //  check intersection of...
                if (Polygen.segmentsIntersect(
                            //  the edges of a
                            vertices[i].x, vertices[i].y,
                            vertices[(i + 1) % len].x,
                            vertices[(i + 1) % len].y,
                            //  vs the line from b's center to (0,0)
                            o.x, o.y, targetX, targetY))
                    intersections++;

            return intersections;
        }

        /**
         * Checks if Poly o is within this's smallest inscribed circle.
         * To be used statically.
         * @param o the alleged surrounder.
         * @return true if o is within this's smallest inscribed circle.
         */
        private boolean innerCircled(Poly o) {
            double radius = this.innerRadius();
            //  if the center is inside, it's circled
            if (Math.hypot(o.x - (this.x + radius), 
                        o.y - (this.y + radius)) < radius)
                return true;
            //  if the other vertices are inside, it's circled
            for (int i = 0; i < o.vertices.length; i++)
                if (Math.hypot(o.vertices[i].x - (this.x + radius),
                            o.vertices[i].y - (this.y + radius)) < radius)
                    return true;
            return false;
        }

        /**
         * Compute the radius of the largest enclosed circle inside the
         * polygon.
         * @return the radius of the smallest enclosed circle inside this
         * instance of Poly.
         */
        private double innerRadius () {
            //  find the longest edge, which is closest to the center.
            double max = Double.NEGATIVE_INFINITY;
            int index = 0;
            int len = this.vertices.length;
            for (int i = 0; i < len; i++) {
                double x1 = this.vertices[i].x,
                       y1 = this.vertices[i].y,
                       x2 = this.vertices[(i + 1) % len].x,
                       y2 = this.vertices[(i + 1) % len].y;
                double hypot = Math.hypot(x1 - x2, y1 - y2);
                if (hypot > max) {
                    max = hypot;
                    index = i;
                }
            }
            //  write out the coordinates of the longest edge
            double x1 = this.vertices[index].x,
                   y1 = this.vertices[index].y,
                   x2 = this.vertices[(index + 1) % len].x,
                   y2 = this.vertices[(index + 1) % len].y;

            double radius = 0d;
            //  handle the edge case that the longest edge is vertical
            if (isVertical(x1, x2)) {
                //  the radius line will be horizontal
                radius = Math.abs(this.x - x1);
            }
            //  handle the edge case that the longest edge is flat
            else if (isVertical(y1, y2)) {
                //  the radius line will be vertical
                radius = Math.abs(this.y - y1);
            }
            //  handle the general case (both will be slanted)
            else {
                //  build the equation of the longest edge
                double m1 = (y2 - y1) / (x2 - x1),
                       b1 = y1 - m1 * x1;
                //  build the inverse equation of that
                double m2, b2;
                //  the slope is (- 1 / m)
                m2 = -1 / m1;
                //  the y intercept can be found by using the coordinates of a's
                //  center
                b2 = this.y - (m2 * this.x);
                //  compute the intersection point
                double intersectionX = - (b1 - b2) / (m1 - m2);
                double intersectionY = m1 * intersectionX + b1;
                //  compute the length
                radius = Math.hypot(this.x - intersectionX,
                        this.y - intersectionY);
            }
            return radius;
        }

        /**
         * Gets the rectangular flattening of the polygon.
         * @return the x and y coordinates of the bounding rectangle. 
         */
        public double[][] reduction () {
            double xmin = 10000D, xmax = -1D, ymin = 10000D, ymax = -1D;
            for (int i = 0; i < this.vertices.length; i++) {
                if (vertices[i].x < xmin)
                    xmin = vertices[i].x;
                else if (vertices[i].x > xmax)
                    xmax = vertices[i].x;

                if (vertices[i].y < ymin)
                    ymin = vertices[i].y;
                else if (vertices[i].y > ymax)
                    ymax = vertices[i].y;
            }
            return new double[][]{
                {xmin, xmax},
                    {ymin, ymax}
            };
        }

        /**
         * Finds the area of the polygon. This is found given a simple formula
         * which is O(V).
         * @return the area of this polygon instance.
         */
        public double area () {
            double sum = 0d;
            int l = vertices.length;
            for (int i = 0; i < l; i++)
                sum += vertices[i].x * vertices[(i + 1) % l].y - 
                    vertices[i].y * vertices[(i + 1) % l].x;
            return Math.abs(sum / 2);
        }

    }

    /**
     * Convert the object-stored vertex and circle info int int[][][].
     * Also record the start and goal locations
     */
    private class Converter {

        private int[][][] coors;

        /**
         * Creates a new converter instance.
         * @param   shapes  Poly list of circles with vertices
         */
        public Converter (List<Poly> shapes) {
            coors = new int[shapes.size()][][];
            for (int a = 0; a < shapes.size(); a++) {
                Poly p = shapes.get(a);
                coors[a] = new int[p.vertices.length][];
                for (int b = 0; b < p.vertices.length; b++) {
                    coors[a][b] = new int[]{
                            (int) Math.round(p.vertices[b].x),
                            (int) Math.round(p.vertices[b].y)
                        };
                }
            }
        }

        /**
         * Accesses the coordinates.
         * @return  the coordinates of all shapes and vertices
         */
        private int[][][] getCoordinates () {
            return coors;
        }

        /**
         * Accesses the coordinates of the start position. This will
         * change if called twice.
         * @return  an array of x and y value of the start which
         * lies on the 0 side of the plane
         */
        private int[] getStartCoordinates () {
            return new int[]{3, ri(height/4, 3*height/4)};
        }

        /**
         * Accesses the coordinates of the goal position. This 
         * will change if called twice.
         * @return  an array of x and y value of the goal with
         * lies on the width side of the plane
         */
        private int[] getGoalCoordinates () {
            return new int[]{width - 3, ri(height/4, 3*height/4)};
        }
    }

}

