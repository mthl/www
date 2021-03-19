(ns fr.reuz.blog
  (:gen-class)
  (:require
   [fr.reuz.cmark :as cmark]
   [datascript.core :as d]
   [hiccup.page :as hp]
   [clojure.java.io :as io]
   [clojure.instant :as inst])
  (:import java.time.OffsetDateTime))

(defmulti parse-value (fn [p o] p))

(defmethod parse-value :default [_ o] o)

(defmethod parse-value :dcterms/date
  [_ date]
  (inst/read-instant-calendar date))

(def meta-context
  "Mapping of markdown metadata keys to unambiguous namespaced keys"
  {:title :dcterms/title
   :creator :dcterms/creator
   :date :dcterms/date
   :subject :dcterms/subject})

(derive :http/slug 'owl/FunctionalProperty)
(derive :dcterms/date 'owl/FunctionalProperty)
(derive :dcterms/title 'owl/FunctionalProperty)

(defn normalize
  [content]
  (reduce-kv (fn [m k v]
               (let [k* (meta-context k k)
                     v* (map #(parse-value k* %) v)
                     v** (if (and (isa? k* 'owl/FunctionalProperty))
                           (first v*)
                           v*)]
                 (assoc m k* v**)))
             {}
             content))

(defn article
  [props]
  (let [{:dcterms/keys [title creator date subject] :keys [content]} props]
    (hp/html5
     {:lang "en"}
     [:head
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
      [:meta {:charset "utf-8"}]
      [:title title]
      [:link {:rel "stylesheet" :href "/style.css"}]
      [:link {:rel "stylesheet" :href "/pygments.css"}]]
     [:body
      [:header
       [:h2 title]
       [:h3 "by " (first creator) " - "
        (when-not (nil? date)
          (-> (.toZonedDateTime ^java.util.GregorianCalendar date)
              (.format java.time.format.DateTimeFormatter/RFC_1123_DATE_TIME)))]
       (into [:ul] (map #(vector :li %)) subject)]
      (into [:content] content)])))

(defn render
  [props]
  (assoc props :html (article props)))

(def ^{:arglists '([p])} compile-post
  "Extract data and pre-render HTML view of markdown article."
  (comp render normalize cmark/markdown->data io/resource))

(def posts
  (mapv compile-post
        ["posts/gsoc-2017-week-1.md"
         "posts/gsoc-2017-week-2+3.md"
         "posts/gsoc-2017-week-4.md"
         "posts/gsoc-2017-week-5.md"
         "posts/gsoc-2017-week-6.md"
         "posts/gsoc-2017-week-7+8.md"
         "posts/gsoc-2017-week-9.md"
         "posts/gsoc-2017-week-10+11.md"
         "posts/gsoc-2017-week-12.md"
         "posts/gsoc-2017-final-report.md"]))

(defn db-id
  "Get the db/id corresponding to a nickname"
  [db nick]
  (d/q '[:find ?me .
         :in $ ?nick
         :where
         [?me :vcard/nickname ?nick]]
       db nick))
