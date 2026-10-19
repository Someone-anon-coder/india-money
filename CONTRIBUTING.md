# Contributing

Issues and pull requests are welcome.

1. Open an issue first for new behaviour; this library is deliberately small (money, quantity, formatting, words, round-off).
2. Branch from `main`: `<type>/<area>/<short-desc>`. Never delete branches.
3. Follow `CLAUDE.md`: file header, Javadoc on public API, `FOLDER.md` per folder, files of at most 400 lines.
4. Every change needs tests (unit, plus property or golden where it fits). Run `./gradlew check` and `./gradlew pitest`.
5. Commits use Conventional Commits, for example `feat(money): add percent rounding`.
6. No runtime dependencies. Test and build tools must be permissively licensed.

Rules that come from law or a standard must cite their source in Javadoc and the README.
