(ns com.piposaude.calenjars.file
  (:require
   [clojure.string :as str]
   [com.piposaude.calenjars.holidays :as gen])
  (:import
   (java.nio.file Paths)))


(def WEEKEND-FILE-NAME "WEEKEND")

(defn gen-path [filename output-path]
  (.toString (Paths/get output-path (into-array String [filename]))))

(defn gen-holiday-file-path [holiday-file output-path]
  (let [filename (.toString (.getFileName (Paths/get holiday-file (into-array String []))))
        output-filename (str/join "" (drop-last 4 filename))]
    (gen-path output-filename output-path)))

(defn generate! [holiday-file output-path year bracket-size]
  (let [holidays (gen/gen-bracketed-formated-holidays holiday-file year bracket-size)
        path (gen-holiday-file-path holiday-file output-path)]
    (spit path (str/join "\n" holidays))))

(defn generate-weekend! [output-path year bracket-size]
  (let [weekends (gen/gen-bracketed-formatted-weekends year bracket-size)
        weekend-path (gen-path WEEKEND-FILE-NAME output-path)]
    (spit weekend-path (str/join "\n" weekends))))
