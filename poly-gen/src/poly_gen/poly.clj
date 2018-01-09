(ns poly-gen.poly
  (:require [poly-gen.vertex :as vertex])
  (:refer-clojure :exclude [contains?]))

(defn make
  "Create a new polygon explicitly, either with vertices or a radius."
  ([radius center]
   {:r radius :c center})
  ([& points]
   {:r radius :c center :points points}))

(defn- sqr
  "Square a number"
  [n]
  (* n n))

(defn in-range?
  "Checks if two circles are close enough to overlap using the Euclidean
  distance formula."
  [a b]
  (<= (vertex/distance (:c a) (:c b))
      (+ (:r a) (:r b))))

(defn intersect?
  "Checks if two polygons have any intersecting lines"
  [a b]
  false)


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
