(ns fr.reuz.main
  (:gen-class)
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.tools.cli :as cli]
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

(defn start-server!
  []
  (let [port (Integer/parseInt (or (System/getenv "PORT") "8080"))]
    (jetty/run-jetty #((make-handler (make-db)) %) {:port port :join? false})))

(def cli-params
  [["-p" "--port PORT" "HTTP server port number"
    :default 8080
    :default-fn (fn [{:keys [port]}]
                  (or (some-> (System/getenv "PORT") parse-long)
                      port))
    :parse-fn parse-long
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-h" "--help"]])

(defn help
  "Return the string representation of --help output"
  [summary]
  (->> ["Usage: reuz [OPTION]..."
        "Run a webserver serving Mthl's homepage."
        (str \newline summary \newline)
        "Report bugs to: <https://github.com/mthl/www/issues>"
        "Project home page: <https://github.com/mthl/www>."]
       (str/join \newline)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn -main
  "Start the web server."
  [& args]
  (let [opts (cli/parse-opts args cli-params)
        {:keys [options errors summary]} opts]
    (cond
      (:help options) (exit 0 (help summary))
      (some? errors) (exit 1 (str/join \newline  errors))
      :else (let [app (make-handler (make-db))]
              (jetty/run-jetty app {:port (:port options)
                                    :join? false})))))
