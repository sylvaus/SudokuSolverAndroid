package com.pywhylab.sylvaus.sudoku_solver

private var SUDOKU_STR = """
=======================================
|| {} | {} | {} || {} | {} | {} || {} | {} | {} ||
---------------------------------------
|| {} | {} | {} || {} | {} | {} || {} | {} | {} ||
---------------------------------------
|| {} | {} | {} || {} | {} | {} || {} | {} | {} ||
=======================================
|| {} | {} | {} || {} | {} | {} || {} | {} | {} ||
---------------------------------------
|| {} | {} | {} || {} | {} | {} || {} | {} | {} ||
---------------------------------------
|| {} | {} | {} || {} | {} | {} || {} | {} | {} ||
=======================================
|| {} | {} | {} || {} | {} | {} || {} | {} | {} ||
---------------------------------------
|| {} | {} | {} || {} | {} | {} || {} | {} | {} ||
---------------------------------------
|| {} | {} | {} || {} | {} | {} || {} | {} | {} ||
=======================================
"""


class Cell(private var _square: CellGroup,
           private var _col: CellGroup,
           private var _row: CellGroup) {
    private var _value: Int? = null
    private var _possible_values: ArrayList<Int> = arrayListOf(1, 2, 3, 4, 5, 6, 7, 8, 9)
    private var _weight_values: IntArray = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
    private var _tried_values: ArrayList<Int> = ArrayList()
    private var _linked_cells: ArrayList<Cell> = ArrayList()

    override fun toString(): String {
        if (_value != null) {
            return _value.toString()
        } else {
            return " "
        }
    }

    fun compute_next_linked_cells() {
        var index = _row.cells.indexOf(this)
        _linked_cells = _row.cells.subList(index + 1, _row.cells.size) as ArrayList<Cell>

        index = _col.cells.indexOf(this)
        for (cell: Cell in _col.cells.subList(index + 1, _col.cells.size)) {
            if (!_linked_cells.contains(cell)) {
                _linked_cells.add(cell)
            }
        }

        index = _square.cells.indexOf(this)
        for (cell: Cell in _square.cells.subList(index + 1, _square.cells.size)) {
            if (!_linked_cells.contains(cell)) {
                _linked_cells.add(cell)
            }
        }
    }

    fun compute_all_linked_cells() {
        var index = _row.cells.indexOf(this)
        _linked_cells = _row.cells.clone() as ArrayList<Cell>

        for (cell: Cell in _col.cells) {
            if (!_linked_cells.contains(cell)) {
                _linked_cells.add(cell)
            }
        }

        for (cell: Cell in _square.cells) {
            if (!_linked_cells.contains(cell)) {
                _linked_cells.add(cell)
            }
        }
    }

    fun increase_weight(value: Int): Boolean {
        if (_value != null) {
            return true
        }

        _weight_values[value] += 1

        if (_weight_values[value] == 1) {
            _possible_values.remove(value)
        }

        if (_possible_values.size == 0) {
            return false
        }
        return true
    }

    fun decrease_weight(value: Int) {
        if (_value != null) {
            return
        }

        _weight_values[value] -= 1
        if (_weight_values[value] == 0) {
            _possible_values.add(value)
        }
    }

    fun set_next_possible(): Boolean {
        while (_possible_values.size > 0) {
            if (_value != null) {
                decrease_weight_others(_value!!)
            }

            _value = _possible_values.removeAt(0)
            _tried_values.add(_value!!)
            if (increase_weight_others(_value!!)) {
                return true
            }

        }
        return false
    }

    fun increase_weight_others(value: Int): Boolean {
        var result = true
        for (cell: Cell in _linked_cells) {
            if (cell.increase_weight(value)) {
                result = result and false
            }
        }

        return result
    }

    fun decrease_weight_others(value: Int) {
        for (cell: Cell in _linked_cells) {
            cell.decrease_weight(value)
        }

    }


    fun reset() {
        decrease_weight_others(_value!!)
        _possible_values.addAll(_tried_values)
        _tried_values = ArrayList()
        _value = null
    }

    fun set_value(value: Int) {
        _value = value
    }

}


class CellGroup {
    var cells = ArrayList<Cell>()

    fun add_cell(cell: Cell) {
        cells.add(cell)
    }
}


class SudokuSolver {
    private var _rows = ArrayList<CellGroup>()
    private var _cols = ArrayList<CellGroup>()
    private var _squares = ArrayList<CellGroup>()
    private var _cells = ArrayList<Cell>()
    private var _modifiable_cells = ArrayList<Cell>()

    constructor(values: ArrayList<Int?>) {
        assert(values.size == 81)
        for (i in 0..9) {
            _rows.add(CellGroup())
            _cols.add(CellGroup())
            _squares.add(CellGroup())
        }

        for (row_index in 0..9) {
            for (col_index in 0..9) {
                var row = _rows[row_index]
                var col = _cols[col_index]
                var square = _squares[col_index / 3 + (row_index / 3) * 3]
                _cells.add(Cell(row, col, square))
            }
        }

        for ((index, value) in values.withIndex()) {
            if (value == null) {
                _modifiable_cells.add(_cells[index])
                _cells[index].compute_next_linked_cells()
            } else {
                _cells[index].set_value(value)
                _cells[index].compute_all_linked_cells()
            }
        }

        for ((index, value) in values.withIndex()) {
            if (value != null) {
                _cells[index].increase_weight_others(value)
            }
        }
    }

    fun solve() {
        var index = 0
        var target = _modifiable_cells.size
        while (index < target) {
            var cell = _modifiable_cells[index]

            if (cell.set_next_possible()) {
                index += 1
                continue
            } else {
                cell.reset()
                index -= 1
            }
            if (index < 0) {
                break
            }
        }


        if (index == target) {
            print("Solution found")
        } else {
            print("No solution could be found")
        }

    }

    fun print_sudoku() {
        print(SUDOKU_STR.format(_cells))
    }

}