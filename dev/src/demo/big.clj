(ns demo.big
  (:require
   [demo.util :refer [load-ds print-ds]]
   [quanta.calendar.ds.compress :refer [compress-to-calendar]]
   [tick.core :as t]
   [tablecloth.api :as tc]
   [quanta.calendar.window :as w]
   [quanta.calendar.ds.window :refer [window->ds join-aligned-bar-ds align-bars get-aligned-columns]]
   [ta.calendar.align :refer [align-to-calendar]]))

(def eurchf (load-ds "EURCHF"))
(def gbpjpy (load-ds "GBPJPY"))
(def usdjpy (load-ds "USDJPY"))

eurchf
gbpjpy
usdjpy

(-> eurchf
    (compress-to-calendar [:forex :m15]))

(-> eurchf
    (compress-to-calendar [:forex :m30]))

(-> eurchf
    (compress-to-calendar [:forex :h]))

(-> eurchf
    (compress-to-calendar [:forex :d]))

(def daily-ds  (compress-to-calendar eurchf [:forex :d]))

daily-ds

(print-ds daily-ds)

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

daily-ds

(align-to-calendar calendar-ds daily-ds)

(join-aligned-bar-ds calendar-ds daily-ds)

(def eurchf-d  (compress-to-calendar eurchf [:forex :d]))
(def gbpjpy-d  (compress-to-calendar gbpjpy [:forex :d]))
(def usdjpy-d  (compress-to-calendar usdjpy [:forex :d]))

(-> (tc/add-column calendar-ds
                   :asset
                   (join-aligned-bar-ds calendar-ds daily-ds))
    (print-ds))

(align-bars calendar-ds ["EURCHF" "GBPJPY" "USDJPY"]
            [eurchf-d gbpjpy-d usdjpy-d])

eurchf-d

(get-aligned-columns calendar-ds :close
                     ["EURCHF" "GBPJPY" "USDJPY"]
                     [eurchf-d gbpjpy-d usdjpy-d])

usdjpy-d

(def usdjpy-d-missing
  (-> usdjpy-d
      (tc/select-rows (fn [row]
                        (t/< (:date row) (t/instant "2025-05-12T21:00:00Z"))))))

usdjpy-d-missing

(join-aligned-bar-ds calendar-ds usdjpy-d)
(join-aligned-bar-ds calendar-ds usdjpy-d-missing)

calendar-ds

(align-bars calendar-ds ["EURCHF" "GBPJPY" "USDJPY"]
            [eurchf-d gbpjpy-d usdjpy-d-missing])

(get-aligned-columns calendar-ds :close
                     ["EURCHF" "GBPJPY" "USDJPY"]
                     [eurchf-d gbpjpy-d usdjpy-d-missing])

(-> (align-bars calendar-m-ds ["EURCHF" "GBPJPY" "USDJPY"]
                [eurchf-d gbpjpy-d usdjpy-d])
    (tc/select-rows (fn [row]
                      (= true (:data? row))))
    (tc/info))

(def calendar-m-ds
  (->> {; note the summer-time change here
        :start (t/instant "2025-03-05T00:02:00Z")
        :end (t/instant "2025-05-18T22:00:00Z")}
       (w/date-range->window [:forex :m])
       (window->ds)))

calendar-m-ds

(tc/select-rows)

(tc/row-count calendar-m-ds)
(tc/row-count m-ds)

(def aligned-ds
  (align-to-calendar2 calendar-m-ds m-ds))

(align-bars calendar-m-ds ["EURCHF" "GBPJPY" "USDJPY"]
            [eurchf gbpjpy usdjpy])

aligned-ds

(-> aligned-ds
    (tc/select-rows (fn [row]
                      (nil? (:close row))))
    ;(print-ds)
    )

(-> (tc/add-column calendar-m-ds
                   :asset
                   (join-aligned-bar-ds calendar-m-ds m-ds))
    (tc/select-rows (fn [row]
                      (nil? (:asset row))))
    ;(print-ds)
    )
(-> (align-bars calendar-m-ds ["EURCHF" "GBPJPY" "USDJPY"]
                [eurchf gbpjpy usdjpy])
    (tc/select-rows (fn [row]
                      (= true (:data? row))))
    ;(tc/select-rows (fn [row] (nil? (get row "GBPJPY"))))

    (tc/info))

(-> gbpjpy
    (tc/select-rows (fn [row]
                      (t/= (:date row)
                           (t/instant "2025-04-08T20:04:00Z")))))

eurchf
gbpjpy
usdjpy

(time
 (align-to-calendar calendar-m-ds m-ds))
; 560ms

(time
 (join-aligned-bar-ds calendar-m-ds m-ds))
; 27 ms

(-> (get-aligned-columns calendar-m-ds :close
                         ["EURCHF" "GBPJPY" "USDJPY"]
                         [eurchf gbpjpy usdjpy])

    ;(print-ds)
    )

(time
 (get-aligned-columns calendar-m-ds :close
                      ["EURCHF" "GBPJPY" "USDJPY"]
                      [eurchf gbpjpy usdjpy]))
