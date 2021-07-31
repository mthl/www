(ns build
  (:refer-clojure :exclude [compile])
  (:require
   [clojure.tools.build.api :as b]
   [hf.depstar.api :as d]))

(def basis (b/create-basis {:project "deps.edn"}))
(def class-dir "target/classes")

(defn clean [_]
  (b/delete {:path "target"}))

(defn compile [_]
  (b/compile-clj
   {:basis basis
    :src-dirs ["src"]
    :class-dir class-dir
    :compile-opts {:elide-meta [:doc :file :line :added]
                   :direct-linking true}}))

(defn package
  "Construct an uberjar containing only useful class and resource files
  that are used at runtime."
  [_]
  (d/uber {:basis basis
           :class-dir class-dir
           :uber-file "target/reuz-standalone.jar"
           :main 'fr.reuz.main
           :exclude ["org/commonmark/.*"
                     "org/python/.*"
                     "clygments/.*"
                     "pygments.*"
                     "Pygments.*"
                     "posts/.*\\.md$"
                     ".*\\.cljc?$"
                     ".*\\.py$"]}))
