(ns fr.reuz.static
  (:require
   [clojure.java.io :as io]
   [fr.reuz.database :as db]
   [fr.reuz.rdf :as rdf]))

(defn- mkdir [path]
  (-> path io/file .mkdirs))

(defn generate
  "Generate static resources required at runtime"
  [{:keys [output-dir]}]
  (mkdir output-dir)
  (spit (io/file output-dir "schema.edn") db/schema)
  (spit (io/file output-dir "facts.edn") db/facts)
  (spit (io/file output-dir "data.ttl")
        (with-out-str (rdf/rdf-triples db/db))))

