# Java-Based Random Convex Polygon Field Generator

N.B.: if you need to check for line segments intersecting, **do not** use
`Lines2D.linesIntersect`. Instead, use `Polygen.segmentsIntersect` from this
file. `Lines2D.linesIntersect` does not cover some edge cases.

How to use:

```java
Polygen gen = new Polygen();
gen.render();
int[][][] coordinates = gen.getCoordinates();
int[] start = gen.getStart();
int[] goal = gen.getGoal();
```


The output of the coordinates looks something like this:

```
field
    shape
        vertex
        vertex
        vertex
    shape
        vertex
        vertex
        vertex
        vertex
    shape
        vertex
        vertex
        vertex
        vertex
    ...
``` 

Where the shapes represent a set of vertices connected by edges and the 
vertices represent a pair of integers for x and y value.

Specifically, this is an example

```java
int[][][] coordinates = new int[][][]{
    { //a five sided shape
        { 15, 40 }, //one of its vertices
        { 60, 3 },
        { 91, 40 },
        { 70, 90 },
        { 20, 80 }
    },
    { //a four sided shape
        { 30, 110 }, //one of its vertices
        { 150, 110 },
        { 151, 150 },
        { 29, 150 }
    },
    { //etc.
        { 90, 100 },
        { 110, 30 },
        { 125, 100 }
    },
    {
        { 130, 5 },
        { 160, 3 },
        { 179, 25 },
        { 131, 55 }
    },
    {
        { 161, 75 },
        { 195, 105 },
        { 170, 130 }
    },
    {
        { 190, 8 },
        { 241, 8 },
        { 240, 85 },
        { 189, 85 }
    },
    {
        { 245, 90 },
        { 271, 110 },
        { 270, 140 },
        { 239, 150 },
        { 221, 140 },
        { 220, 110 }
    },
    {
        { 265, 5 },
        { 280, 20 },
        { 270, 95 },
        { 248, 15 }
    }
};
```
Moreover, the vertices are already in sorted order (clockwise), which means
that you can draw a shape using an implementation of this psuedocode:

```
//assuming 0 indexing
for a = 0 to shapes.length (exclusive)
    for b = 0 to the shape's vertices.length (inclusive)
        draw edge from vertices[b] to vertices[(b + 1) % vertices.length]
```

