(ns build
  (:refer-clojure :exclude [compile])
  (:require
   [clojure.java.io :as io]
   [clojure.tools.build.api :as b]))

(def run (b/create-basis {:project "deps.edn"
                          :aliases [:run]}))

(def class-dir "target/classes")
(def built-resources "target/resources")

(defn clean [_]
  (b/delete {:path "target"}))

(defn compile [_]
  (b/copy-dir
   {:src-dirs ["resources" "target/resources"]
    :target-dir class-dir})
  (b/compile-clj
   {:basis run
    :src-dirs ["src/run"]
    :class-dir class-dir
    :compile-opts {:elide-meta [:doc :file :line :added]
                   :direct-linking true}}))

(defn package
  "Construct an uberjar containing only useful class and resource files
  that are used at runtime."
  [_]
  (b/uber {:basis run
           :class-dir class-dir
           :uber-file "target/reuz-standalone.jar"
           :main 'fr.reuz.main}))
