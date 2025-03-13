(ns quanta.calendar.compress
  (:require
   [tick.core :as t]
   [tablecloth.api :as tc]
   [quanta.calendar.core :refer [current-close next-close]]
   [quanta.calendar.db.calendars :refer [get-calendar-timezone]]))

(defn other-cal-link-fast
  "returns a date column.
   date equals the next closing date of a calendar that is slower.
   so for example dt-col can be :m and calendar can be [:crypto :m5].
   can be used for compression or for series-linking in multi-timeframe
   algos."
  [dt-col calendar]
  (let [next-dt-a (atom (->> (dt-col 0)
                             (current-close calendar)))
        align-next (fn [next-dt row-dt]
                     (loop [next-dt (next-close calendar next-dt)]
                       ; when we align the next value, it might
                       ; be that the input ds misses bars, so we 
                       ; might have to jump several bars to the future
                       (if (t/> row-dt next-dt)
                         (recur (next-close calendar next-dt))
                         next-dt)))]
    (map
     (fn [row-dt]
       (let [next-dt @next-dt-a]
         (if (t/<= row-dt next-dt)
           next-dt
           (let [next-dt (align-next next-dt row-dt)]
             (println)
             (reset! next-dt-a next-dt)
             next-dt))))
     dt-col)))

(defn add-date-group-calendar [ds calendar]
  (let [[market-kw interval-kw] calendar
        calendar-tz (get-calendar-timezone market-kw)
        ds-cal (tc/map-columns ds :date #(t/in % calendar-tz))
        col-date (:date ds-cal)]
    (tc/add-column ds
                   :date-group (other-cal-link-fast col-date calendar))))

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
       compress-ds
       (tc/map-columns :date t/instant)))