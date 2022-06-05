(ns fr.reuz.main
  (:gen-class)
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [datascript.core :as d]
   [datascript.db :as db]
   [fr.reuz.core :as reuz]
   [fr.reuz.util :as util]
   [hiccup.page :as hp]
   [reitit.ring :as ring]
   [ring.adapter.jetty :as jetty]))

(defn find-articles
  [db]
  (d/q '[:find [(pull ?pubs [*]) ...]
         :where
         [?who :foaf/publications ?pubs]]
       db))

(defn find-article
  [db slug]
  (d/q '[:find (pull ?pub [*]) .
         :in $ ?slug
         :where
         [?who :foaf/publications ?pub]
         [?pub :http/slug ?slug]]
       db slug))

(defn db-id
  "Get the db/id corresponding to a nickname"
  [db nick]
  (d/q '[:find ?me .
         :in $ ?nick
         :where
         [?me :vcard/nickname ?nick]]
       db nick))

(defn blog
  [articles]
  (hp/html5
   {:lang "en"}
   [:h2 "Articles"]
   [:p
    [:ul
     (for [{:keys [http/slug dcterms/title dcterms/date]} articles]
       [:li [:a {:href (str "/blog/" slug)} title]
        " - " (util/render-date date)])]]))

(defn get-article
  [db req]
  (let [slug (get-in req [:path-params :slug])]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (:html (find-article db slug))}))

(defn router
  [db]
  (let [articles (sort-by :dcterms/date #(compare %2 %1) (find-articles db))]
    [["/"
      {:name ::index
       :get (constantly
             {:status 200
              :headers {"Content-Type" "text/html"}
              :body (reuz/index db)})}]
     ["/blog"
      {:name ::blog
       :get (constantly
             {:status 200
              :headers {"Content-Type" "text/html"}
              :body (blog articles)})}]

     ["/blog/:slug"
      {:name ::articles
       :get #(get-article db %)}]

     ["/data/index.ttl"
       {:name ::data
        :get (constantly
              {:status 200
               :headers {"Content-Type" "text/turtle"}
               :body (-> "data.ttl" io/resource slurp)})}]]))

(defn make-handler
  [db]
  (ring/ring-handler
   (ring/router (router db))
   (ring/routes (ring/redirect-trailing-slash-handler {:method :strip})
                (ring/create-resource-handler {:path "/" :root ""})
                (ring/create-default-handler))))

(defn- edn-resource [f]
  (-> f io/resource slurp edn/read-string))

(defn make-db []
  (let [schema (edn-resource "schema.edn")
        facts (edn-resource "facts.edn")]
    (-> (db/empty-db schema)
        (d/db-with facts))))

(def db* (make-db))

(defn start-server!
  []
  (let [port (Integer/parseInt (or (System/getenv "PORT") "8080"))]
    (jetty/run-jetty #((make-handler (make-db)) %) {:port port :join? false})))

(defn -main
  "Start the web server."
  [& _args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "8080"))
        app (make-handler (make-db))]
    (jetty/run-jetty app {:port port :join? false})))
