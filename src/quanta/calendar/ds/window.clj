(ns quanta.calendar.ds.window
  (:require
   [tick.core :as t]
   [tech.v3.datatype :as dtype]
   [tech.v3.datatype.functional :as dfn]
   [tech.v3.tensor :as dtt]
   [tech.v3.dataset :as tds]
   [tablecloth.api :as tc]
   [quanta.calendar.window :as w]
   [quanta.calendar.ds.nil :as nil-fill]))

(defn window->ds
  "converts a calendar window to a dataset. 
   returns dataset with :date column, date being in ascending order (same sort order of bars)"
  [w]
  (let [{:keys [window]} (w/window->intervals w)
        close-asc (->> window
                       (map :close)
                       reverse
                       (into []))]
    (tds/new-dataset {:key-fn (fn [_id] :date)} [close-asc])))

(defn create-idx-links
  "returns a column with the size of calendar-ds.
   column is of type integer, and represents indices of the 
   bar-ds that match the :date column. If no date match is found, 
   nil is returned"
  [calendar-ds bar-ds]
  (let [idx (volatile! 0)
        idx-max (tc/row-count bar-ds)
        date-col (:date bar-ds)
        align (fn [dt]
                ;; advance bar index while bar date is strictly before calendar date
                (while (and (< @idx idx-max)
                            (t/< (get date-col @idx) dt))
                  (vswap! idx inc))
                (if (and (< @idx idx-max)
                         (t/= (get date-col @idx) dt))
                  (let [i @idx]
                    (vswap! idx inc)
                    i)
                  nil))]
    ; emap does not work when there are missing values, it cannot return nil.
    ;(dtype/clone (dtype/emap align :int64 (:date calendar-ds)))
    (->> (map align  (:date calendar-ds))
         (into []) ; into makes this calculation not lazy.
         )))

(def fill-nil-dict
  {:fill-forward nil-fill/fill-forward
   :fill-start-backward nil-fill/fill-start-backward
   :fill-forward-start-backward nil-fill/fill-forward-start-backward
   :none identity})

(defn add-asset-idx-col
  [calendar-ds asset-name bar-ds fill-nil-strategy]
  (let [fill-nil-fn (get fill-nil-dict fill-nil-strategy)
        idx-link-col (-> (create-idx-links calendar-ds bar-ds)
                         (fill-nil-fn))]
    (tc/add-column calendar-ds
                   asset-name
                   idx-link-col)))

(defn align-bars
  "returns a dataset with cols:
     :date from calendar-ds
     one column for each bar-ds with column-name asset-name
        the value of the column is the linked index.
     data? column that is true if all columns are linked"
  [calendar-ds asset-ds-map fill-nil-strategy]
  (let [process-asset (fn [ds [asset-name bar-ds]]
                        (add-asset-idx-col ds asset-name bar-ds fill-nil-strategy))]
    (reduce process-asset calendar-ds asset-ds-map)))

(defn aligned-ds-map
  "calendar-ds is a ds that defines :date column.
   asset-ds-map is a map with key asset and value a ds data (typically bars) with :date column
   fill-nil-strategy is a keyword: :fill-forward-start-backward, :fill-forward, :fill-start-backward, :none"

  [calendar-ds asset-ds-map fill-nil-strategy]
  (let [index-ds (align-bars calendar-ds asset-ds-map fill-nil-strategy)]
    (->> asset-ds-map
         (map (fn [[asset unaligned-ds]]
                [asset (-> (tc/select-rows unaligned-ds (get index-ds asset))
                           (tc/add-column :date (:date calendar-ds)))]))
         (into {}))))

(defn eager-and [x y]
  (-> (dfn/and x y)
      (dtype/clone)))

(defn int->bool [col]
  (dtype/clone (dtype/emap (fn [n]
                             (if n true false)) :bool col)))

(defn all-assets-have-data? [ds asset-name-seq]
  (let [asset-cols (map #(get ds %) asset-name-seq)
        asset-cols (map int->bool asset-cols) ; this is necessary because of tml bug
        ]
    (->> ;(reduce dfn/and (first asset-cols) (rest asset-cols))
     (reduce eager-and (first asset-cols) (rest asset-cols))
     (into []) ; makes the result not lazy
     )))

(defn drop-rows-with-missing-data
  "input: - ds with :date and all other columsn are :asset column names
   removes rows where not all asset columsn have a value. "
  [ds]
  (let [col-seq (->> ds
                     tc/column-names
                     (remove #(= :date %)))]
    (-> ds
        (tc/add-column :data? (all-assets-have-data? ds col-seq))
        (tc/select-rows (fn [row] (= true (:data? row))))
        (tc/drop-columns [:data?]))))

(defn multi-asset-col [asset-ds-map col]
  (let [assets (keys asset-ds-map)
        asset-0 (first assets)
        ds (tc/select-columns (get asset-ds-map asset-0) [:date])]
    (reduce (fn [ds asset]
              (tc/add-column ds asset (-> (get asset-ds-map asset)
                                          (get col)))) ds assets)))
