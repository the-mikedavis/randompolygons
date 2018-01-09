(ns poly-gen.vertex)

; vertices are seqs with '(x y)

(defn- sqr
  "Square a number"
  [n]
  (* n n))

(defn distance
  "Find the distance between two points"
  [[xa ya] [xb yb]]
  (let [vert (Math/abs (- yb ya))
        horiz (Math/abs (- xb xa))]
    (Math/sqrt (+ (sqr horiz) (sqr vert)))))
