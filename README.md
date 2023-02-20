# sudoku

a naive sudoku solver. this code is an attempt to clarify, generalize, and
formalize my own intuitive approach to solving sudoku puzzles with pencil and
paper.

    $ clj -M:solve " 8 7    3   2       4 9  279   8 5  8  9 2  4  1 3   252  4 8       9   1    8 3 "
    ┌───┬───┬───┰───┬───┬───┰───┬───┬───┐
    │ 2 │ 8 │ 5 ┃ 7 │ 6 │ 4 ┃ 1 │ 9 │ 3 │
    ├───┼───┼───╂───┼───┼───╂───┼───┼───┤
    │ 6 │ 9 │ 7 ┃ 2 │ 1 │ 3 ┃ 4 │ 5 │ 8 │
    ├───┼───┼───╂───┼───┼───╂───┼───┼───┤
    │ 3 │ 1 │ 4 ┃ 8 │ 9 │ 5 ┃ 6 │ 2 │ 7 │
    ┝━━━┿━━━┿━━━╋━━━┿━━━┿━━━╋━━━┿━━━┿━━━┥
    │ 9 │ 3 │ 2 ┃ 4 │ 8 │ 1 ┃ 5 │ 7 │ 6 │
    ├───┼───┼───╂───┼───┼───╂───┼───┼───┤
    │ 8 │ 5 │ 6 ┃ 9 │ 7 │ 2 ┃ 3 │ 1 │ 4 │
    ├───┼───┼───╂───┼───┼───╂───┼───┼───┤
    │ 4 │ 7 │ 1 ┃ 5 │ 3 │ 6 ┃ 9 │ 8 │ 2 │
    ┝━━━┿━━━┿━━━╋━━━┿━━━┿━━━╋━━━┿━━━┿━━━┥
    │ 5 │ 2 │ 3 ┃ 1 │ 4 │ 7 ┃ 8 │ 6 │ 9 │
    ├───┼───┼───╂───┼───┼───╂───┼───┼───┤
    │ 7 │ 6 │ 8 ┃ 3 │ 5 │ 9 ┃ 2 │ 4 │ 1 │
    ├───┼───┼───╂───┼───┼───╂───┼───┼───┤
    │ 1 │ 4 │ 9 ┃ 6 │ 2 │ 8 ┃ 7 │ 3 │ 5 │
    └───┴───┴───┸───┴───┴───┸───┴───┴───┘
    Done:      true
    Solved:    true
    Duration:  1.405

there is also a scraper to test the solver against random puzzles from the web,
but i don't recommend running it since it violates the website's TOS.
