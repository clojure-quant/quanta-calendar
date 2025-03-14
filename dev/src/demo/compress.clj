(ns demo.compress
  (:require
   [tick.core :as t]
   [tablecloth.api :as tc]
   [quanta.calendar.ds.compress :refer [compress-to-calendar]]))

(def ds-intraday
  (tc/dataset [{:date (t/instant "2024-07-04T14:25:00Z")
                :open 100
                :high 110
                :low 90
                :close 105
                :volume 100}
               {:date (t/instant "2024-07-04T14:46:00Z")
                :open 106
                :high 115
                :low 101
                :close 109
                :volume 100}
               {:date (t/instant "2024-07-04T14:59:00Z")
                :open 110
                :high 121
                :low 105
                :close 116
                :volume 100}]))

(-> ds-intraday
    (compress-to-calendar [:crypto :m15]))

(-> ds-intraday
    (compress-to-calendar [:crypto :m30]))

(-> ds-intraday
    (compress-to-calendar [:crypto :h]))

(-> ds-intraday
    (compress-to-calendar [:crypto :d]))

(def ds (tc/dataset [{:date (t/instant "2021-01-01T15:30:00Z")
                      :open 100
                      :high 110
                      :low 90
                      :close 105
                      :volume 100}
                     {:date (t/instant "2021-01-02T15:30:00Z")
                      :open 106
                      :high 115
                      :low 101
                      :close 109
                      :volume 100}
                     {:date (t/instant "2021-02-01T15:30:00Z")
                      :open 110
                      :high 121
                      :low 105
                      :close 116
                      :volume 100}]))

(compress-to-calendar ds [:crypto :d])
(compress-to-calendar ds [:crypto :h])
(compress-to-calendar ds [:crypto :m30])


