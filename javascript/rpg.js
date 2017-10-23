(function (global) {

    const rpg = global.rpg || {};
    global.rpg = rpg;
    var width, height;

    rpg.render = function (w, h, count) {
        width = w;
        height = h;

        const data = new Array(count),
            maxr = 2 * Math.sqrt(width * height) / (count),
            minr = width / 50;

        data[0] = new Poly(ri(5, width - 5), ri(5, height - 5),
            ri(minr, maxr));

        let r = 20;
        for (let i = 1; i < data.length; i++) {
            //  create a toddler circle which is not contained by a circle
            //  the minimum radius is enforced here
            //  This is a strict while loop. If the data does not satisfy
            //  the conditions, the map cannot be made
            do {
                data[i] = new Poly(ri(5, width - 5), ri(5, height - 5), r);
            } while (isContained(data, i));
            // expand this toddler circle out as far as possible, but still
            // randomly
            data[i].radius = ri(3*maxr/4, maxr);
            for (let rad = data[i].radius; isContained(data, i); rad -= 5)
                data[i].radius = rad;
        }

        //  on each shape, create the vertices
        for (let c of data) {
            let arr = [];
            for (let i = 0; i < c.sides; i++) {
                let count = 0;
                do {
                    //  generate a random point on the circumference
                    let angle = Math.random() * Math.PI * 2,
                        x = Math.cos(angle) * c.radius,
                        y = Math.sin(angle) * c.radius;

                    c.vertices[i] = {
                        x : Math.floor(x) + c.x,
                        y : Math.floor(y) + c.y
                    };

                    //  for proximity to other vertices testing
                    arr[i] = new Vertex(x, y, c.radius);

                    //  if you can't have your cake and eat it, just eat it.
                //  just make sure the point is on the map if you can't fit it.
                if (count++ > 50 &&
                    !pointIsntOnMap(c.vertices[i].x, c.vertices[i].y))
                        break;
                    //  repeat if the point is too close to others or off the map
                } while (pointIsntOnMap(c.vertices[i].x, c.vertices[i].y) || 
                    isContained(arr, i));
            }

            //  attach the angle, by which these are sorted
            let start;
            for (let e of c.vertices) {
                let ang = Math.atan2(e.y - c.y, e.x - c.x);
                if (!start)
                    start = ang;
                else if (ang < start)
                    ang += Math.PI * 2;
                e.angle = ang;
            }

            //  sort into clockwise order for rendering
            c.vertices.sort((a, b) => a.angle - b.angle);

            //  grow the polygons
        }

        return data;
    }

    function isContained (data, index) {
        for (let i = 0; i < index; i++)
            if (data[i].overlaps(data[index]))
                return true;
        return false;
    }

    function pointIsntOnMap(x, y) {
        return !(x > 0 && x < width && y > 0 && y < height);
    }

    function ri (min, max) {
        min = Math.ceil(min);
        max = Math.floor(max);
        return Math.floor(Math.random() * (max - min + 1)) + min;
    }

    class Vertex {
        constructor (x, y, d) {
            this.x = x;
            this.y = y;
            this.d = d;
            this.angle = 0;
        }

        overlaps (o) {
            let dx = o.x - this.x,
                dy = o.y - this.y,
                distance = Math.sqrt(dx*dx + dy*dy)
            return distance <= this.d - 1;
        }
    }


    class Poly {

        constructor (x, y, r) {
            this.x = x;
            this.y = y;
            this.radius = r;
            this.sides = ri(3,6);
            this.vertices = [];
        }

        overlaps (o) {
            let dx = o.x - this.x,
                dy = o.y - this.y,
                distance = Math.sqrt(dx*dx + dy*dy)
            return distance <= o.radius + this.radius + 3;
        }

        equals (o) {
            return this.x == o.x && this.y == o.y && this.vx == o.vx && this.vy == o.vy
                && this.radius == o.radius;
        }

    }

})(window);
