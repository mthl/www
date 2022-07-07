(ns build
  (:refer-clojure :exclude [compile])
  (:require
   [clojure.java.io :as io]
   [clojure.tools.build.api :as b]))

(def webapp
  (binding [b/*project-root* "webapp"]
    (b/create-basis {:project "deps.edn"})))

(def class-dir (-> "target/classes" io/file .getAbsolutePath))

(defn clean [_]
  (b/delete {:path "target"})
  (b/delete {:path "resources/target/resources"}))

(defn compile [_]
  (b/copy-dir
   {:src-dirs ["webapp/resources"]
    :target-dir class-dir})
  (binding [b/*project-root* "webapp"]
    (b/compile-clj
     {:basis webapp
      :src-dirs ["src"]
      :class-dir class-dir
      :compile-opts {:elide-meta [:doc :file :line :added]
                     :direct-linking true}})))

(defn package
  "Construct an uberjar containing only useful class and resource files
  that are used at runtime."
  [_]
  (b/uber {:basis webapp
           :class-dir class-dir
           :uber-file "target/reuz-standalone.jar"
           :main 'fr.reuz.main}))
