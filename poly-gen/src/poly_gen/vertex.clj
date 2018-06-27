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

(defn rand-x
  "Create a random x coordinate given a radius and angle"
  [r angle]
  (-> angle
      Math/cos
      (* radius)
      Math/ceil))

(defn rand-y
  "Create a random y coordinate given a radius and angle"
  [r angle]
  (-> angle
      Math/sin
      (* radius)
      Math/ceil))

(defn rand
  "Create a random vertex [(x y) pairing] in bounds"
  ([]
    (list (dims/rand-x) (dims/rand-y)))
  ([[cx cy] radius]
   (let [angle (* Math/PI 2.0 (clojure.core/rand))
         x (Math/ceil (* radius (Math/cos(angle))))
         y (Math/ceil (* radius (Math/sin(angle))))]
     (list (+ cx x) (+ cy y)))))


(defn equals?
  "Checks if one point is the same as another"
  [a b]
  (= a b))


(defn unique?
  "Checks if a point is different from the rest of the points in a list."
  [points point]
  (not-any? (partial equals? point)
            points))
