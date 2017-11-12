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
