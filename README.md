# sudoku

A naive sudoku solver. This code is an attempt to clarify, generalize,
and formalize my own intuitive approach to solving sudoku puzzles with
pencil and paper.

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

## Core concepts

At the heart of the data model is the *grid*, a vector of 81 elements
representing the squares (*cells*) of a sudoku puzzle in left-to-right,
top-to-bottom order (the 9 cells of the first row, then of the second
row, and so on). Grids and their cells can store different kinds of data
depending on what we’re doing. In a *puzzle*, each cell of the grid
contains either a single number (a *value*) or a blank (nil).

When solving a puzzle, we might be interested in the *scope* of each
cell, or the set of possible values it can contain based on what we’ve
deduced so far. A grid of scopes is called a *scope grid*.

*Groups* are the shapes within a grid that must contain the numbers 1-9
according to the rules of sudoku: rows, columns, and boxes. There are 9
of each, and they are referenced by vectors of type and index (again,
indexed from left to right): `[:row 3]`, `[:col 0]`, or `[:box 8]`.

Our solution algorithm centers around the idea of a *containment*, or an
assertion that a certain set of values is contained within a certain set
of cells within a group. Trivial examples of containments include the
fundamental rule of sudoku that each group must contain the numbers 1-9
(for e.g. row 0, `[#{1 2 3 4 5 6 7 8 9} #{0 1 2 3 4 5 6 7 8}]`) and the
constraints imposed by the numbers that were already entered into the
puzzle (e.g. `[#{4} #{26}]`). The number of values must be less than or
equal to the number of cells; if equal, the containment is called a
*perfect containment*.

The solution algorithm goes roughly like this:

1.  Find a small containment *C* within a group, *G*.
2.  Observe that *C* is also completely contained within another group, *H*.
3.  Use *C* to narrow scopes within group *H*.
4.  Repeat.
