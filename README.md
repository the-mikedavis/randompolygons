# Random Convex Polygon Field Generators

- Free as in speech
- Small API, easy to use

These scripts, in Java and JavaScript, allow the user to generate the coordinates of a 2D plane of randomly placed, randomly shaped convex polygons.

See the readme in each language folder to learn more about the specific implementations.

Sample outputs:
![sample](polygonfield0.png)

The circles are not in the output, but they show how the field is generated.

![sample](polygonfield1.png)

## How it works

**TL;DR**: this solution first places circles randomly and gives them randomly large radii. Then it places vertices in those circles and then sorts them clockwise.

---

Not all convex polygons can be circumscribed by a circle, but all polygons which _can_ be circumscribed by a circle are convex. This makes an easy way to generate them:

1. Add a circle of random radius to a random location on a 2D plane.
2. Add another circle with a fixed small radius to a random location to that 2D plane.
    - While this circle overlaps any other circle, remove it and repeat step 2.
3. Change the radius of the circle in step 2 to a randomly large amount.
    - While that circle overlaps any other circle, shrink the radius by a fixed amount until it no longer overlaps.
4. Repeat steps 2 and 3 for a random number of times (e.g. between 8 and 20 times).

Then at this point the plane is filled with random non-overlapping circles.

For each circle:

1. Generate a random angle between 0 and 2π, θ.
2. Add a “vertex” coordinate to the circle with x value cos θ times the circles radius plus the circle’s center and y sin θ times the radius plus the circle’s center, while that coordinate is on the 2D plane.
3. Add another vertex coordinate by the same method.
    - If this vertex is too close to the other vertex, remove it and repeat step 3. This ensures large enough shapes (without it, they come out as stringy and awkward). “Too close” is defined simply as the radius of the circle.
    - If you’ve tried to add this vertex 50 to 100 times without finding a good spot for it, just use any angle which results in the vertex being on the 2D plane.
4. Repeat until the circle has a random number of vertices between 3 and 6 (or however many sides you want your polygons to be).
5. Sort the vertices in a clockwise or anti-clockwise fashion.
    - This makes it so that when you draw the polygon, the edges connect to draw a polygon, not a twisted mess.
    - You can do this by converting the ordinal coordinates to polar (Math.atan2) with respect to the center of the circle, picking a random vertex to be the “start” vertex. Then if any other vertex has an angle that’s less than the start vertex, add 2π to its angle.

If then you require a start and goal vertex pair, you can choose random locations on the left and rightmost sides which are not inside of circles.

The time complexity seems to be θ(n^2) because of overlap checking, where n is the number of polygons. The functions are practically instant, however, because in most cases the number of polygons is very small (between 8 and 50).
