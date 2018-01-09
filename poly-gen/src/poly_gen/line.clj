(ns poly-gen.line
  (:import [java.awt.geom Line2D]))

(defn make
  "Create a line from vertices"
  [[xa ya] [xb yb]]
  (list (list xa ya) (list xb yb))) ; explicitly defined for clarity

(defn vertices->lines
  "Morph a polygon's list of points into a list of lines"
  [points]
  (loop [lines []
         recent (first points)
         pts (rest points)]
    (if (empty? pts)
      (conj lines (make (first points) (last points))) ; the wrap-around
      (recur (conj lines (make recent (first pts)))
             (first pts) (rest pts)))))

(defn intersect?
  "Tests if two lines intersect using vector cross product"
  [[[x1 y1] [x2 y2]]  [[x3 y3] [x4 y4]]]
  (Line2D/linesIntersect x1 y1 x2 y2 x3 y3 x4 y4))
