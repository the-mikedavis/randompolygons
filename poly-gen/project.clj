(defproject poly-gen "0.1.0-SNAPSHOT"
  :description "Randomly generates a field of convex polygons."
  :url ""
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]]
  :main ^:skip-aot poly-gen.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
