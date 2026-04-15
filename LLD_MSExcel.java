import java.util.*;

public class LLD_MSExcel {
    public static void main(String[] args) {
        // initialize and operate with the excel APIs
        // implemented APIs: updateContentInCell(), deleteCell(), updateFormula(), evaluateFormula()
    }
}

class WorkBook {
    List<WorkSheet> workSheets;
    int n;

    public WorkBook(int n) {
        this.n = n;
        workSheets = new ArrayList<>(n);
    }

    public void addWorkSheet() {
        workSheets.add(new WorkSheet());
    }
}

class WorkSheet {
    HashMap<String, Cell> cells;

    WorkSheet() {
        cells = new HashMap<>();
    }

    public void deleteCell(String cellId) {
        if(cells.containsKey(cellId)) {
            this.updateContentInCell(cellId, 0.0);
            Cell cell = cells.get(cellId);
            for(Cell parent:cell.parents) {
                if(parent.dependantCells.contains(cell)) {
                    parent.dependantCells.remove(cell);
                }
            }
            cells.remove(cellId);
        }
    }

    public boolean updateContentInCell(String id, double content) {
        if(!cells.containsKey(id)) {
            cells.put(id, new Cell(id));
        }
        Cell cellToBeUpdated = cells.get(id);

        // if(!cellToBeUpdated.formula.equals("")) {
        //     return false;
        //     // i can't update a cell directly, if it contains a formula
        // }

        cellToBeUpdated.updateContent(content);

        // update dependants
        updateDependantCells(cellToBeUpdated);

        return true;
    }

    private void updateDependantCells(Cell cell) {
        // whenever any parent cell is updated -- i need to evelaue the formula used to derive another cell everytime
        // update all the dependent cells recursively
        evaluateFormula(cell.formula, cell);
        for(Cell next:cell.dependantCells) {
            updateDependantCells(next);
        }
    }

    public void evaluateFormula(String formula, Cell cellWithFormula) {
        // for simplicity -- lets consider input as 
        // CellId SUM 

        StringTokenizer st = new StringTokenizer(formula, "=");

        // evaluate formula
        String cellToBeInitializedWithFormula = st.nextToken();

        if(!cells.containsKey(cellToBeInitializedWithFormula)) {
            cells.put(cellToBeInitializedWithFormula, new Cell(cellToBeInitializedWithFormula));
        }

        Cell cellRefToBeInitializedWithFormula = cells.get(cellToBeInitializedWithFormula);
        cellRefToBeInitializedWithFormula.updateFormula(formula);

        String expression = "("+st.nextToken()+")";

        int n = expression.length();

        List<Double> operand = new LinkedList<>();
        List<Character> opcode = new LinkedList<>();

        String cellId = "";
        double constant = 0;
        
        for(int i=0; i<n; ++i) {
            char ch = expression.charAt(i);
            // for every '(' -- i will add Integer.MAX_VALUE in the operand stack
            if(ch == '(') {
                opcode.add(ch);
            }
            else if(ch == ')') {
                // first part is to push operand to the stack
                if(!cellId.equals("")) {
                    if(!cells.containsKey(cellId)) {
                        cells.put(cellId, new Cell(cellId));
                    }
                    operand.add(cells.get(cellId).content);
                    cells.get(cellId).dependantCells.add(cellRefToBeInitializedWithFormula);
                    cellRefToBeInitializedWithFormula.parents.add(cells.get(cellId));
                    cellId = "";
                }
                else if(constant != Double.MIN_VALUE) {
                    operand.add(constant);
                    constant = Double.MIN_VALUE;
                }

                while(opcode.getLast() != '(') {
                    // considering the expression inside paranthesis is always valid
                    // at least there are 
                    double b = operand.removeLast(), a = operand.removeLast();
                    char opn = opcode.removeLast();
                    if(opn == '+') a+=b;
                    else if(opn == '-') a-=b;
                    else if(opn == '*') a*=b;
                    else if(opn == '/') a/=b;
                    else if(opn == '%') a%=b;
                    operand.add(a);
                }
                opcode.removeLast();
            }
            else if(ch == '*' || ch == '/' || ch == '%') {
                // first part is to push operand to the stack
                if(!cellId.equals("")) {
                    if(!cells.containsKey(cellId)) {
                        cells.put(cellId, new Cell(cellId));
                    }
                    operand.add(cells.get(cellId).content);
                    cells.get(cellId).dependantCells.add(cellRefToBeInitializedWithFormula);
                    cellRefToBeInitializedWithFormula.parents.add(cells.get(cellId));
                    cellId = "";
                }
                else if(constant != Double.MIN_VALUE) {
                    operand.add(constant);
                    constant = Double.MIN_VALUE;
                }

                opcode.add(ch);
            }
            else if(ch == '+' || ch == '-') {
                // first part is to push operand to the stack
                if(!cellId.equals("")) {
                    if(!cells.containsKey(cellId)) {
                        cells.put(cellId, new Cell(cellId));
                    }
                    operand.add(cells.get(cellId).content);
                    cells.get(cellId).dependantCells.add(cellRefToBeInitializedWithFormula);
                    cellRefToBeInitializedWithFormula.parents.add(cells.get(cellId));
                    cellId = "";
                }
                else if(constant != Double.MIN_VALUE) {
                    operand.add(constant);
                    constant = Double.MIN_VALUE;
                }

                // chk precendence first
                // evaluate based on precedence
                while(opcode.getLast() == '*' || opcode.getLast() == '/' || opcode.getLast() == '%') {
                    double b = operand.removeLast(), a = operand.removeLast();
                    char opn = opcode.removeLast();
                    if(opn == '+') a+=b;
                    else if(opn == '-') a-=b;
                    else if(opn == '*') a*=b;
                    else if(opn == '/') a/=b;
                    else if(opn == '%') a%=b;
                    operand.add(a);
                }
                opcode.add(ch);
            }
            else if(Character.isLetter(ch)) {
                cellId += ch;
            }
            else if(Character.isDigit(ch)) {
                if(cellId.equals("")) {
                    constant = constant*10 + (ch - '0');
                }
                else {
                    cellId += ch;
                }
            }
        }

        while(!opcode.isEmpty()) {
            double b = operand.removeLast(), a = operand.removeLast();
            char opn = opcode.removeLast();
            if(opn == '+') a+=b;
            else if(opn == '-') a-=b;
            else if(opn == '*') a*=b;
            else if(opn == '/') a/=b;
            else if(opn == '%') a%=b;
            operand.add(a);
        }

        double res = operand.removeLast();

        cellRefToBeInitializedWithFormula.content = res;
    }

    public void updateFormula(String formula, String cellId) {
        if(!cells.containsKey(cellId)) {
            cells.put(cellId, new Cell(cellId));
        }
        Cell cell = cells.get(cellId);
        if(!cell.formula.equals("")) {
            // we have the cell prev
            // we are updating the formula
            // reset all prev markings first
            // delete the parent dependencies
            for(Cell parent:cell.parents) {
                if(parent.dependantCells.contains(cell)) {
                    parent.dependantCells.remove(cell);
                }
            }
        }
        this.evaluateFormula(formula, cell);
    }
}

class Cell {
    int row, col;
    HashSet<Cell> dependantCells, parents;
    double content;
    String id, formula;

    public Cell(String id) {
        this.id = id;
        this.content = 0.0;
        getRowAndColFromCellId(this.id);
    }

    private void getRowAndColFromCellId(String cellId) {       
        // A5 -> (5, 1)
        StringBuilder colStr = new StringBuilder();
        int n = cellId.length(), row = 0, col = 0;
        for(int i=0; i<n; ++i) {
            char ch = cellId.charAt(i);
            if(Character.isLetter(ch)) {
                colStr.append(ch);
            }
            else {
                col = col * 10 + (ch - '0');
            }
        }
        n = colStr.length();
        int mul = (int)Math.pow(26, n-1);
        for(int i=0; i<n; ++i) {
            int ch = colStr.charAt(i) - 'A';
            col += (ch * mul);
            mul /= 26;
        }
        col++; // to get the present col
        this.row = row;
        this.col = col;
    }

    public void updateContent(double newContent) {
        this.content = newContent;
    }

    public void updateFormula(String formula) {
        this.formula = formula;
    }
}

/*

Class Representations: 

Cell: Atomic and lowest level in the excel sheet
Worksheet: Matrix/Collection of cells
Workbook: Collection of multiple worksheets

Important features:

1. Update content of cell
2. Store formula for a cell, to calculate the result in runtime (Formula parser) -- should be like A11 = SUM(A1:A10) or A5 = DIFF(SUM(A1, A2), PRODUCT(A3, A4))
3. Store reference of dependant cells of a cell. So, whenever a specific cell is modified, update dependant cells using DFS till the last limit
4. 


Will be using sparse matrix instead of continuous matrix for lazy initialization and reduce memory consumption. So, we would be initalizing a cell only upon a request.

*/