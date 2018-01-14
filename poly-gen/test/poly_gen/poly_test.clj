(ns poly-gen.poly-test
  (:require [clojure.test :refer :all]
            [poly-gen.poly :refer :all]))


(deftest unique-rand-polies
  (testing "polygon's points are unique"
    (is 
