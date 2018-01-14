(ns poly-gen.vertex
  (:require [poly-gen.dimensions :as dims])
  (:refer-clojure :exclude [rand]))

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


(defn rand
  "Create a random vertex [(x y) pairing] in bounds"
  ([]
    (list (dims/rand-x) (dims/rand-y)))
  ([[cx cy] radius]
   (let [angle (* Math/PI 2.0 (clojure.core/rand))
         x (Math/ceil (* radius Math/cos(angle)))
         y (Math/ceil (* radius Math/sin(angle)))]
     (list (+ cx x) (+ cy y)))))
