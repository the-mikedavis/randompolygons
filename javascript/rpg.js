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
                        x = Math.cos(angle) * c.radius + c.x,
                        y = Math.sin(angle) * c.radius + c.y;

                    c.vertices[i] = new Vertex(x, y, c.radius);
                    c.vertices[i].setangle = angle;

                    //  for proximity to other vertices testing
                    arr[i] = new Vertex(x, y, c.radius);

                    //  if you can't have your cake and eat it, just eat it.
                //  just make sure the point is on the map if you can't fit it.
                if (count++ > 50 &&
                    !pointIsntOnMap(c.vertices[i].x, c.vertices[i].y))
                        break;
                    //  repeat if the point is too close to others or off the map
                } while (pointIsntOnMap(c.vertices[i].x, c.vertices[i].y) || 
                    isContained(c.vertices, i));
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

            
        }

        //  grow the polygons
        for (let i = 0; i < data.length; i++) {
            let c = data[i];
            c.grow(3 * c.radius / 2);
            for (let setr = c.radius; isStrongContained(data, i) ||
                !strongIsOnMap(c); setr -= 1) {
                c.grow(setr);
            }
        }

        return data;
    }

    function isContained (data, index) {
        for (let i = 0; i < index; i++)
            if (data[i].overlaps(data[index]))
                return true;
        return false;
    }

    function isStrongContained (data, index) {
        for (let i = 0; i < data.length; i++)
            if (i !== index && data[i].strongOverlap(data[index]))
                return true;
        return false;
    }

    function pointIsntOnMap(x, y) {
        return !(x > 0 && x < width && y > 0 && y < height);
    }

    function strongIsOnMap (e) {
        let reduction = e.reduction();
        if (reduction.xmin > 5 && reduction.xmax < width - 5 &&
            reduction.ymin > 5 && reduction.ymax < height - 5)
            return true;
        return false;
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
            this.setangle = 0;
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

        grow (r) {
            this.radius = r;
            for (let v of this.vertices) {
                v.x = Math.cos(v.setangle) * r + this.x;
                v.y = Math.sin(v.setangle) * r + this.y;
            }
        }

        reduction () {
            let xmin = 100000, xmax = -1,
                ymin = 100000, ymax = -1;
            for (let e of this.vertices) {
                if (e.x < xmin)
                    xmin = e.x;
                else if (e.x > xmax)
                    xmax = e.x;

                if (e.y < ymin)
                    ymin = e.y;
                else if (e.y > ymax)
                    ymax = e.y;
            }

            return {
                xmin, xmax, ymin, ymax
            };
        }

        strongOverlap (o) {
            //  weed out those too far away from one another
            if (!this.overlaps(o))
                return false;

            for (let i = 0; i < this.vertices.length; i++)
                for (let j = 0; j < o.vertices.length; j++)
                    if (rpg.lineIntersects(this.vertices[i].x,
                        this.vertices[i].y,
                        this.vertices[i + 1 == this.vertices.length ? 0 : i + 1].x,
                        this.vertices[i + 1 == this.vertices.length ? 0 : i + 1].y,
                        o.vertices[j].x, o.vertices[j].y,
                        o.vertices[j + 1 == o.vertices.length ? 0 : j + 1].x,
                        o.vertices[j + 1 == o.vertices.length ? 0 : j + 1].y))
                            return true;

            let tred = this.reduction();
            let ored = o.reduction();

            if ((tred.xmin < ored.xmin && tred.xmax > ored.xmax &&
                tred.ymin < ored.ymin && tred.ymax > ored.ymax) || 
                (ored.xmin < tred.xmin && ored.xmax > tred.xmax &&
                ored.ymin < tred.ymin && ored.ymax > ored.ymax))
                return true;

            return false;
        }

        equals (o) {
            return this.x == o.x && this.y == o.y && this.vx == o.vx && this.vy == o.vy
                && this.radius == o.radius;
        }

    }

    //  adapted from https://gist.github.com/Joncom/e8e8d18ebe7fe55c3894
    rpg.lineIntersects = function (p0_x, p0_y, p1_x, p1_y, p2_x, p2_y, p3_x, p3_y) {
        var s1_x, s1_y, s2_x, s2_y;
        s1_x = p1_x - p0_x;
        s1_y = p1_y - p0_y;
        s2_x = p3_x - p2_x;
        s2_y = p3_y - p2_y;

        var s, t;
        s = (-s1_y * (p0_x - p2_x) + s1_x * (p0_y - p2_y)) / (-s2_x * s1_y + s1_x * s2_y);
        t = ( s2_x * (p0_y - p2_y) - s2_y * (p0_x - p2_x)) / (-s2_x * s1_y + s1_x * s2_y);

        return (s >= 0 && s <= 1 && t >= 0 && t <= 1);
    }

})(window);
