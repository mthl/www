(ns fr.reuz.core
  (:require
   [datascript.core :as d]
   [hiccup.core :as h]
   [hiccup.page :as hp]
   [clojure.java.io :as io]
   [clojure.string :as str]))

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

(defn render-date
  [^java.util.Date d]
  (some-> d .toGMTString str))

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
  [db me]
  (let [gh (find-github-forge db 'fr.reuz/github-account)]
    [:p
     "I have been involved in the development of several free software projects."
     " The source code of those projects can be found on my "
     (apply xref gh) " account."]))

(defn blog-desc
  []
  [:p
   "I have a personal " (iref "/blog" "blog") " containing the weekly
reports I have made during my last "
   (xref "https://summerofcode.withgoogle.com/" "Google Summer of Code") "
experience as a student in 2017."])

(defn address-link
  [{:keys [rdf/id org/site-address]}]
  (let [{:vcard/keys [postal-code locality]} site-address
        city (str postal-code " " locality)
        {:vcard/keys [street-address region country-name]} site-address]
    (xref id (str/join ", " [street-address city region country-name]))))

(defn contact-info
  [workplace emails]
  (let [emails* (into [:dd] mailto emails)
        pgp (iref "/mthl.asc"
                  "F2A3 8D7E EB2B 6640 5761  070D 0ADE E100 9460 4D37")]
    [:dl
     [:dt "Oscaro postal address"] [:dd (address-link workplace)]
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
                (pull ?site [:rdf/id {:org/site-address [*]}])]
         :keys job-title occupation work homepage workplace
         :in $ ?who
         :where
         [?who :org/holds ?pos]
         [?pos :org/role ?role]
         [?role-id :rdfs/label ?job-title]
         [?role-id :rdfs/comment ?occupation]
         [?pos :org/post-in ?org]
         [?org :foaf/name ?work]
         [?org :foaf/homepage ?homepage]
         [?org :org/has-site ?site]]
       db who))

(defn index
  [db]
  (let [me [:rdf/about 'fr.reuz/me]
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
       (free-software-desc db me)
       (blog-desc)]
      [:footer
       [:h4 "Contact Information"]
       (contact-info (:workplace work-data) emails)]])))

(defn router
  [db]
  [["/"
    {:get (constantly
           {:status 200
            :headers {"Content-Type" "text/html"}
            :body (index db)})}]])
