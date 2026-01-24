(ns sudoku.core
  "Core data structures and functions for representing, manipulating, and
  solving sudoku puzzles"
  (:require [clojure.math.combinatorics :refer [combinations]]
            [clojure.set :refer [union difference]]
            [clojure.spec.alpha :as s]))

; specs as documentation
(s/def ::grid (s/coll-of any? :kind vector? :count 81))
(s/def ::cell (s/int-in 0 81))
(s/def ::group-type #{:row :col :box})
(s/def ::group-index (s/int-in 0 9))
(s/def ::group (s/tuple ::group-type ::group-index))
(s/def ::value #{1 2 3 4 5 6 7 8 9})
(s/def ::scope (s/coll-of ::value :kind set?))
(s/def ::scope-grid (s/coll-of ::scope :kind vector? :count 81))
(s/def ::puzzle (s/coll-of (s/nilable ::value) :kind vector? :count 81))
(s/def ::containment
  (s/and
    (s/tuple (s/coll-of ::value :kind set?) (s/coll-of ::cell :kind set?))
    (fn [[values cells]] (<= (count values) (count cells)))))

(def all-values #{1 2 3 4 5 6 7 8 9})

(def initial-scope all-values)

(def all-groups
  (for [t [:row :col :box]
        i (range 0 9)]
    [t i]))

(defn puzzle->scope-grid
  "Convert a puzzle into a scope grid"
  [puzzle]
  (mapv #(if % #{%} initial-scope) puzzle))

(defn scope-grid->puzzle
  "Convert a scope grid into a puzzle"
  [scope-grid]
  (mapv #(when (= 1 (count %)) (first %)) scope-grid))

(defn get-cells-in-group
  "Return a sequence of the cells in the given group"
  [[t i]]
  (case t
    :row (map #(+ % (* i 9)) (range 0 9))
    :col (map #(+ (* % 9) i) (range 0 9))
    :box (let [r (* (quot i 3) 3)
               c (* (mod i 3) 3)]
           (for [y (range r (+ r 3))
                 x (range c (+ c 3))]
             (+ (* y 9) x)))))

(defn get-cell-contents-in-group
  "Return a sequence of the contents of each cell in the given group"
  [grid [t i]]
  (map (partial nth grid) (get-cells-in-group [t i])))

(defn cell->coords
  "Convert 1D cell to 2D [y x] coordinates"
  [cell]
  [(quot cell 9) (mod cell 9)])

(defn get-group-of-cell
  "Return the group of type group-type that contains cell"
  [cell group-type]
  (let [[y x] (cell->coords cell)]
    (case group-type
      :row [:row y]
      :col [:col x]
      :box [:box (+ (* (quot y 3) 3) (quot x 3))])))

(defn get-groups-of-cell
  "Return a map of group-type to group, where each group contains cell"
  [cell]
  (into {} (map #(vector % (get-group-of-cell cell %)) [:row :col :box])))

(defn find-perfect-containments-in-group
  "Return a sequence of perfect containments of the given size within group.
  A perfect containment means that the number of values equals the number of cells."
  [scope-grid group size]
  (let [group-scopes (map vector (get-cells-in-group group)
                                 (get-cell-contents-in-group scope-grid group))
        eligible-scopes (filter #(<= (count (second %)) size) group-scopes)
        get-containment (fn [combo]
                          (let [cells (into #{} (map first combo))
                                scopes (map second combo)
                                u (apply union scopes)]
                            (when (= (count u) size)
                              [u cells])))
        containments (->> (combinations eligible-scopes size)
                          (map get-containment)
                          (filter some?))]
    containments))

(defn find-value-based-containments-in-group
  "Return a sequence of value-based containments within group.
  I.e., for each possible value, enumerate the cells it can go in."
  [scope-grid group]
  (let [group-scopes (map vector (get-cells-in-group group)
                                 (get-cell-contents-in-group scope-grid group))]
    (for [v all-values]
      [#{v} (->> group-scopes
                 (filter #(contains? (second %) v))
                 (map first)
                 (into #{}))])))

(defn narrow-group-scopes-by-containment
  "Use containment to narrow the appropriate scopes in group"
  [scope-grid group containment]
  (let [[con-vals con-cells] containment
        group-cells (get-cells-in-group group)]
    (reduce
      (fn [sg cell]
        (cond
          ; if cell is not in the containment, remove contained value(s) from it
          (not (contains? con-cells cell)) (update sg cell difference con-vals)
          ; special case: if value is contained to a single cell, narrow cell's scope to one
          (= con-cells #{cell}) (assoc sg cell con-vals)
          :else sg))
      scope-grid
      group-cells)))

(defn get-groups-of-containment
  "Return a sequence of groups that fully encompass the containment, and
  therefore are eligible to have their scopes narrowed by it"
  [containment]
  (let [[_ con-cells] containment
        relevant-groups (mapcat (comp vals get-groups-of-cell) con-cells)
        relevant-groups-by-type (-> (group-by first relevant-groups)
                                    (update-vals #(into #{} (map second %))))]
    (->> relevant-groups-by-type
         ; Keep only where number of groups of the type = 1, i.e. containment
         ; is completely within that group.
         (filter (comp (partial = 1) count val))
         (map #(vector (first %) (first (second %)))))))

(defn narrow-grid-scopes-by-containment
  "Narrow all eligible scopes in the grid by containment"
  [scope-grid containment]
  (reduce
    (fn [sg group]
      (narrow-group-scopes-by-containment sg group containment))
    scope-grid
    (get-groups-of-containment containment)))

(defn done?
  "Return true if all scopes have been narrowed to 1, false otherwise"
  [scope-grid]
  (every? #(= (count %) 1) scope-grid))

(defn group-solved?
  "Return true if group is completely filled in with valid values"
  [puzzle group]
  (let [values (get-cell-contents-in-group puzzle group)]
    (= all-values (set values))))

(defn solved?
  "Return true if puzzle is completely filled in with valid values"
  [puzzle]
  (every? #(group-solved? puzzle %) all-groups))

(defn solve
  "Narrow all scopes in the grid to 1 member by repeatedly finding containments
  and narrowing scopes based on them"
  [scope-grid]
  (loop [i 0
         sg scope-grid
         [con-size & con-sizes] (cycle [1 2 3 4])]
    (let [perfect-containments (mapcat #(find-perfect-containments-in-group sg % con-size) all-groups)
          vb-containments (mapcat #(find-value-based-containments-in-group sg %) all-groups)
          containments (concat perfect-containments vb-containments)
          sg (reduce
               narrow-grid-scopes-by-containment
               sg
               containments)]
      (cond
        (done? sg) sg
        (= i 100) #_ (throw (ex-info "took too many iterations" {})) sg
        :else (recur (inc i) sg con-sizes)))))

(defn solve-puzzle
  "Solve puzzle, returning a map of information about the solution process"
  [puzzle]
  (let [sg (puzzle->scope-grid puzzle)
        sg' (try
              (solve sg)
              (catch Exception _ sg))
        puzzle' (scope-grid->puzzle sg')]
    {:solution puzzle'
     :done? (done? sg')
     :solved? (solved? puzzle')}))
