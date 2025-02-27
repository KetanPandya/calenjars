(ns com.piposaude.calenjars.types.ddmmm
  (:require [clojure.string :as str]
            [com.piposaude.calenjars.types.common :refer [holiday]])
  (:import (java.time.format DateTimeParseException)))

(defn get-holiday-ddmm [year name [day month rules]]
  (try
    (holiday name day month year false (or (rest rules) []))
    (catch DateTimeParseException e
      (if (str/includes? (.getMessage e) "not a leap year")
        nil
        (throw e)))))
