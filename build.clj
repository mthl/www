(ns build
  (:refer-clojure :exclude [compile])
  (:require
   [clojure.java.io :as io]
   [clojure.tools.build.api :as b]))

(def basis (b/create-basis {:project "deps.edn"}))
(def class-dir "target/classes")
(def built-resources "target/resources")

(defn clean [_]
  (b/delete {:path "target"}))

(defn generate
  "Generate static resources required at runtime"
  [_]
  (.mkdirs (io/file built-resources))
  (let [schema (requiring-resolve 'fr.reuz.database/schema)
        facts (requiring-resolve 'fr.reuz.database/facts)
        db (requiring-resolve 'fr.reuz.database/db)
        rdf-triples (requiring-resolve 'fr.reuz.rdf/rdf-triples)]
    (spit (str built-resources "/schema.edn") @schema)
    (spit (str built-resources "/facts.edn") @facts)
    (spit (str built-resources "/data.ttl") (with-out-str (rdf-triples @db)))))

(defn compile [_]
  (b/copy-dir
   {:src-dirs ["resources" "target/resources"]
    :target-dir class-dir})
  (b/compile-clj
   {:basis basis
    :src-dirs ["src/main"]
    :class-dir class-dir
    :compile-opts {:elide-meta [:doc :file :line :added]
                   :direct-linking true}}))

(defn package
  "Construct an uberjar containing only useful class and resource files
  that are used at runtime."
  [_]
  (b/uber {:basis basis
           :class-dir class-dir
           :uber-file "target/reuz-standalone.jar"
           :main 'fr.reuz.main}))
