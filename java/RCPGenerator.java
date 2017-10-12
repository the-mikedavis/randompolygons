import java.util.Arrays;
import java.util.ArrayList;

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
        //  start with an enforced minimum of 8
        count = count > 0 ? count : 8;

        Body[] data = new Body[count];

        //  adjust the min and max radii for the count
        //  and aspect ratio
        maxr = (width * width) / (height * count);
        minr = width / 50;
        //  create the first circle totally randomly
        data[0] = new Body(ri(5, width - 5), ri(5, height - 5),
                ri(minr, maxr));

        //  create as many circles
        //  as the specified number of shapes
        int r = minr;
        for (int i = 1; i < data.length; i++) {
            //  create a new point until it's free from other circles
            do {
                data[i] = new Body(ri(5, width - 5), ri(5, height - 5), r);
            } while (isContained(data, i));

            //  shrink the circle from randomly large to fit if needed
            data[i].radius = ri(3*maxr/4, maxr);
            for (int rad = (int) data[i].radius; rad >= r && isContained(data, i); rad -= 5) {
                data[i].radius = rad;
            }
        }

        //  add vertices to each circle
        for (int ct = 0; ct < data.length; ct++) {
            Body c = data[ct];
            //  for each side (or vertex, it's the same)
            for (int i = 0; i < c.sides; i++) {
                //  provide a way to break out of solutions
                int whilecount = 0;
                do {
                    double angle = Math.random() * Math.PI * 2,
                           x = Math.cos(angle) * c.radius,
                           y = Math.sin(angle) * c.radius;
                    c.vertices[i] = new Vertex(x + c.x, y + c.y, c.radius);
                    //  break out if the min distance is unsatisfiable
                    if (whilecount++ > 100 &&
                            !pointIsntOnMap(c.vertices[i].x, c.vertices[i].y))
                        break;
                    //  repeat while the point isn't on the map or
                    //  it's to close to another vertex
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

            //  sort the data here
            Arrays.sort(c.vertices);
        }

        //  get the new data
        Converter c = new Converter(data);

        coordinates = c.getCoordinates();
        start = c.getStartCoordinates();
        goal = c.getGoalCoordinates();

        return true;
    }

    /** Gets the rendered coordinate field. If render has
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
     * butis Theta(n)
     * @param   arr the body array
     * @param   index   the index of the node to test
     */
    private boolean isContained (Body[] arr, int index) {
        for (int i = 0; i < index; i++)
            if (arr[i].overlaps(arr[index]))
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
    private class Vertex implements Comparable<Vertex> {

        public double x, y, radius;
        public double angle;

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

        /**
         * Checks if one Vertex is too close to the other
         * @param   o   the other vertex instance
         * @return  true if they are too close
         */
        public boolean overlaps (Vertex o) {
            double dx = o.x - this.x,
                dy = o.y - this.y,
                distance = Math.sqrt(dx*dx + dy*dy);
            return distance <= this.radius - 1D;
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
    private class Body extends Vertex {

        public int sides;
        public Vertex[] vertices;

        /**
         * Creates a new circle.
         * @param   x   x coordinate
         * @param   y   y coordinate
         * @param   r   radius of the circle
         */
        public Body (double x, double y, double r) {
            super(x, y, r);
            this.sides = ri(3, 6);
            this.vertices = new Vertex[this.sides];
        }

        /**
         * Checks if this circle overlaps another.
         * @param   o   the other circle
         * @return true if they intersect
         */
        public boolean overlaps (Body o) {
            double dx = o.x - this.x,
                dy = o.y - this.y,
                distance = Math.sqrt(dx*dx + dy*dy);
            return distance <= o.radius + this.radius;
        }

        /**
         * Checks equality cheaply.
         * @param   o   the other circle
         * @return  true if they have the same coordinates
         * and radii
         */
        public boolean equals (Body o) {
            return this.x == o.x &&
                this.y == o.y &&
                this.radius == o.radius;
        }
    }

    /**
     * Convert the object-stored vertex and circle info int int[][][].
     * Also record the start and goal locations
     */
    private class Converter {

        private Body[] shapes;
        private int[][][] coors;

        /**
         * Creates a new converter instance.
         * @param   shapes  Body array of circles with vertices
         */
        public Converter (Body[] shapes) {
            this.shapes = shapes;    
            coors = new int[shapes.length][][];
            for (int a = 0; a < shapes.length; a++) {
                coors[a] = new int[shapes[a].vertices.length][];
                for (int b = 0; b < shapes[a].vertices.length; b++) {
                    coors[a][b] = new int[]{
                            (int) shapes[a].vertices[b].x,
                            (int) shapes[a].vertices[b].y
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

        /**
         * Exports the converted coordinates to a Vessel. The
         * Vessel will hold all data statically.
         * @return  a new vessel with all points.
        public Vessel export () {
            return new Vessel(coors, getStartCoordinates(), getGoalCoordinates());
        }
        */
    }
}

