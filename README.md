# REST Query library

Allows to read queries from the REST requests (query parameters) and map them to the typed SQL Slick queries.
Supports ANDs and ORs, pagination and sorting.

Queries can be build in the code as well (e.g., for testing):

```
SearchFilterExpr.Atom.NAry("id", In, objects.map(x => Long.box(x.id)))
```

or

```
SearchFilterExpr.Union(Seq(
  SearchFilterExpr.Atom.Binary("status", Eq, "New"),
  SearchFilterExpr.Atom.Binary("status", Eq, "Verified")
)
```
