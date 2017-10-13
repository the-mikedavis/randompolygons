# JavaScript-Based Random Convex Polygon Field Generator

How to use:
```js
rpg.render(width, height, n);
```

Where the width and height are arbitrary and `n` is the number of polygons desired. The output is like this:

```
[
    {
        vertices : [
            { x, y },
            { x, y },
            { x, y },
            { x, y }
        ]
    },
    { 
        vertices : [
            { x, y },
            { x, y },
            { x, y }
        ]
    },
        ...
]
```

The output is an array of `body` objects, which have an array `vertices` property. Each vertex is an object with an x and y properties. It is not recommended to exceed 100 polygons. I have not tested the output above this value.

The output is already sorted clockwise, so an implementation like this can be used to draw the edges between vertices:

```js
for (let c : field)
    for (let i = 0; i < c.vertices.length; i++)
        drawEdge(c.vertices[i].x, c.vertices[i].y,
            c.vertices[i + 1 == c.vertices.length ? 0 : i + 1].x,
            c.vertices[i + 1 == c.vertices.length ? 0 : i + 1].y);
```

To see a working version made by d3.js, see my site [here](https://mcarsondavis.com/projects/rpfp).
