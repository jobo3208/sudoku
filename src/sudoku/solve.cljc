(ns sudoku.solve
  "Data structures and functions for solving sudoku puzzles"
  (:require [clojure.math.combinatorics :refer [combinations]]
            [clojure.set :refer [difference intersection union]]
            [clojure.spec.alpha :as s]
            [sudoku.core :as core]))

(s/def ::containment
  (s/and
    (s/tuple (s/coll-of ::value :kind set?) (s/coll-of ::cell :kind set?))
    (fn [[values cells]] (<= (count values) (count cells)))))

(defn get-groups-of-containment
  "Return a sequence of groups that fully encompass the containment, and
  therefore are eligible to have their scopes narrowed by it"
  [containment]
  (let [[_ con-cells] containment
        relevant-groups (mapcat (comp vals core/get-groups-of-cell) con-cells)
        relevant-groups-by-type (-> (group-by first relevant-groups)
                                    (update-vals #(into #{} (map second %))))]
    (->> relevant-groups-by-type
         ; Keep only where number of groups of the type = 1, i.e. containment
         ; is completely within that group.
         (filter (comp (partial = 1) count val))
         (map #(vector (first %) (first (second %)))))))

(defn perfect?
  "Return true if containment is perfect."
  [containment]
  (let [[con-vals con-cells] containment]
    (= (count con-vals) (count con-cells))))

(defn significant?
  "Return true if containment narrows scopes in sg."
  [sg containment]
  (let [[con-vals con-cells] containment]
    (some
      (fn [cell]
        (let [scope (nth sg cell)]
          (if (con-cells cell)
            (and (perfect? containment) (some? (seq (difference scope con-vals))))
            (some? (seq (intersection scope con-vals))))))
      (mapcat core/get-cells-in-group (get-groups-of-containment containment)))))

(defn narrow-group-scopes-by-containment
  "Use containment to narrow the appropriate scopes in group"
  [scope-grid group containment]
  (let [[con-vals con-cells] containment
        group-cells (core/get-cells-in-group group)]
    (reduce
      (fn [sg cell]
        (cond
          ; if cell is not in the containment, remove contained value(s) from it
          (not (contains? con-cells cell)) (update sg cell difference con-vals)
          ; if cell is in the containment, and containment is perfect, narrow cell's scope to vals in con-vals
          (= (count con-vals) (count con-cells)) (update sg cell intersection con-vals)
          :else sg))
      scope-grid
      group-cells)))

(defn narrow-grid-scopes-by-containment
  "Narrow all eligible scopes in the grid by containment"
  [scope-grid containment]
  (reduce
    (fn [sg group]
      (narrow-group-scopes-by-containment sg group containment))
    scope-grid
    (get-groups-of-containment containment)))

(defn make-containment-finder
  "Return a function for finding containments. This function is meant to
  illustrate the isomorphic structure of the algorithms for finding outward
  and inward containments. Consider these descriptions of the algorithms:

                    (1)                           (2)
    outward: find n cells  s/t the union of their values is size n
    inward:  find n values s/t the union of their cells  is size n

  Immediately we can see they share a structure. If we call the items in the
  (1) column \"keys\" and the items in the (2) column \"values\", we can
  recreate either algorithm generically by accepting 3 parameters:

    ->ks [sg g]: return the keys to test
    k->vs [sg g k]: given a key, return the corresponding values
    ksu->c [ks u]: given keys and union of values, return a containment

  See following implementations."
  [->ks k->vs ksu->c]
  (fn -find-containment
    ([sg]
     (some (partial -find-containment sg) [1 2 3 4]))
    ([sg size]
     (some (partial -find-containment sg size) core/all-groups))
    ([sg size group]
     (let [eligible-ks (filter (fn [k]
                                 (let [v (k->vs sg group k)]
                                   (<= (min size 2) (count v) size)))
                               (->ks sg group))]
       (some
         (fn [ks]
           (let [ks (set ks)
                 vs (map #(set (k->vs sg group %)) ks)
                 u (apply union vs)]
             (when (= (count u) size)
               (let [containment (ksu->c ks u)]
                 (when (significant? sg containment)
                   containment)))))
         (combinations eligible-ks size))))))

(def find-outward-containment
  "Find a significant outward containment"
  (make-containment-finder
    (fn [_ group]
      (core/get-cells-in-group group))
    (fn [sg _ cell]
      (nth sg cell))
    (fn [cells values]
      [values cells])))

(def find-inward-containment
  "Find a significant inward containment"
  (make-containment-finder
    (constantly core/all-values)
    (fn [sg group value]
      (into #{}
        (keep (fn [cell]
                (let [scope (nth sg cell)]
                  (when (contains? scope value)
                    cell)))
              (core/get-cells-in-group group))))
    (fn [values cells]
      [values cells])))

(defn get-cells-with-value
  "Return a set of cells in group whose scopes contain value"
  [sg group value]
  (into #{}
    (keep (fn [cell]
            (let [scope (nth sg cell)]
              (when (contains? scope value)
                cell)))
          (core/get-cells-in-group group))))

(defn find-vb-containment
  "Find a significant value-based containment"
  ([sg]
   (some (partial find-vb-containment sg) [1 2 3 4]))
  ([sg size]
   (some (partial find-vb-containment sg size) core/all-groups))
  ([sg size group]
   (some
     (fn [vs]
       (let [cells (mapcat #(get-cells-with-value sg group %) vs)
             containment [(set vs) (set cells)]]
         (when (significant? sg containment)
           containment)))
     (combinations (vec core/all-values) size))))

(defn find-containment
  "Find a significant containment"
  ([sg]
   (some
    (fn [[find-fn algo size group]]
      (when-let [containment (find-fn sg size group)]
        (with-meta containment {:algo algo :group group})))
    (for [[find-fn algo size] [[find-outward-containment :outward 1]
                               [find-inward-containment :inward 1]
                               [find-outward-containment :outward 2]
                               [find-outward-containment :inward 3]
                               [find-vb-containment :value 1]]
          group core/all-groups]
      [find-fn algo size group]))))

(defn solve-with-containment-finder
  "Attempt to solve puzzle by repeatedly finding containments using find-fn and
  using them to narrow scopes. Return data about solution process."
  [find-fn puzzle]
  (loop [sg (core/puzzle->scope-grid puzzle)
         i 0]
    (if-let [containment (find-fn sg)]
      (recur (narrow-grid-scopes-by-containment sg containment) (inc i))
      (let [puzzle (core/scope-grid->puzzle sg)]
        {:steps i
         :sg sg
         :puzzle puzzle
         :solved (core/solved? puzzle)
         :done (core/done? sg)}))))
