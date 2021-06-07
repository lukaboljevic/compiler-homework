# Compiler homework

This homework was part of the subject "Programski prevodioci", or as everybody tends to call it, "Kompajleri".

This homework includes the implementation of the Scanner (Lexical Analyzer) and Parser (Syntax Analyzer). A parse tree to go with the grammar 
has also been added. The grammar can be found in `grammar.txt`.

There are no dependencies for this mini project. All you need to do is run the project from an IDE (I use IntelliJ) or the CMD. 

### Small note

If you are running the project from the CMD, you may need to change the line `path += "\\src\\test.txt";` in `TestParser.java` (the main class)
to `path += "\\test.txt";`, as in the CMD, you need to navigate to the `src` folder in order to run `TestParser.java`.

# Grammar

The grammar is LL(1). That means, for every nonterminal A and for every string of symbols U and V such that U =/= V, and A -> U | V,
we have that:
1. First(U) ∩ First(V) is an empty set,
2. Either only U or only V can produce an empty string (eps),
3. If U =>* eps, then First(V) ∩ Follow(A) is also an empty set.

Conditions 1. and 2. are 100% satisfied. I have checked number 3 "in my head", but there shouldn't be any problems regardless.
