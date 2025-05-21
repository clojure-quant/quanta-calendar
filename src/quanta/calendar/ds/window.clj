(ns quanta.calendar.ds.window
  (:require
   [tick.core :as t]
   [tech.v3.datatype :as dtype]
   [tech.v3.datatype.functional :as dfn]
   [tech.v3.tensor :as dtt]
   [tech.v3.dataset :as tds]
   [tablecloth.api :as tc]
   [quanta.calendar.window :as w]))

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

(def max-dt
  (t/max-of-type (t/instant)))

(defn join-aligned-bar-ds
  "returns a column with the size of calendar-ds.
   column is of type integer, and represents indices of the 
   bar-ds that match the :date column. If no date match is found, 
   nil is returned"
  [calendar-ds bar-ds]
  (let [idx (volatile! 0)
        idx-max (tc/row-count bar-ds)
        date-col (:date bar-ds)
        bar-dt (fn []
                 ; check for idx is important, because (bar-dt) gets evaluated
                 ; even if the idx is at idx-max.
                 (let [i @idx]
                   (if (= i idx-max)
                     max-dt
                     (get date-col @idx))))
        align (fn [dt]
                ; skip past
                (while (and (not (= idx idx-max))
                            (t/< (bar-dt) dt))
                  (vswap! idx inc))
                (if (and (not (= idx idx-max))
                         (t/= (bar-dt) dt))
                  ; date match - return index
                  ; avoid skip past, as by default we expect to have aligned bard.
                  (let [i @idx] ; idx might be increased before
                    (when (not (= idx idx-max))
                      (vswap! idx inc))
                    i ; return index
                    )
                  ; no date match - we must be in the future, so no match
                  nil))]
    ; emap does not work when there are missing values, it cannot return nil.
    ;(dtype/clone (dtype/emap align :int64 (:date calendar-ds)))
    (->> (map align  (:date calendar-ds))
         (into []) ; into makes this calculation not lazy.
         )))

(defn align-asset-idx [calendar-ds asset-name bar-ds]
  (tc/add-column calendar-ds
                 asset-name
                 (join-aligned-bar-ds calendar-ds bar-ds)))

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

(defn align-bars
  "returns a dataset with cols:
     :date from calendar-ds
     one column for each bar-ds with column-name asset-name
        the value of the column is the linked index.
     data? column that is true if all columns are linked"
  [calendar-ds asset-name-seq bar-ds-seq]
  (let [process-asset (fn [ds [asset-name bar-ds]]
                        (align-asset-idx ds asset-name bar-ds))
        ds (reduce process-asset calendar-ds (map vector asset-name-seq bar-ds-seq))]
    (tc/add-column ds :data? (all-assets-have-data? ds asset-name-seq))))

(defn get-aligned-columns
  "input: - calendar-ds (a dataset that only contains :date column)
          - asset-name-seq (a seq of asset names (strings))
          - bar-ds-seq (a seq of datasets, one for  each asset, must contain :date and bar-ds-col columns)
  returns a dataset with 
     :date from calendar-ds, but with possibly less rows, so that only rows from :asset are 
     taken that have a matching :date etnry
     one column for each asset with name asset, and value from columns bar-ds-col"
  [calendar-ds bar-ds-col asset-name-seq bar-ds-seq]
  (let [ds (-> (align-bars calendar-ds asset-name-seq bar-ds-seq)
               (tc/select-rows (fn [row] (= true (:data? row)))))
        _ (println "data-count: " (tc/row-count ds))
        asset-dict (->> (map (fn [asset bar-ds]
                               [asset bar-ds]) asset-name-seq bar-ds-seq)
                        (into {}))
        get-data (fn [asset]
                   (let [idx-col (get ds asset)
                         bar-ds (get asset-dict asset)
                         value-col (get bar-ds bar-ds-col)]
                     (dtt/select value-col idx-col)))
        add-data-asset (fn [ds asset]
                         (tc/add-column ds asset (get-data asset)))]
    (-> (reduce add-data-asset ds asset-name-seq)
        (tc/drop-columns [:data?]))))
