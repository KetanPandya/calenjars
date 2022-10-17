(ns com.piposaude.calenjars.generate
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [com.piposaude.calenjars.file :as file]
   [tick.alpha.api :as t]
   [tick.format :as tf]))

(defn -main [bracket-size calendar-file-dir output-path]
  (let [today (t/today)
        current-year (edn/read-string (t/format (tf/formatter "yyyy") today))
        directory (io/file calendar-file-dir)
        [_ & files] (file-seq directory)]
    (println "-----------------------------------------------------------")
    (println (format "Generating holidays for holiday files under %s" calendar-file-dir))
    (when (empty? files) (println "No holiday files to process"))
    (run! #(file/generate! (str %) output-path current-year (edn/read-string bracket-size)) files)
    (file/generate-weekend! output-path current-year (edn/read-string bracket-size))
    (println "Holiday generation finished")
    (println "-----------------------------------------------------------")))
