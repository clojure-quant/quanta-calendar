(ns ta.calendar.compress
  (:require
   [tick.core :as t]
   [tech.v3.datatype :as dtype]
   [tech.v3.datatype.functional :as dfn]
   [tablecloth.api :as tc]
   [ta.helper.date-ds :as h]
   [ta.calendar.core :refer [current-close2]]))

(def midnight (t/time "00:00:00"))

;; MONTH 

(defn- year-month->date [year month]
  (-> (t/new-date year month 1)
      (.plusMonths 1)
      (.plusDays -1)
      (t/at midnight)
      (t/in "UTC")))

(defn month-end-date [dt]
  (let [y (-> dt t/year .getValue)
        m (-> dt t/month .getValue)]
    (year-month->date y m)))

(defn add-date-group-month [ds]
  (let [date-group-col (dtype/emap month-end-date :zoned-date-time (:date ds))]
    (tc/add-column ds :date-group date-group-col)))

;; YEAR

(defn- year->date [year]
  (->
   (t/new-date year 1 1)
   (.plusYears 1)
   (.plusDays -1)
   (t/at midnight)
   (t/in "UTC")))

(defn year-end-date [dt]
  (let [y (-> dt t/year .getValue)]
    (year->date y)))

(defn add-date-group-year [ds]
  (let [date-group-col (dtype/emap year-end-date :zoned-date-time (:date ds))]
    (tc/add-column ds :date-group date-group-col)))

; HOUR

(defn hour-end-date [dt]
  (let [h (-> dt t/hour .getValue)
        m (-> dt t/minute .getValue)]
    (if (= 0 m)
      dt
      (-> dt
          (t/with :minute-of-hour 0)
          (t/with :hour-of-day (inc h))))))

(defn add-date-group-hour [ds]
  (let [date-group-col (dtype/emap year-end-date :zoned-date-time (:date ds))]
    (tc/add-column ds :date-group date-group-col)))

(defn add-date-group-calendar [ds calendar]
  (let [group-fn (partial current-close2 calendar)
        date-group-col (dtype/emap group-fn :zoned-date-time (:date ds))]
    (tc/add-column ds :date-group date-group-col)))

;; COMPRESS

(defn compress-ds [grouped-ds]
  (->
   grouped-ds
   (tc/group-by [:date-group])
   (tc/aggregate {:open (fn [ds]
                          (-> ds :open first))
                  :high (fn [ds]
                          (->> ds
                               :high
                               (apply max)))
                  :low (fn [ds]
                         (->> ds
                              :low
                              (apply min)))
                  :close (fn [ds]
                           (-> ds :close last))
                  :volume (fn [ds]
                            (->> ds
                                 :volume
                                 (apply +)))
                  :count (fn [ds]
                           (->> ds
                                :close
                                (count)))})
   (tc/rename-columns {:date-group :date})))

(defn compress-to-calendar [ds calendar]
  (->  ds
       (add-date-group-calendar calendar)
       compress-ds))

;; bad prior implementation from quanta.studio.

#_(defn add-date-group [ds interval]
    (case interval
      :month (compress/add-date-group-month ds)
      :year  (compress/add-date-group-year ds)
      :h (compress/add-date-group-hour ds)))

#_(-> ds
      (add-date-group interval)
      (compress/compress-ds))

(comment

  (year-month->date 2021 04)
  ;; => #time/zoned-date-time "2021-04-30T00:00Z[UTC]"

  (month-end-date (t/instant))
  ;; => #time/zoned-date-time "2024-07-31T00:00Z[UTC]"

  (month-end-date (t/instant "2023-01-01T15:30:00Z"))
  ;; => #time/zoned-date-time "2023-01-31T00:00Z[UTC]"

  
  ds

  (-> ds
      (add-date-group-year)
      (compress-ds))

  (-> ds
      (add-date-group-month)
      (compress-ds))


 ;;    |           
