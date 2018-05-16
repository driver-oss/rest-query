# REST Query library

Allows to read queries from the REST requests (query parameters) and map them to the typed SQL Slick queries.
Supports ANDs and ORs, pagination and sorting.

Example of the REST request applying filtering:
```
https://example.org/v1/tasks?filters=status+EQ+New&filters=assignee+EQ+null&pageNumber=1&pageSize=20&sort=lastUpdate
```

Queries can be built in the code as well (e.g., for testing):

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
