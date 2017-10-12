public class Vessel {

    private int[][][] coors;
    private int[] start, goal;

    /**
     * Creates a new vessel with field data.
     * @param   coors   coordinates of all points in the space
     * organized by shape
     * @param   start   the coordinates of the start node
     * @param   goal    the coordinates of the goal node
     */
    public Vessel (int[][][] coors, int[] start, int[] goal) {
        this.coors = coors;
        this.start = start;
        this.goal = goal;
    }

    /**
     * Accesses the coordinates of all shapes on the plane.
     * This is a triple array. The first layer is shapes, e.g.
     * a triangle or rectangle. It contains a double array.
     * The double array is for the vertices. The last array
     * inside that holds the x and y coordinates. This is
     * an array of length 2.
     * @return  a triple array of coordinates
     */
    public int[][][] getCoordinates () {
        return coors;
    }

    /**
     * Accesses the start coordinate pair.
     * @return the x and y values of the start position.
     */
    public int[] getStartCoordinates () {
        return start;
    }

    /**
     * Accesses the goal coordinate pair.
     * @return the x and y values of the goal position.
     */
    public int[] getGoalCoordinates () {
        return goal;
    }
}
