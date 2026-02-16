(ns sudoku.core
  "Data structures and functions for working with sudoku puzzles"
  (:require [clojure.spec.alpha :as s]))

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
