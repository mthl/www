(ns fr.reuz.database
  (:require
   [fr.reuz.static :refer [defdata]]
   [fr.reuz.core :as reuz]))

(def schema
  {:foaf/member {:db/cardinality :db.cardinality/many
                 :db/valueType :db.type/ref}
   :foaf/developer {:db/cardinality :db.cardinality/many
                    :db/valueType :db.type/ref}
   :rdf/type {:db/cardinality :db.cardinality/many}
   :rdf/about {:db/unique :db.unique/identity}
   :vcard/email {:db/cardinality :db.cardinality/many
                 :db/valueType :db.type/ref}
   :foaf/account {:db/cardinality :db.cardinality/many
                  :db/valueType :db.type/ref}
   :org/role {:db/valueType :db.type/ref}
   :org/holds {:db/valueType :db.type/ref}
   :org/post-in {:db/valueType :db.type/ref}
   :org/has-site {:db/valueType :db.type/ref}
   :org/site-address {:db/valueType :db.type/ref}
   :http/slug {:db/unique :db.unique/identity}
   :foaf/publications {:db/cardinality :db.cardinality/many
                       :db/valueType :db.type/ref}
   :doap/developer {:db/cardinality :db.cardinality/many
                    :db/valueType :db.type/ref}})

(defdata facts
  (require '[fr.reuz.blog :as blog])
  [{:rdf/id "https://reuz.fr/#me"
    :rdf/about ::reuz/me
    :rdf/type #{:foaf/Person :vcard/Individual}
    :vcard/nickname "mthl"
    :foaf/birthday "1986-07-11"
    :vcard/fn "Mathieu Lirzin"
    :foaf/given-name "Mathieu"
    :foaf/family-name "Lirzin"
    :foaf/has-gender :vcard/Male
    :foaf/img "/images/mthl.png"
    :foaf/homepage "https://reuz.fr"
    :foaf/weblog "/blog"
    :vcard/address {:rdf/type :vcard/Address
                    :vcard/locality "Saint Cyr sur Loire"
                    :vcard/postal-code "37540"
                    :vcard/region "Centre-Val de Loire"
                    :vcard/country-name "France"}
    :vcard/email #{{:rdf/id "mailto:mthl@reuz.fr"
                    :rdfs/label "mthl@reuz.fr"
                    :rdf/type :vcard/Email}
                   {:rdf/id "mailto:mathieu.lirzin@oscaro.com"
                    :rdfs/label "mathieu.lirzin@oscaro.com"
                    :rdf/type :vcard/Email}
                   {:rdf/id "mailto:mthl@gnu.org"
                    :rdfs/label "mthl@gnu.org"
                    :rdf/type :vcard/Email}}
    :foaf/account [{:rdfs/label "Github"
                    :rdf/id "https://github.com/mthl"
                    :rdf/about ::reuz/github-account}
                   {:rdfs/label "Gitlab"
                    :rdf/id "https://gitlab.com/mthl"}
                   {:rdfs/label "Néréide Labs"
                    :rdf/id "https://labs.nereide.fr/mthl"}
                   {:rdfs/label "Savannah"
                    :rdf/id "https://savannah.gnu.org/users/mthl"}
                   {:rdfs/label "Notabug"
                    :rdf/id "https://notabug.org/mthl"}]
    :foaf/publications blog/posts
    :org/holds
    {:rdf/type :org/Post
     :org/role
     {:rdfs/label "software developer"
      :rdfs/comment
      "research and developpement on the automobile parts and
      accessories catalog"}
     :org/post-in {:rdf/type #{:org/Organization :foaf/Organization}
                   :foaf/name "Oscaro"
                   :rdfs/label "Oscaro"
                   :foaf/homepage "https://www.oscaro.com"
                   :org/has-site
                   {:rdf/type :org/Site
                    :rdf/id "https://www.openstreetmap.org/node/8987859088"
                    :org/site-address
                    {:rdf/type :vcard/Address
                     :vcard/geo "geo:48.9198151,2.2989956"
                     :vcard/street-address "34-40 rue Henri Barbusse"
                     :vcard/postal-code "92230"
                     :vcard/locality "Gennevilliers"
                     :vcard/region "Île de france"
                     :vcard/country-name "France"}}}}
    ;; {:rdf/type :org/Post
    ;;  :org/role
    ;;  {:rdfs/label "software engineer"
    ;;   :rdfs/comment
    ;;   "research and developpement on the Apache OFBiz ERP framework"}
    ;;  :org/post-in {:db/id 37
    ;;                :rdf/type #{:org/Organization :foaf/Organization}
    ;;                :foaf/name "Néréide"
    ;;                :rdfs/label "Néréide"
    ;;                :foaf/homepage "https://nereide.fr"
    ;;                :org/has-site
    ;;                {:rdf/type :org/Site
    ;;                 :rdf/id "https://www.openstreetmap.org/node/4999813121"
    ;;                 :org/site-address
    ;;                 {:rdf/type :vcard/Address
    ;;                  :vcard/geo "geo:47.39390,0.68710"
    ;;                  :vcard/street-address "8, rue des déportés"
    ;;                  :vcard/postal-code "37000"
    ;;                  :vcard/locality "Tours"
    ;;                  :vcard/region "Centre-Val de Loire"
    ;;                  :vcard/country-name "France"}}}}
    }

   {:doap/vendor "Apache"
    :doap/name "OFBiz"
    :doap/homepage "https://ofbiz.apache.org/"
    :doap/programming-language #{"Java", "XML", "Groovy", "Freemarker"}
    :doap/developer [:rdf/about ::reuz/me]}
   {:doap/vendor "GNU"
    :doap/name "Automake"
    :doap/homepage "https://www.gnu.org/software/automake"
    :doap/programming-language "Perl"
    :doap/developer [:rdf/about ::reuz/me]}
   {:doap/vendor "GNU"
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
   {:doap/vendor "GNU"
    :doap/name "Shepherd"
    :doap/homepage "https://www.gnu.org/software/shepherd"
    :doap/programming-language "Scheme"
    :doap/developer [:rdf/about ::reuz/me]}
   {:doap/vendor "GNU"
    :doap/name "Texinfo"
    :doap/homepage "https://www.gnu.org/software/texinfo"
    :doap/programming-language #{"Perl", "C"}
    :doap/developer [:rdf/about ::reuz/me]}])
