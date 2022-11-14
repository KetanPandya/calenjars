(ns com.piposaude.date-range-test
  (:require [clojure.test :refer :all]
            [com.piposaude.calenjars :refer [date-range]]
            [tick.alpha.api :as t]))

(deftest should-create-date-ranges-based-on-units-and-calendars
  (are [start-date end-date units calendars expected]
       (= expected (apply date-range start-date end-date units calendars))
    (t/date "2020-01-01") (t/date "2000-02-01") :business-days [] [(t/date "2020-01-01")]
    (t/date "2020-01-01") (t/date "2020-01-02") :business-days [] [(t/date "2020-01-01")(t/date "2020-01-02")]
    (t/date "2020-01-01") (t/date "2020-01-08") :business-days [] [(t/date "2020-01-01")(t/date "2020-01-02")(t/date "2020-01-03")(t/date "2020-01-06")(t/date "2020-01-07")(t/date "2020-01-08")]
    (t/date "2020-08-01") (t/date "2020-08-09") :business-days [] [(t/date "2020-08-03")(t/date "2020-08-04")(t/date "2020-08-05")(t/date "2020-08-06")(t/date "2020-08-07")]
    (t/date "2020-08-01") (t/date "2020-08-09") :days [] [(t/date "2020-08-01")(t/date "2020-08-02")(t/date "2020-08-03")(t/date "2020-08-04")(t/date "2020-08-05")(t/date "2020-08-06")(t/date "2020-08-07")(t/date "2020-08-08")(t/date "2020-08-09")]
    (t/date "2020-07-27") (t/date "2020-08-03") :business-days ["DAY-TWENTY-NINE"] [(t/date "2020-07-27")(t/date "2020-07-28")(t/date "2020-07-30")(t/date "2020-07-31")(t/date "2020-08-03")]))
