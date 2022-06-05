(ns fr.reuz.core
  {:vann/preferredNamespacePrefix "reuz"
   :vann/preferredNamespaceUri "https://reuz.fr/#"}
  (:require
   [clojure.string :as str]
   [datascript.core :as d]
   [hiccup.page :as hp]))

(defn stylesheet
  [file]
  [:link {:rel "stylesheet" :href file}])

(defn anchor
  [content uri]
  [:a {:href uri} content])

(def cc
  [:a {:href "https://creativecommons.org/licenses/by-sa/4.0/"}
   "Creative Commons Attribution Share-Alike 4.0 International"])

(defn xref
  ([href]
   (xref href [:code href]))
  ([href desc]
   [:a {:href href :target "_blank" :rel "noopener"} desc]))

(defn iref
  ([href]
   (iref href [:code href]))
  ([href desc]
   [:a {:href href} desc]))

(def mailto
  "'mailto:' links list transducer"
  (comp
   (map (fn [[label href]] (iref href label)))
   (interpose ", ")))

(def desc-ref
  "describe link data transducer"
  (comp
   (map (fn [[name about]] (xref about name)))
   (interpose ", ")))

(defn presentation
  "Introduce myself by describing my job and general interest."
  [{:keys [job-title occupation work homepage]}]
  [:p "I am a " job-title " at " (xref homepage work) " doing "
occupation ". My topics of interest are Programming Languages, Network
Based Software Architecture, Software packaging and the World Wide
Web."])

(defn find-github-forge
  [db id]
  (d/q '[:find [?url ?label]
         :in $ ?id
         :where
         [?account :rdf/about ?id]
         [?account :rdf/id ?url]
         [?account :rdfs/label ?label]]
       db id))

(defn free-software-desc
  [db]
  (let [[url label] (find-github-forge db ::github-account)]
    [:p
     "I have been involved in the development of several free software projects."
     " The source code of those projects can be found on my "
     (xref url label) " account."]))

(defn blog-desc
  []
  [:p
   "I have a personal " (iref "/blog" "blog") " containing the weekly
reports I have made during my last "
   (xref "https://summerofcode.withgoogle.com/" "Google Summer of Code") "
experience as a student in 2017."])

(defn address-link
  [{:keys [rdf/id org/siteAddress]}]
  (let [{:vcard/keys [postal-code locality]} siteAddress
        city (str postal-code " " locality)
        {:vcard/keys [street-address region country-name]} siteAddress]
    (xref id (str/join ", " [street-address city region country-name]))))

(defn contact-info
  [work workplace emails]
  (let [emails* (into [:dd] mailto emails)
        pgp (iref "/mthl.asc"
                  "F2A3 8D7E EB2B 6640 5761  070D 0ADE E100 9460 4D37")]
    [:dl
     [:dt work " postal address"] [:dd (address-link workplace)]
     [:dt "Email"] emails*
     [:dt "PGP"] [:dd pgp]]))

(defn full-name
  [db who]
  (d/q '[:find ?fn .
         :in $ ?who
         :where [?who :vcard/fn ?fn]]
       db who))

(defn find-emails
  [db who]
  (d/q '[:find ?label ?link
         :in $ ?who
         :where
         [?who :vcard/email ?email]
         [?email :rdfs/label ?label]
         [?email :rdf/id ?link]]
       db who))

(defn job-info
  [db who]
  (d/q '[:find [?job-title ?occupation ?work ?homepage
                (pull ?site [:rdf/id {:org/siteAddress [*]}])]
         :keys job-title occupation work homepage workplace
         :in $ ?who
         :where
         [?who :org/hasMembership ?mem]
         [?mem :org/role ?role]
         [?role :rdfs/label ?job-title]
         [?role :rdfs/comment ?occupation]
         [?mem :org/organization ?org]
         [?org :foaf/name ?work]
         [?org :foaf/homepage ?homepage]
         [?org :org/hasSite ?site]
         [?mem :org/memberDuring ?period]
         (not [?period :time/hasEnd _])]
       db who))

(defn index
  [db]
  (let [me [:rdf/about ::me]
        name (full-name db me)
        emails (find-emails db me)
        work-data (job-info db me)]
    (hp/html5
     {:lang "en"}
     [:head
      [:meta {:charset "utf-8"}]
      [:title name]
      [:link {:rel "stylesheet" :href "style.css"}]]
     [:body
      [:header
       [:h1 name]]
      [:main
       [:img {:src "/images/mthl.png" :alt "portrait"}]
       (presentation work-data)
       (free-software-desc db)
       (blog-desc)]
      [:footer
       [:h4 "Contact Information"]
       (contact-info (:work work-data)
                     (:workplace work-data)
                     emails)]])))
