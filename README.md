# sudoku

A naive sudoku solver. This code is an attempt to clarify, generalize,
and formalize my own intuitive approach to solving sudoku puzzles with
pencil and paper.

    $ clj -X:solve :puzzle '" 8 7    3   2       4 9  279   8 5  8  9 2  4  1 3   252  4 8       9   1    8 3 "'
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
    Steps:     74
    Duration:  0.785

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

## Containments

Our solution algorithm centers around the idea of a *containment*, or an
assertion that a certain set of values is contained within a certain set of
cells within a group. It is represented as a two-element vector containing a
set of values and a set of cells. Trivial examples of containments include the
fundamental rule of sudoku that each group must contain the numbers 1-9
(e.g. in row 0, `[#{1 2 3 4 5 6 7 8 9} #{0 1 2 3 4 5 6 7 8}]`) and the
constraints imposed by the numbers that were already entered into the puzzle
(e.g. `[#{4} #{26}]`). The number of values must be less than or equal to the
number of cells; if equal, the containment is called a *perfect containment*.
We work toward a solution by finding containments that are *significant*,
meaning that they allow us to narrow scopes in the grid.

Containments can be categorized in many ways: by how they are found, by how
many values and cells they contain, by whether and how they narrow scopes, etc.
Perhaps the simplest method for finding containments is the following:

  - within group *g*, loop through each possible value *v* (1 through 9)
  - for each *v*, find all cells *cs* in *g* whose scopes contain *v*
  - this produces 9 containments of the form `[#{v} (set cs)]`

Because we find these containments by looking at each value in turn, we call
these *value-based containments*. As you may suspect, this method is not
particularly good at finding significant containments. While most of the
containments found this way will not help us toward solving the puzzle, this
method is still essential (more on that later).

Another method, focused on perfect containments, is better at finding
significant containments. In this method, we look for perfect containments of a
particular size:

  - within group *g*, find all cells *cs* whose scopes are <= size *s*
  - for each *s*-sized combination *o* of the cells in *cs*, check if the union
    of their scopes *u* is also of size *s*
  - if so, produce the containment `[u (set o)]`

To see why this works, consider two cells *c* and *d* in a group who both have
a scope of `#{2 4}`, meaning their union is also `#{2 4}`. According to the
scope, the solution to *c* must be 2 or 4. If the solution to *c* turns out to
be 2, then *d* must be 4. If the solution to *c* turns out to be 4, then *d*
must be 2. In either case, 2 and 4 can only go in the cells *c* and *d*, and no
others.

This idea generalizes to sizes beyond 2, and not all cells need have the same
size scope: if *e* has a scope of `#{7 8}`, *f* has a scope of `#{8 9}`, and
*g* has a scope of `#{7 8 9}`, they would form a containment of size 3.

We call a containment like this an *outward* containment because it narrows
scopes outwardly: e.g., *e*, *f*, and *g*'s containment can remove 7, 8, and 9
from all group scopes *outside* of the containment.

*Inward* containments are the inverse of outward containments: they narrow
scopes *within* the containment. Consider cells *c* and *d* in group *g* with
scopes of `#{1 3 5}` and `#{3 5 7}`, respectively. If we look at all scopes in
*g* and find that *c* and *d* are the only ones that have 3 and 5 in them, then
they form a containment of `#{3 5}` that can be used to narrow their own scopes
(i.e. narrow "inwardly").

Inward containments are found in a similar but opposite way to outward
containments. If we describe the process of finding outward containments as:

    find n cells such that the union of their values is size n

then the process of finding inward containments can be described as:

    find n values such that the union of their cells is size n

It turns out that we don't actually need both outward and inward containments
to solve sudokus generally -- one or the other is fine -- but we do need
value-based containments. Value-based containments are useful because they can
help us find *cross-group* containments, or containments that are insignificant
within the group in which they are found, but significant in another group that
happens to contain the same cells.

Imagine that *c*, *d*, and *e* in the grid below have the following scopes:
`#{4 6}`, `#{4 7}`, and `#{4 9}`. Also assume that no other scope in the first
row has a 4.

    ┌───┬───┬───┰───┬───┬───┰───┬───┬───┐
    │   │   │   ┃ c │ d │   ┃   │   │   │
    ├───┼───┼───╂───┼───┼───╂───┼───┼───┤
    │   │   │   ┃   │   │   ┃   │   │   │
    ├───┼───┼───╂───┼───┼───╂───┼───┼───┤
    │   │   │   ┃   │   │ e ┃   │   │   │
    ┝━━━┿━━━┿━━━╋━━━┿━━━┿━━━╋━━━┿━━━┿━━━┥
    │   │   │   ┃   │   │   ┃   │   │   │
    ├───┼───┼───╂───┼───┼───╂───┼───┼───┤
    │   │   │   ┃   │   │   ┃   │   │   │
    ├───┼───┼───╂───┼───┼───╂───┼───┼───┤
    │   │   │   ┃   │   │   ┃   │   │   │
    ┝━━━┿━━━┿━━━╋━━━┿━━━┿━━━╋━━━┿━━━┿━━━┥
    │   │   │   ┃   │   │   ┃   │   │   │
    ├───┼───┼───╂───┼───┼───╂───┼───┼───┤
    │   │   │   ┃   │   │   ┃   │   │   │
    ├───┼───┼───╂───┼───┼───╂───┼───┼───┤
    │   │   │   ┃   │   │   ┃   │   │   │
    └───┴───┴───┸───┴───┴───┸───┴───┴───┘

Within the group `[:row 0]`, 4 is contained within c and d: `[#{4} #{3 4}]`.
This containment is not perfect, and is not an inward or outward containment.
Additionally, it is not a significant containment with respect to `[:row 0]`:
it can't be used to narrow scopes within that group. However, it *can* be used
to narrow scopes within `[:box 1]`, namely the scope of *e*. If we know *c* or
*d* has to be a 4, then no other cell in `[:box 1]` can be a 4, including *e*.

Note that any of the techniques mentioned above for finding containments will
find perfect containments of size 1, or *solved* containments. These
containments are fundamental to solving the puzzle because every sudoku puzzle
starts with some number of them -- they are where we begin. They are also a
natural first choice to test for significance as we progress through solving
the puzzle.

## Narrowing scopes

Given a scope grid and a containment, here's how we narrow scopes:

  - for each group *g* that fully encompasses the cells of the containment:
      - for each cell *c* in *g*, let *S* be *c*'s scope and let *V* be the
        values of the containment:
          - if *c* is outside the containment, *S* = *S* - *V*
          - if *c* is inside the containment, and the containment is perfect,
            *S* = *S* & *V*

This follows naturally from the definition of a containment: if some values are
contained within some cells, and *c* is outside those cells, it can't contain
those values. If *c* is inside those cells, and if there's only enough room in
those cells for the contained values, then it can't contain anything else.

## Solution algorithm

The solution algorithm boils down to finding a significant containment,
narrowing scopes based on that containment, and repeating until the puzzle
is solved. We try different techniques of finding containments in an order
that seems somewhat intuitive for a human:

 1. outward, size 1 (includes solved containments)
 2. inward, size 1
 3. outward, size 2
 4. outward, size 3
 5. value-based (for cross-group containments, arguably the trickiest)

The algorithm for finding the next significant containment is surely not the
most efficient, but it is stateless and simple.

## Testing

To run the solver against the test puzzles:

    $ clj -X:test
