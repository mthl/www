(defproject reuz "0.1.0-SNAPSHOT"
  :description "Mathieu Lirzin Homepage"
  :url "https://reuz.fr/"
  :license {:name "Creative Commons Attribution Share-Alike 4.0 International"
            :url "https://creativecommons.org/licenses/by-sa/4.0/"}
  :resource-paths ["resources"]
  :dependencies
  [[metosin/reitit-ring "0.5.5"]
   [org.clojure/clojure "1.10.1"]
   [ring/ring "1.8.1"]
   [datascript/datascript "1.0.1"]
   [hiccup/hiccup "1.0.5"]]
  :global-vars {*warn-on-reflection* true
                *assert* false}
  :uberjar-name "reuz-standalone.jar"
  :main fr.reuz.main
  :profiles
  {:uberjar {:aot :all
             :omit-source true
             :jvm-opts ["-Dclojure.compiler.direct-linking=true"
                        "-Dclojure.compiler.elide-meta=[:doc :file :line :added]"]}
   :provided
   {:dependencies
    ;; Those dependencies are needed only at compile and dev time.
    [[com.atlassian.commonmark/commonmark "0.14.0"]
     [com.atlassian.commonmark/commonmark-ext-gfm-tables "0.14.0"]
     [com.atlassian.commonmark/commonmark-ext-yaml-front-matter "0.14.0"]
     [clygments/clygments "2.0.2"]]}}
  :repl-options {:init-ns fr.reuz.core})
