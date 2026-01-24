(ns sudoku.draw
  "Functions for rendering puzzles as strings")

(def ^:dynamic *draw-size* ::big)

(def big-format-string "┌───┬───┬───┰───┬───┬───┰───┬───┬───┐
│ %s │ %s │ %s ┃ %s │ %s │ %s ┃ %s │ %s │ %s │
├───┼───┼───╂───┼───┼───╂───┼───┼───┤
│ %s │ %s │ %s ┃ %s │ %s │ %s ┃ %s │ %s │ %s │
├───┼───┼───╂───┼───┼───╂───┼───┼───┤
│ %s │ %s │ %s ┃ %s │ %s │ %s ┃ %s │ %s │ %s │
┝━━━┿━━━┿━━━╋━━━┿━━━┿━━━╋━━━┿━━━┿━━━┥
│ %s │ %s │ %s ┃ %s │ %s │ %s ┃ %s │ %s │ %s │
├───┼───┼───╂───┼───┼───╂───┼───┼───┤
│ %s │ %s │ %s ┃ %s │ %s │ %s ┃ %s │ %s │ %s │
├───┼───┼───╂───┼───┼───╂───┼───┼───┤
│ %s │ %s │ %s ┃ %s │ %s │ %s ┃ %s │ %s │ %s │
┝━━━┿━━━┿━━━╋━━━┿━━━┿━━━╋━━━┿━━━┿━━━┥
│ %s │ %s │ %s ┃ %s │ %s │ %s ┃ %s │ %s │ %s │
├───┼───┼───╂───┼───┼───╂───┼───┼───┤
│ %s │ %s │ %s ┃ %s │ %s │ %s ┃ %s │ %s │ %s │
├───┼───┼───╂───┼───┼───╂───┼───┼───┤
│ %s │ %s │ %s ┃ %s │ %s │ %s ┃ %s │ %s │ %s │
└───┴───┴───┸───┴───┴───┸───┴───┴───┘")

(def small-format-string " %s %s %s │ %s %s %s │ %s %s %s 
 %s %s %s │ %s %s %s │ %s %s %s 
 %s %s %s │ %s %s %s │ %s %s %s 
───────┼───────┼───────
 %s %s %s │ %s %s %s │ %s %s %s 
 %s %s %s │ %s %s %s │ %s %s %s 
 %s %s %s │ %s %s %s │ %s %s %s 
───────┼───────┼───────
 %s %s %s │ %s %s %s │ %s %s %s 
 %s %s %s │ %s %s %s │ %s %s %s 
 %s %s %s │ %s %s %s │ %s %s %s ")

(def bare-format-string " %s %s %s │ %s %s %s │ %s %s %s 
 %s %s %s │ %s %s %s │ %s %s %s 
 %s %s %s │ %s %s %s │ %s %s %s 

 %s %s %s │ %s %s %s │ %s %s %s 
 %s %s %s │ %s %s %s │ %s %s %s 
 %s %s %s │ %s %s %s │ %s %s %s 

 %s %s %s │ %s %s %s │ %s %s %s 
 %s %s %s │ %s %s %s │ %s %s %s 
 %s %s %s │ %s %s %s │ %s %s %s ")

(def format-strings
  {::big big-format-string
   ::small small-format-string
   ::bare bare-format-string})

(defn- draw* [format-string grid]
  (apply format format-string (map #(or % \space) grid)))

(defn draw
  ([grid]
   (draw *draw-size* grid))
  ([size grid]
   (draw* (format-strings size) grid)))

(defn drawp [& args]
  (println (apply draw args)))
