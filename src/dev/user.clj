(ns user
  (:require
   [clojure.tools.namespace.repl :refer [refresh]]))

(defonce state nil)

(defn halt []
  (alter-var-root #'state (fn [state]
                            (when state
                              (.stop state))))
  :halted)

(defn go []
  (halt)
  (let [start (requiring-resolve 'fr.reuz.main/start-server!)]
    (alter-var-root #'state (fn [_] (start))))
  :initiated)

(defn reset []
  (halt)
  (refresh :after 'user/go)
  :resumed)
