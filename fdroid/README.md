# F-Droid build recipe

`metadata/app.swiftnef.yml` is the proposed build recipe for SwiftRAW's
submission to the official F-Droid repository.

The canonical recipe must ultimately be submitted to the `metadata/` directory
of [`fdroid/fdroiddata`](https://gitlab.com/fdroid/fdroiddata) as
`metadata/app.swiftnef.yml`. This copy is kept with the upstream source so the
application maintainer and F-Droid packagers can review and update it together.

Before submitting the recipe to `fdroiddata`, validate it there with:

```sh
fdroid readmeta
fdroid rewritemeta app.swiftnef
fdroid lint app.swiftnef
fdroid build -v -l app.swiftnef
```
