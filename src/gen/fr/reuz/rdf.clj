(ns fr.reuz.rdf
  (:require
   [datascript.core :as d]
   [grafter-2.rdf.protocols :as rdf]
   [grafter-2.rdf4j.io :as io]
   [ont-app.vocabulary.core :as v])
  (:import
   java.net.URI))

(v/put-ns-meta!
 'vcard
 {:vann/preferredNamespacePrefix "vcard"
  :vann/preferredNamespaceUri "http://www.w3.org/2006/vcard/ns#"})

(v/put-ns-meta!
 'doap
 {:vann/preferredNamespacePrefix "doap"
  :vann/preferredNamespaceUri "http://usefulinc.com/ns/doap#"})

(v/put-ns-meta!
 'org
 {:vann/preferredNamespacePrefix "org"
  :vann/preferredNamespaceUri "http://www.w3.org/ns/org#"})

(v/put-ns-meta!
 'dcterms
 {:vann/preferredNamespacePrefix "dcterms"
  :vann/preferredNamespaceUri "http://purl.org/dc/terms/"})

(v/put-ns-meta!
 'time
 {:vann/preferredNamespacePrefix "time"
  :vann/preferredNamespaceUri "http://www.w3.org/2006/time#"})

(defn subjects-uri-mapping
  "Construct a mapping between :db/id and URI other will be blank nodes."
  [db]
  (let [subjects (->> (d/q '[:find ?x ?subject
                             :where
                             [?x :rdf/id ?subject]]
                           db)
                      (into {} (map (fn [[k v]] [k (URI. v)]))))
        kw-subjects (->> (d/q '[:find ?x ?subject
                                :where
                                [?x :rdf/about ?subject]
                                (not [?x :rdf/id _])]
                              db)
                         (into {} (map (fn [[k v]] [k (-> v v/uri-for URI.)]))))
        blanks (->> (d/q '[:find [?x ...]
                           :where
                           [?x _ _]
                           (not [?x :rdf/about _])
                           (not [?x :rdf/id _])]
                         db)
                    (into {} (map (fn [x] [x (rdf/make-blank-node x)]))))]
    (merge subjects kw-subjects blanks)))

(defn value-transformation
  "Construct a mapping for every property to a transformation to perform to its value"
  [schema id->uri]
  (let [refs (reduce-kv (fn [acc property {:db/keys [valueType]}]
                          (if (= valueType :db.type/ref)
                            (conj acc property)
                            acc))
                        #{}
                        schema)]
    (fn [p o]
      (if (contains? refs p) (id->uri o) o))))

(defn graph->triples
  [db]
  (let [id->uri (subjects-uri-mapping db)
        transform (value-transformation (d/schema db) id->uri)]
    (->> (d/q '[:find ?s ?p ?o
                :where
                [?s ?p ?o]
                (not [?x :rdf/about ?o])
                (not [?x :rdf/id ?o])]
              db)
         (sort-by first)
         (keep (fn [[s p o]]
                 (when-not (contains? #{"http" nil} (namespace p))
                   (rdf/->Triple (id->uri s)
                                 (-> p v/uri-for URI.)
                                 (transform p o))))))))

(defn- prefixes []
  (reduce-kv (fn [acc k v]
               (let [ns (-> v v/get-ns-meta :vann/preferredNamespaceUri)]
                 (assoc acc k ns)))
             {}
             (v/prefix-to-ns)))

(defn rdf-triples
  [db]
  (let [target (io/rdf-writer *out*
                              :format :ttl
                              :prefixes (prefixes))]
    (rdf/add target (graph->triples db))))
