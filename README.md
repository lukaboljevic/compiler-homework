# Compiler homework

This homework was part of the subject "Programski prevodioci",
or as everybody tends to call it, "Kompajleri".

As of now, this homework includes the implementation
of the Scanner (Lexical Analyzer) and Parser (Syntax Analyzer).
It is not the best code I've ever written, and I can't say
I'm 100% proud of it, but it will do just fine for the purposes
of the final exam. Don't judge.

# Note

The grammar is LL(1). That means, for every nonterminal A and 
every string  of symbols b and c such that b =/= c, and A -> b | c,
we have that:
1. First(b) ∩ First(c) is an empty set,
2. Out of at most one of b and c you can produce an empty string,
3. If b =>* eps, then First(c) ∩ Follow(A) is also an empty set.

Conditions 1. and 2. are 100% satisfied. I have checked number 3.
"in my head", but there shouldn't be any problems regardless.