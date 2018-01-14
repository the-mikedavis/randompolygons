(ns poly-gen.dimensions)


(def width 500N)

(def height 300N)


(def margin-ratio 1/50)


(def inner-width (->> margin-ratio
                      (* 2N width)
                      (- width)))

(def inner-height (->> margin-ratio
                       (* 2N height)
                       (- height)))


(def width-margin (* width margin-ratio))

(def height-margin (* height margin-ratio))


(def average (/ width 20))

(def deviation (/ average 5))


(defn rand-radius []
  "Return a random int normalized to the average"
  (+ average (rand-int (inc (* 2 deviation)))))


(defn rand-x []
  "Returns a random x value leaving a small horizontal margin"
  (+ width-margin (rand-int (inc inner-width))))

(defn rand-y []
  "Returns a random y value leaving a small vertical margin"
  (+ height-margin (rand-int (inc inner-height))))
