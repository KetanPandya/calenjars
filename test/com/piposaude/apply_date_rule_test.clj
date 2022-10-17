(ns com.piposaude.apply-date-rule-test
  (:require [clojure.test :refer :all]
            [com.piposaude.calenjars :refer [apply-date-rules]]
            [tick.alpha.api :as t]))

(deftest should-identify-business-days-when-business-days?-with-date
  (are [date date-rule calendars expected]
       (= expected (apply apply-date-rules date date-rule calendars))
    (t/date "2020-08-01") "MF" [] (t/date "2020-08-03")
    (t/date "2020-08-03") "MF" [] (t/date "2020-08-03")
    (t/date "2020-02-29") "MF" [] (t/date "2020-02-28")
    (t/date "2020-01-31") "MF" [] (t/date "2020-01-31")
    (t/date "2020-02-29") "F" [] (t/date "2020-03-02")
    (t/date "2020-02-29") "P" [] (t/date "2020-02-28")
    (t/date "2020-08-02") "MP" [] (t/date "2020-08-03")
    (t/date "2020-08-02") "P" [] (t/date "2020-07-31")
    (t/date "2020-08-03") "MF"  ["DAY-THREE"] (t/date "2020-08-04")))
