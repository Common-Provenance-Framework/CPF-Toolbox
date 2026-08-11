# Contributing

Thank you for your interest in contributing!

## Setup

1. Fork the repository.
2. Clone your fork and create a feature branch:
   ```sh
   git checkout -b feat/your-feature
   ```

3. Build and test:

   ```sh
   mvn clean verify
   ```

> **Note:** the test suite regenerates files under `cpm-template/src/test/resources/`, so your working tree will be dirty afterwards. Unless you intentionally changed the expected output, discard them before committing:
>
> ```sh
> git checkout -- cpm-template/src/test/resources/
> ```

## Commit Guidelines

Use [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/). Examples:

* `feat(core): add support for X`
* `fix(template): correct behavior of Y`
* `docs: README commit guideline`

## Pull Requests

* Target the `main` branch.
* Ensure all tests pass.
* Include relevant tests and documentation.

By contributing, you agree that your contributions will be licensed under the Apache 2.0 License.
