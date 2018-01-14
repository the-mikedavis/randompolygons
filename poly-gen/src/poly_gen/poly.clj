(ns poly-gen.poly
  (:require [poly-gen
             [vertex :as vertex]
             [line :as line]
             [dimensions :as dims]])
  (:refer-clojure :exclude [rand contains?]))


(defn rand-vertex-count []
  "Returns a random int between 3 and 7"
  (+ 3 (rand-int 5)))


(defn make
  "Create a new polygon explicitly, either with vertices or a radius."
  ([radius center]
   {:r radius :c center})
  ([radius center & points]
   {:r radius :c center :points points}))


(defn rand
  "Create a random polygon"
  ([]
   (rand dims/rand-to-scale))
  ([radius]
   (rand radius (vertex/rand)))
  ([radius [cx cy]]
   (rand radius '(cx cy) (rand-vertex-count)))
  ([radius [cx cy] vertex-count]
   nil))


(defn in-range?
  "Checks if two circles are close enough to overlap using the Euclidean
  distance formula."
  [a b]
  (<= (vertex/distance (:c a) (:c b))
      (+ (:r a) (:r b))))


;(defn intersect?
;  "Checks if two polygons have any intersecting lines"
;  [a b]
;  (let [lines-a (line/vertices->lines a)
;        lines-b (line/vertices->lines b)]
;    (loop [sect? false
;           current (first lines-a)
;           remaining (rest lines-a)]
;      (if (empty? remaining)
;        sect?
;        (recur (or sect?
;                   (some #(line/intersect? current %)
;                         remaining))
;               (first remaining)
;               (rest remaining))))))

(defn intersect?
  "Checks if two polygons have any intersecting lines. Runs in O(n*m) where
  n is the number of lines in a and m is the number of lines in b. Only truly
  runs in O(n*m) when they don't intersect. `some` will return quickly if they
  do intersect."
  [a b]
  (let [lines-a (line/vertices->lines a)
        lines-b (line/vertices->lines b)]
    (some (fn [line-a] (some #(line/intersect? line-a %) lines-b))
          lines-a)))

; distinct? can be used for coordinates (lists, vects, etc)

(defn contains?
  "Checks if a polygon contains another inside it using ray tracing."
  [a other]
  false)


(defn overlap?
  "Checks if two polygons (which have points) overlap. Makes significant
  use of short circuit evaluation."
  [a b]
  (and (in-range? a b)
       (or (intersect? a b)
           (contains? a b)
           (contains? b a))))
