# inference

Experimental way to program something

## Example - query

```kotlin
query("? is a friend of ?")
// => []

query("John is a friend of James")
// => [Relation(John, is a friend of, James)]

query("John is a friend of Sarah")
// => [Relation(John, is a friend of, Sarah)]

query("Sarah is a friend of James")
// => [Relation(Sarah, is a friend of, James)]

query("John is a friend of ?")
// => [Relation(John, is a friend of, James),
//     Relation(John, is a friend of, Sarah)]

query("? is a friend of ?")
// => [Relation(John, is a friend of, James),
//     Relation(John, is a friend of, Sarah),
//     Relation(Sarah, is a friend of, James)]
```

## Example - join

```kotlin
// common friends of Max and Sarah:
join(
    "? is a friend of Max",
    "? is a friend of Sarah"
)
// => ["Claudia", "Victoria", "John"]
```
