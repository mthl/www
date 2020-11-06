(ns fr.reuz.static
  (:require [clojure.java.io :as io]))

(defmacro defresource
  "Create a compiled resource file containing content which will be
  stored in the *compile-path*."
  [sym path & content]
  (let [path$ (eval path)
        full-path (str *compile-path* "/" path$)
        content-res (eval `(do ~@content))]
    (-> full-path io/file .getParentFile .mkdirs)
    (spit full-path content-res)
    `(def ~sym (io/resource ~path$))))
