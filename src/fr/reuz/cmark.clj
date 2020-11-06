(ns fr.reuz.cmark
  (:gen-class)
  (:require
   [clojure.string :as str]
   [clojure.java.shell :as shell]
   [hiccup.core :as hiccup]
   [hiccup.util :as util]
   [clojure.java.io :as io]
   [clojure.walk :as walk]
   [clygments.core :as clygments])
  (:import
   org.commonmark.node.Node
   org.commonmark.parser.Parser
   org.commonmark.ext.gfm.tables.TablesExtension
   org.commonmark.ext.front.matter.YamlFrontMatterExtension))

(defprotocol Hiccup
  (read-hiccup [this]
    "Convert an object into an hiccup structure."))

(defn- children
  "Return a seq of the children of a commonmark-java AST node."
  [^Node node]
  (->> (.getFirstChild node)
       (iterate #(.getNext ^Node %))
       (take-while some?)))

(defn- content
  "Parse the content of of sub nodes."
  [n]
  (map read-hiccup (children n)))

(extend-protocol Hiccup
  org.commonmark.node.Document
  (read-hiccup [node]
    (content node))

  org.commonmark.node.Heading
  (read-hiccup [h]
    (let [hx (->> (.getLevel h) (str "h") keyword)]
      (into [hx] (content h))))

  org.commonmark.node.Paragraph
  (read-hiccup [p]
    (into [:p] (content p)))

  org.commonmark.node.BlockQuote
  (read-hiccup [bq]
    (into [:blockquote] (content bq)))

  org.commonmark.node.BulletList
  (read-hiccup [ul]
    (into [:ul] (content ul)))

  org.commonmark.node.OrderedList
  (read-hiccup [ol]
    (let [start (.getStartNumber ol)
          attrs (if start {:start start} {})]
      (into [:ol attrs] (content ol))))

  org.commonmark.node.ListItem
  (read-hiccup [li]
    (into [:li] (content li)))

  org.commonmark.node.Link
  (read-hiccup [l]
    (let [link (-> l .getDestination (str/replace-first #".md$" ""))]
      (into [:a {:href link}] (content l))))

  org.commonmark.node.FencedCodeBlock
  (read-hiccup [cb]
    (clygments/highlight (.getLiteral cb) (.getInfo cb) :html))

  org.commonmark.node.Code
  (read-hiccup [c]
    [:code (util/escape-html (.getLiteral c))])

  org.commonmark.node.Emphasis
  (read-hiccup [n]
    (into [:em] (content n)))

  org.commonmark.node.StrongEmphasis
  (read-hiccup [n]
    (into [:strong] (content n)))

  org.commonmark.node.Image
  (read-hiccup [i]
    [:img {:src (.getDestination i)
           :title (.getTitle i)
           :alt (first (content i))}])

  org.commonmark.node.Text
  (read-hiccup [node]
    (.getLiteral node))

  org.commonmark.node.SoftLineBreak
  (read-hiccup [_]
    " ")

  org.commonmark.node.ThematicBreak
  (read-hiccup [_]
    [:hr])

  org.commonmark.node.HardLineBreak
  (read-hiccup [_]
    [:br])

  org.commonmark.ext.front.matter.YamlFrontMatterBlock
  (read-hiccup [yb]
    (into {} (content yb)))

  org.commonmark.ext.front.matter.YamlFrontMatterNode
  (read-hiccup [yn]
    [(keyword (.getKey yn)) (.getValues yn)]))

(defn markdown->data
  "Takes a string of markdown and a renderer configuration and converts the string
  to a hiccup-compatible data structure."
  [^java.net.URL s]
  (let [ext [(TablesExtension/create) (YamlFrontMatterExtension/create)]
        parser (-> (Parser/builder) (.extensions ext) (.build))
        slug (-> s .getFile io/file .getName (str/replace-first #".md$" ""))
        tree (.parse parser (slurp s))
        [metadata & data :as all] (read-hiccup tree)]
    (if (map? metadata)
      (assoc metadata
             :content data
             :http/slug [slug])
      {:http/slug [slug]
       :content all})))
