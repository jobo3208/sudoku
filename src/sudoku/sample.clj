(ns sudoku.sample
  "Sample puzzles")

(def easy-puzzle
  ^{:url "https://www.websudoku.com/?level=1&set_id=2856367253"}
  [6 nil 8 nil 1 5 nil 3 7
   nil 1 nil nil nil nil 2 nil 9
   2 7 nil nil 9 4 nil nil nil
   nil nil 6 3 nil nil nil nil 8
   1 8 nil nil nil nil nil 7 5
   7 nil nil nil nil 8 9 nil nil
   nil nil nil 4 8 nil nil 9 6
   9 nil 1 nil nil nil nil 2 nil
   8 4 nil 9 2 nil 5 nil 3])

(def medium-puzzle
  ^{:url "https://www.websudoku.com/?level=2&set_id=3054003092"}
  [nil nil 8 nil nil nil nil nil nil
   nil 3 nil nil nil 9 5 nil nil
   nil 4 9 5 nil 3 8 nil nil
   nil nil 7 1 9 nil nil nil 6
   nil 6 nil 3 7 4 nil 9 nil
   4 nil nil nil 2 6 1 nil nil
   nil nil 4 2 nil 8 6 5 nil
   nil nil 5 9 nil nil nil 8 nil
   nil nil nil nil nil nil 3 nil nil])

(def hard-puzzle
  ^{:url "https://www.websudoku.com/?level=3&set_id=9919475220"}
  [nil 2 nil nil 8 nil nil 1 nil
   nil 1 4 3 nil nil 2 nil nil
   nil nil nil nil 6 nil nil nil 9
   nil nil nil 9 nil nil nil 2 7
   nil 7 nil nil nil nil nil 6 nil
   6 3 nil nil nil 5 nil nil nil
   4 nil nil nil 5 nil nil nil nil
   nil nil 5 nil nil 9 8 4 nil
   nil 9 nil nil 2 nil nil 3 nil])

(def evil-puzzle
  ^{:url "https://www.websudoku.com/?level=4&set_id=3492588264"}
  [nil 9 nil 1 nil nil nil nil nil
   5 1 6 nil nil 7 nil nil nil
   nil 4 nil nil 5 nil nil nil nil
   nil nil 5 8 nil nil 6 nil nil
   nil nil 4 nil 2 nil 8 nil nil
   nil nil 2 nil nil 9 4 nil nil
   nil nil nil nil 1 nil nil 7 nil
   nil nil nil 3 nil nil 5 6 2
   nil nil nil nil nil 6 nil 3 nil])
