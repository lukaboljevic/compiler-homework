public class Token {

	public int kind;		// token kind
	public int line;		// token line
	public int col;			// token column
	public int intVal;		// token value for integers
	public double doubleVal;// token value for doubles
	public String string;	// for whatever

	public String toString(){
		// for printing purposes, not really crucial
		String s = string + " " + kind;
		if (kind == 42){  // integer constant
			s += ", " + intVal;
		}
		if (kind == 43){  // double constant
			s += ", " + doubleVal;
		}
		return s;
	}
}
