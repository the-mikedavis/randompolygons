import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.awt.geom.Line2D;

/**
 * Generates a random field of <b>convex</b> polygons.
 *
 * RCP stands for Random Convex Polygon.
 *
 * How to use:
{@code: 
RCPGenerator generator = new RCPGenerator(width, height);
generator.render(n);
int[][][] shapeCoordinates = generator.getCoordinates();
int[] startCoor = generator.getStart();
int[] goalCoor = generator.getGoal();
}
 *
 * Coordinates come in the form of <code>int[]{x, y}</code>.
 * Each vertex of a shape has an array of x and y. Each shape has
 * an array of vertices (<code>int[][]</code>). The whole field
 * which is returned by <code>generator.getCoordinates()<code>.
 * is an array of shapes (<code>int[][][]</code>).
 *
 * I recommend using this triple array to create objects (object
 * shape, object vertex, object edge, etc), but the array is
 * usable in its raw form.
 * <hr>
 * The method this tool employs is:
 *
 * 1. Create a 2D plane with width and height.
 * 2. Create a circle with an arbitrary radius.
 * 3. For the specified count of shapes, add a circle with
 * a minimum radius and a random "sides" property.
 * 4. Make sure that new circle is not contained or intersected
 * by another circle on the map. If so, give it new random
 * coordinates until it's free.
 * 5. Expand the radius of that node to a random amount.
 * 6. If that circle and another overlap, shrink the radius by
 * a fixed amount until it fits.
 * 7. Repeat until the shape array has filled with circles
 * to the amount specified.
 * <hr>
 * Now a field of random non-overlapping circles has been
 * generated.
 * For each circle:
 * Create a vertex which is not outside the map nor too close
 * to another vertex on that shape. The "minimum" distance is
 * the radius of the circle, but that can be overriden if a
 * point cannot be created (results in an infinite loop).
 * The "minimum" distance makes the convex shapes larger
 * when not positioned at the edges of the plane. Without it,
 * the shape space comes off as stringy and awkward.
 * Then the list of vertices is sorted, which is highly important.
 * The vertices must be sorted in either a clockwise or 
 * anticlockwise nature so that when the edges or each shape
 * is drawn, a convex polygon is formed without any edges
 * crossing over eachother. This enforces convex-ness.
 *
 * @author Michael C. Davis
 *
 * authored on vim
 * vim &gt; IDE
 */
public class RCPGenerator {

    private int width, height, maxr, minr;
    private int[][][] coordinates = new int[0][][];
    private int[] start = new int[0], goal = new int[0];

    /**
     * Creates a generator with default 500, 300 dimensions.
     */
    public RCPGenerator () {
        this(500, 300);
    }

    /**
     * Creates a generator with width x width
     * dimensions.
     * @param   width   width and height of the 2D plane
     */
    public RCPGenerator (int width) {
        this(width, width);
    }

    /**
     * Creates a generator with width x height dimensions.
     * @param   width   width of the 2D plane
     * @param   height  height of the 2D plane
     */
    public RCPGenerator (int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Renders a new 2D plane filled with 8 polygons.
     * @return  a vessel object, with has coordinates,
     * start, and goal properties.
     */
    public boolean render () {
        return render(8);
    }

    /**
     * Renders a new 2D plane filled with n polygons.
     * @param   count   the number of polygons in the field
     * @return  true when complete.
     * start, and goal properties.
     */
    public boolean render (int count) {
        double targetDensity = 0.35;
        //  start with an enforced minimum of 8
        count = count > 0 ? count : 8;

        List<Poly> shapes = new ArrayList<Poly>(60);

        //  adjust the min and max radii for the count
        //  and aspect ratio
        //maxr = (int) (2 * Math.sqrt(width * height) / count);
        maxr = width / 5;
        minr = width / 25;

        //  create as many circles
        //  as the specified number of shapes
        do {
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

        //  perform a tight fit of all polygons, expanding the radii
        for (Poly s : shapes) {
            s.grow(2 * s.radius);
            for (double setr = s.radius; 
                    isStrongContained(shapes, s) ||
                    !strongIsOnMap(s); setr -= 2)
                s.grow(setr);
        }

        //  prep the data for export
        Converter c = new Converter(shapes);

        coordinates = c.getCoordinates();
        start = c.getStartCoordinates();
        goal = c.getGoalCoordinates();

        return true;
    }

    public void populateVertices (Poly c) {
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
    private boolean pointIsntOnMap(int x, int y) {
        return !(x > 0 && x < width && y > 0 && y < height);
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
        double mapArea = (width - 6) * (height - 6);
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

    /** Generates a random integer between [min, max] inclusive.
     * @param   min minimum bound
     * @param   max maximum bound, which can be produced
     */
    public static int randomInt (int min, int max) {
        min = min;
        max = max;
        return (int) Math.round((Math.random() * (max - min + 1) + min));
    }

    /** Point on a circle. */
    public class Vertex implements Comparable<Vertex> {

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
    public class Poly extends Vertex {

        public int sides;
        public Vertex[] vertices;

        /**
         * Creates a new circle.
         * @param   x   x coordinate
         * @param   y   y coordinate
         * @param   r   radius of the circle
         */
        public Poly (double x, double y, double r) {
            super(x, y, r);
            this.sides = (int) (Math.random() * (7 - 3)) + 3;
            this.vertices = new Vertex[this.sides];
        }

        public Poly (int[][] coordinates) {
            vertices = new Vertex[coordinates.length];
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
         * Checks equality cheaply.
         * @param   o   the other circle
         * @return  true if they have the same coordinates
         * and radii
         */
        public boolean equals (Poly o) {
            return this.x == o.x &&
                this.y == o.y &&
                this.radius == o.radius;
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
                    if (Line2D.linesIntersect(vertices[i].x, vertices[i].y,
                                vertices[(i + 1) % len].x,
                                vertices[(i + 1) % len].y,
                                o.vertices[j].x, o.vertices[j].y,
                                o.vertices[(j + 1) % o.vertices.length].x,
                                o.vertices[(j + 1) % o.vertices.length].y))
                        return true;

            //  this is the point in the polygon problem which is solved by ray
            //  tracing.

            //  check if o is inside this by counting ray traces to 0. If it's
            //  odd, it's inside. If even, it's outside
            if (rayIntersections(this, o) % 2 == 1)
                return true;

            //  check if this is inside o
            return (rayIntersections(o, this) % 2 == 1);
        }

        //  use statically
        public int rayIntersections (Poly a, Poly b) {
            int intersections = 0;
            int len = a.vertices.length;

            for (int i = 0; i < len; i++) {
                if (Line2D.linesIntersect(a.vertices[i].x, a.vertices[i].y,
                            a.vertices[(i + 1) % len].x,
                            a.vertices[(i + 1) % len].y,
                            b.x, b.y, 0D, 0D)) {
                    intersections++;
                    //  check for an endpoint intersection half of the time
                    if (intersections % 2 == 0 && intersectsOnEndpoint(
                                a.vertices[i].x, a.vertices[i].y,
                                a.vertices[(i + 1) % len].x,
                                a.vertices[(i + 1) % len].y,
                                b.x, b.y, 0D, 0D))
                        intersections--;
                }
            }
            return intersections;
        }

        public boolean intersectsOnEndpoint(double x1, double y1,
                double x2, double y2, double x3, double y3,
                double x4, double y4) {
            double m1, m2, b1, b2;
            if (x2 - x1 == 0)
                m1 = 1000000D;
            else
                m1 = ((y2 - y1) / (x2 - x1));
            b1 = (y1 - m1 * x1);

            if (x4 - x3 == 0)
                m2 = 1000000D;
            else
                m2 = ((y4 - y3) / (x4 - x3));
            b2 = (y3 - m2 * x3);

            double x = (-(b2 - b1)) / (m2 - m1);

            return doubleEquals(x, x1) || doubleEquals(x, x2);
        }

        public boolean doubleEquals(double a, double b) {
            if (Math.abs(a - b) < 2)
                System.out.println("a=" + a + ". b=" + b);
            return Math.abs(a - b) < 0.5;
        }

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

