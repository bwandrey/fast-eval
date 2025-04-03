# Rule Engine Design Summary

## Feature: Rule Parsing, Evaluation, and Grouping
*(Scope is hard set on this)*

Rules are defined in a `.txt` file, parsed at application startup, and compiled into bytecode (via ByteBuddy) for fast runtime evaluation.

---

## Rule Syntax

Each rule follows the format:  
`TOKEN EXPRESSION VALUE`

Where `TOKEN` can be:
- a declared token (e.g. `stockPrice`)
- a literal value (`true`, `100.0`)
- another rule name (`priceHigh`)

Expressions supported:
- Logical: `AND`, `OR`, `XOR`, `NOT`
- Comparison: `>`, `<`, `==`, `!=`, `>=`, `<=`
- Parentheses are supported for grouping.

---

## Rule File Format

```
tokens:
    stockPrice: double
    stockHalted: boolean

rules:
    priceHigh: stockPrice > 100.0
    stockStopped: stockHalted == true
    criticalCondition: priceHigh AND stockStopped
groups:
    criticalSet:
        - priceHigh
        - stockStopped
        - criticalCondition
```

---

## Tokens

Tokens are defined as:
- A name
- A primitive type (`double`, `boolean`, `int`, etc.)

They are loaded into a map at startup:

```java
Map<String, TokenDefinition>
```

---

## Rule Parsing

Expressions are parsed into an AST using a recursive descent parser.

RuleNode types:
- `COMPARISON(token, operator, value)`
- `RULE_REFERENCE(ruleName)`
- `LOGICAL(type, left, right)` — for `AND`, `OR`, `XOR`
- `NOT(left)` (future)

---

## Eval Context

Token values are passed at runtime using a fluent interface:

```java
EvalContext context = new SimpleEvalContext()
    .withDouble("stockPrice", 120.0)
    .withBoolean("stockHalted", true);

context.getDouble("stockPrice");
```

Backed by a `Map<String, Object>`.

---

## Rule Evaluation

At application startup:
1. Parse tokens and rules.
2. Convert rule expressions into RuleNode ASTs.
3. Compile ASTs into classes implementing `Rule`.
4. Each compiled rule class takes tokens or sub-rules as constructor parameters.

Example usage:

```java
boolean result = FastEval.evaluate("criticalCondition")
    .withToken("stockPrice", 110.0)
    .withToken("stockHalted", true)
    .eval();
```

---

## Rule Groups

Rule Groups evaluate multiple rules in batch and return which ones matched.

Example definition:

```
groups:
    criticalSet:
        - priceHigh
        - stockStopped
        - criticalCondition
```

Example usage:

```java
List<String> triggered = FastEval.evaluateGroup("criticalSet", context);
// returns: ["priceHigh", "criticalCondition"]
```
