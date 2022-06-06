(ns fr.reuz.util)

(defn render-date
  [^java.util.Date d]
  (some-> d .toGMTString str))
