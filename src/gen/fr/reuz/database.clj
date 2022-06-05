(ns fr.reuz.database
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [datascript.core :as d]
   [datascript.db :as db]
   [hiccup.page :as hp]
   [fr.reuz.util :as util]
   [fr.reuz.cmark :as cmark]
   [fr.reuz.core :as-alias reuz]))

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
       [:h3 "by " (first creator) " - " (util/render-date date)]
       (into [:ul] (map #(vector :li %)) subject)]
      (into [:content] content)])))

(defn render
  [props]
  (assoc props :html (article props)))

(def blog-posts
  (mapv (fn [path]
          (let [f (str "resources/posts/" path)]
            (-> f cmark/markdown->data render)))
        ["gsoc-2017-week-1.md"
         "gsoc-2017-week-2+3.md"
         "gsoc-2017-week-4.md"
         "gsoc-2017-week-5.md"
         "gsoc-2017-week-6.md"
         "gsoc-2017-week-7+8.md"
         "gsoc-2017-week-9.md"
         "gsoc-2017-week-10+11.md"
         "gsoc-2017-week-12.md"
         "gsoc-2017-final-report.md"]))

(def schema
  {:foaf/member {:db/cardinality :db.cardinality/many
                 :db/valueType :db.type/ref}
   :foaf/developer {:db/cardinality :db.cardinality/many
                    :db/valueType :db.type/ref}
   :rdf/type {:db/cardinality :db.cardinality/many
              :db/valueType :db.type/ref}
   :rdf/about {:db/unique :db.unique/identity}
   :dcterms/subject {:db/cardinality :db.cardinality/many}
   :dcterms/creator {:db/cardinality :db.cardinality/many}
   :doap/programming-language {:db/cardinality :db.cardinality/many}
   :vcard/address {:db/cardinality :db.cardinality/many
                   :db/valueType :db.type/ref}
   :vcard/email {:db/cardinality :db.cardinality/many
                 :db/valueType :db.type/ref}
   :foaf/account {:db/cardinality :db.cardinality/many
                  :db/valueType :db.type/ref}
   :foaf/has-gender {:db/valueType :db.type/ref}
   :org/hasMembership {:db/cardinality :db.cardinality/many
                       :db/valueType :db.type/ref}
   :org/role {:db/valueType :db.type/ref}
   :org/organization {:db/valueType :db.type/ref}
   :org/memberDuring {:db/valueType :db.type/ref}
   :org/hasSite {:db/valueType :db.type/ref}
   :org/siteAddress {:db/valueType :db.type/ref}
   :http/slug {:db/unique :db.unique/identity}
   :foaf/publications {:db/cardinality :db.cardinality/many
                       :db/valueType :db.type/ref}
   :doap/developer {:db/cardinality :db.cardinality/many
                    :db/valueType :db.type/ref}})

(def facts
  [{:rdf/id "https://reuz.fr/#me"
    :rdf/about ::reuz/me
    :rdf/type #{{:rdf/about :foaf/Person}
                {:rdf/about :vcard/Individual}}
    :vcard/nickname "mthl"
    :foaf/birthday "1986-07-11"
    :vcard/fn "Mathieu Lirzin"
    :foaf/given-name "Mathieu"
    :foaf/family-name "Lirzin"
    :foaf/has-gender {:rdf/about :vcard/Male}
    :foaf/img "/images/mthl.png"
    :foaf/homepage "https://reuz.fr"
    :foaf/weblog "/blog"
    :vcard/address {:rdf/type {:rdf/about :vcard/Address}
                    :vcard/locality "Saint Cyr sur Loire"
                    :vcard/postal-code "37540"
                    :vcard/region "Centre-Val de Loire"
                    :vcard/country-name "France"}
    :vcard/email #{{:rdf/id "mailto:mthl@reuz.fr"
                    :rdfs/label "mthl@reuz.fr"
                    :rdf/type {:rdf/about :vcard/Email}}
                   {:rdf/id "mailto:mathieu.lirzin@oscaro.com"
                    :rdfs/label "mathieu.lirzin@oscaro.com"
                    :rdf/type {:rdf/about :vcard/Email}}
                   {:rdf/id "mailto:mthl@gnu.org"
                    :rdfs/label "mthl@gnu.org"
                    :rdf/type {:rdf/about :vcard/Email}}}
    :foaf/account [{:rdfs/label "Github"
                    :rdf/about ::reuz/github-account
                    :rdf/id "https://github.com/mthl"}
                   {:rdfs/label "Gitlab"
                    :rdf/id "https://gitlab.com/mthl"}
                   {:rdfs/label "Néréide Labs"
                    :rdf/id "https://labs.nereide.fr/mthl"}
                   {:rdfs/label "Savannah"
                    :rdf/id "https://savannah.gnu.org/users/mthl"}
                   {:rdfs/label "Notabug"
                    :rdf/id "https://notabug.org/mthl"}]
    :foaf/publications (set blog-posts)
    :org/hasMembership
    #{{:rdf/type {:rdf/about :org/Membership}
       :org/role
       {:rdfs/label "software developer"
        :rdfs/comment
        "research and developpement on the automobile parts and
      accessories catalog"}
       :org/organization
       {:rdf/type #{{:rdf/about :org/Organization}
                    {:rdf/about :foaf/Organization}}
        :foaf/name "Oscaro"
        :rdfs/label "Oscaro"
        :foaf/homepage "https://www.oscaro.com"
        :org/hasSite
        {:rdf/type {:rdf/about :org/Site}
         :rdf/id "https://www.openstreetmap.org/node/8987859088"
         :org/siteAddress
         {:rdf/type {:rdf/about :vcard/Address}
          :vcard/geo "geo:48.9198151,2.2989956"
          :vcard/street-address "34-40 rue Henri Barbusse"
          :vcard/postal-code "92230"
          :vcard/locality "Gennevilliers"
          :vcard/region "Île de france"
          :vcard/country-name "France"}}}
       :org/memberDuring {:rdf/type {:rdf/about :time/Interval}
                          :time/hasBeginning #inst "2020-10-01T00:00:00Z"}}
      {:rdf/type {:rdf/about :org/Membership}
       :org/role
       {:rdfs/label "software engineer"
        :rdfs/comment
        "research and developpement on the Apache OFBiz ERP framework"}
       :org/organization
       {:rdf/type #{{:rdf/about :org/Organization}
                    {:rdf/about :foaf/Organization}}
        :foaf/name "Néréide"
        :rdfs/label "Néréide"
        :foaf/homepage "https://nereide.fr"
        :org/hasSite
        {:rdf/type {:rdf/about :org/Site}
         :rdf/id "https://www.openstreetmap.org/node/4999813121"
         :org/siteAddress
         {:rdf/type {:rdf/about :vcard/Address}
          :vcard/geo "geo:47.39390,0.68710"
          :vcard/street-address "8, rue des déportés"
          :vcard/postal-code "37000"
          :vcard/locality "Tours"
          :vcard/region "Centre-Val de Loire"
          :vcard/country-name "France"}}}
       :org/memberDuring {:rdf/type {:rdf/about :time/Interval}
                          :time/hasBeginning #inst "2018-10-01T00:00:00Z"
                          :time/hasEnd #inst "2020-09-30T00:00:00Z"}}}}

   {:rdf/id "http://dbpedia.org/resource/Apache_OFBiz"
    :doap/vendor "Apache"
    :doap/name "OFBiz"
    :doap/homepage "https://ofbiz.apache.org/"
    :doap/programming-language #{"Java", "XML", "Groovy", "Freemarker"}
    :doap/developer [:rdf/about ::reuz/me]}
   {:rdf/id "http://dbpedia.org/resource/GNU_Automake"
    :doap/vendor "GNU"
    :doap/name "Automake"
    :doap/homepage "https://www.gnu.org/software/automake"
    :doap/programming-language "Perl"
    :doap/developer [:rdf/about ::reuz/me]}
   {:rdf/id "http://dbpedia.org/resource/GNU_Guix"
    :doap/vendor "GNU"
    :doap/name "Guix"
    :doap/homepage "https://www.gnu.org/software/guix"
    :doap/programming-language "Scheme"
    :doap/developer [:rdf/about ::reuz/me]}
   {:doap/vendor "GNU"
    :doap/name "JWhois"
    :doap/homepage "https://www.gnu.org/software/jwhois"
    :doap/programming-language "C"
    :doap/developer [:rdf/about ::reuz/me]}
   {:doap/vendor "GNU"
    :doap/name "Mcron"
    :doap/homepage "https://www.gnu.org/software/mcron"
    :doap/programming-language "Scheme"
    :doap/developer [:rdf/about ::reuz/me]}
   {:rdf/id "http://dbpedia.org/resource/GNU_Shepherd"
    :doap/vendor "GNU"
    :doap/name "Shepherd"
    :doap/homepage "https://www.gnu.org/software/shepherd"
    :doap/programming-language "Scheme"
    :doap/developer [:rdf/about ::reuz/me]}
   {:rdf/id "http://dbpedia.org/resource/GNU_Texinfo"
    :doap/vendor "GNU"
    :doap/name "Texinfo"
    :doap/homepage "https://www.gnu.org/software/texinfo"
    :doap/programming-language #{"Perl", "C"}
    :doap/developer [:rdf/about ::reuz/me]}])

(def db
  (-> (db/empty-db schema)
      (d/db-with facts)))
