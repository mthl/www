(ns fr.reuz.main
  (:gen-class)
  (:require
   [clojure.java.io :as io]
   [clojure.edn :as edn]
   [fr.reuz.core :as reuz]
   [fr.reuz.database :as db]
   [datascript.core :as d]
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

(defn index
  [articles]
  (hp/html5
   {:lang "en"}
   [:h2 "Articles"]
   [:p
    [:ul
     (for [{:keys [http/slug dcterms/title dcterms/date]} articles]
       [:li [:a {:href (str "/blog/" slug)} title]
        " - " (reuz/render-date date)])]]))

(defn blog-router
  [db]
  (let [articles (sort-by :dcterms/date #(compare %2 %1) (find-articles db))]
    [["/blog"
      {:name ::index
       :get (constantly
             {:status 200
              :headers {"Content-Type" "text/html"}
              :body (index articles)})}]

     ["/blog/:slug"
      {:name ::articles
       :get
       (fn [req]
         (let [slug (get-in req [:path-params :slug])]
           {:status 200
            :headers {"Content-Type" "text/html"}
            :body (:html (find-article db slug))}))}]]))

(defn router
  [db]
  (ring/router
   (concat
    (reuz/router db)
    (blog-router db)
    (db/router db))))

(defn make-handler
  [db]
  (ring/ring-handler
   (router db)
   (ring/routes
    (ring/redirect-trailing-slash-handler {:method :strip})
    (ring/create-resource-handler {:path "/" :root ""})
    (ring/create-default-handler))))

(defn start-server!
  []
  (let [port (Integer/parseInt (or (System/getenv "PORT") "8080"))
        db-ref #'db/db]
    (jetty/run-jetty #((make-handler @db-ref) %) {:port port :join? false})))

(defn -main
  "Start the web server."
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "8080"))
        app (make-handler db/db)]
    (jetty/run-jetty app {:port port :join? false})))
