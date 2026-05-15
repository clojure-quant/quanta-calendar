(ns quanta.notebook.calendar.compress-align-big
  (:require
   [tick.core :as t]
   [tablecloth.api :as tc]
   [quanta.calendar.window :as w]
   [quanta.calendar.ds.compress :refer [compress-to-calendar]]
   [quanta.calendar.ds.window :refer [window->ds create-idx-links align-bars
                                      aligned-ds-map multi-asset-col
                                      drop-rows-with-missing-data]]
   [quanta.notebook.demo-datas.util :refer [load-ds print-ds]]))

(def eurchf (load-ds "EURCHF"))
(def gbpjpy (load-ds "GBPJPY"))
(def usdjpy (load-ds "USDJPY"))

eurchf
gbpjpy
usdjpy

;; compress to different frequencies
(-> eurchf (compress-to-calendar [:forex :m15]))
(-> eurchf (compress-to-calendar [:forex :m30]))
(-> eurchf (compress-to-calendar [:forex :h]))
(-> eurchf (compress-to-calendar [:forex :d]))

;; compress to daily. we will work with that.
(def eurchf-d  (compress-to-calendar eurchf [:forex :d]))
(def gbpjpy-d  (compress-to-calendar gbpjpy [:forex :d]))
(def usdjpy-d  (compress-to-calendar usdjpy [:forex :d]))

(print-ds eurchf-d)

(->> {; note the summer-time change here
      :start (t/instant "2025-03-01T21:30:00Z")
      :end (t/instant "2025-03-10T20:30:00Z")}
     (w/date-range->window [:forex :d])
     (window->ds))

(def calendar-ds
  (->> {; note the summer-time change here
        :start (t/instant "2025-03-05T22:00:00Z")
        :end (t/instant "2025-05-19T21:00:00Z")}
       (w/date-range->window [:forex :d])
       (window->ds)))
calendar-ds

;; new syntax
(create-idx-links calendar-ds eurchf-d)

(align-bars calendar-ds {"EURCHF" eurchf-d
                         "GBPJPY" gbpjpy-d
                         "USDJPY" usdjpy-d}
            :fill-forward-start-backward)

;|                :date | EURCHF | GBPJPY | USDJPY |
;|----------------------|-------:|-------:|-------:|
;| 2025-03-05T22:00:00Z |      0 |      0 |      0 |
;| 2025-03-06T22:00:00Z |      1 |      1 |      1 |
;| 2025-03-07T22:00:00Z |      2 |      2 |      2 |
;| 2025-03-10T21:00:00Z |      3 |      3 |      3 |

(-> (align-bars calendar-ds {"EURCHF" eurchf-d
                             "GBPJPY" gbpjpy-d
                             "USDJPY" usdjpy-d}
                :fill-forward-start-backward)
    (drop-rows-with-missing-data))

(aligned-ds-map
 calendar-ds {"EURCHF" eurchf-d
              "GBPJPY" gbpjpy-d
              "USDJPY" usdjpy-d}
 :fill-forward-start-backward)

(-> (aligned-ds-map
     calendar-ds {"EURCHF" eurchf-d
                  "GBPJPY" gbpjpy-d
                  "USDJPY" usdjpy-d}
     :fill-forward-start-backward)
    (multi-asset-col :close))

;; test to see that missing data is handled correctly

usdjpy-d

(def usdjpy-d-missing
  (-> usdjpy-d
      (tc/select-rows (fn [row]
                        (t/< (:date row) (t/instant "2025-05-12T21:00:00Z"))))))

usdjpy-d-missing

(create-idx-links calendar-ds usdjpy-d)
(create-idx-links calendar-ds usdjpy-d-missing)

calendar-ds

(-> (aligned-ds-map
     calendar-ds {"EURCHF" eurchf-d
                  "GBPJPY" gbpjpy-d
                  "USDJPY" usdjpy-d-missing}
     :fill-forward-start-backward)
    (multi-asset-col :close))

;; test to see that big datasets are handled correctly

(def calendar-m-ds
  (->> {; note the summer-time change here
        :start (t/instant "2025-03-05T00:02:00Z")
        :end (t/instant "2025-05-18T22:00:00Z")}
       (w/date-range->window [:forex :m])
       (window->ds)))

calendar-m-ds

(def min-close
  (-> (aligned-ds-map
       calendar-m-ds {"EURCHF" eurchf
                      "GBPJPY" gbpjpy
                      "USDJPY" usdjpy}
       :fill-forward-start-backward)
      (multi-asset-col :close)))

(tc/info min-close)
(tc/row-count min-close)
