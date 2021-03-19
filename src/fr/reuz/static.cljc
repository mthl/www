(ns fr.reuz.static
  "Compute static data at compile time.")

(defmacro defdata
  "Define a var which value is plain data computed at compile time"
  [sym & exps]
  (let [data (eval `(do ~@exps))]
    `(def ~sym '~data)))
