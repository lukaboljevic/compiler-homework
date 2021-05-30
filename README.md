# Compiler homework

This homework was part of the subject "Programski prevodioci", or as everybody tends to call it, "Kompajleri".

This homework includes the implementation of the Scanner (Lexical Analyzer) and Parser (Syntax Analyzer). A parse tree to go with the grammar 
has also been added. The grammar can be found in `grammar.txt`.

There are no dependencies for this mini project. All you need to do is run the project from an IDE (I use IntelliJ) or the CMD. 

### Small note

If you are running the project from the CMD, you may need to change the line `path += "\\src\\test.txt";` in `TestParser.java` (the main class)
to `path += "\\test.txt";`, as in the CMD, you need to navigate to the `src` folder in order to run `TestParser.java`.

# Grammar

The grammar is LL(1). That means, for every nonterminal A and  every string  of symbols b and c such that b =/= c, and A -> b | c,
we have that:
1. First(b) ∩ First(c) is an empty set,
2. Out of at most one of b and c you can produce an empty string,
3. If b =>* eps, then First(c) ∩ Follow(A) is also an empty set.

Conditions 1. and 2. are 100% satisfied. I have checked number 3. "in my head", but there shouldn't be any problems regardless.
