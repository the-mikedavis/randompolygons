import java.awt.geom.Line2D;

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
