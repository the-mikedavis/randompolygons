(ns poly-gen.line)

(defn make
  "Create a line from vertices"
  [[xa ya] [xb yb]]
  (list (list xa ya) (list xb yb))) ; explicitly defined for clarity

(defn vertices->lines
  "Morph a polygons points into a list of lines"
  [points]
  (loop [lines []
         recent (first points)
         pts (rest points)]
    (if (empty? pts)
      (conj lines (make (first points) (last points))) ; the wrap-around
      (recur (conj lines (make recent (first pts)))
             (first pts) (rest pts)))))

