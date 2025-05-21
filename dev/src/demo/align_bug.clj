(ns demo.align-bug
  (:require
   [tablecloth.api :as tc]))

(defn align-to-calendar
  "aligns ds-bars to a calendar.
   missing bars will have empty values."
  [calendar-ds bars-ds]
  (-> (tc/left-join calendar-ds bars-ds :date)
      (tc/order-by [:date] [:asc])
      (tc/set-dataset-name (-> bars-ds meta :name))))

(def calendar-daily
  (tc/dataset {:date ()}))