# MDSL Release Process

This is a step-by-step guide to create a public release of the MDSL tool.

## Preparation in PRIVATE Repository

0. Freeze feature development, test, document (at a minimum, provide samples for new features in `examples-advanced`or `experimental`). Update top-level README and `changelog.md` (also/again after version number update, see below).
   * The example `APILInterTestAndDemo.mdsl` is the only one that is supposed to show validation errors, but many other examples emit warnings.
1. Make sure that all your changes are merged to the master branch of the internal repository.
   * Validate that all examples (Git Pages, examples folder) validate and generate proper output.
2. Ensure that both builds (Maven and Gradle) run successfully:
   * `cd dsl-core`
   * `mvn clean verify` 
   * `./gradlew clean build` or `gradlew clean build` <!-- (note: remove `clean` if there is nothing to clean) -->
3. Update the plugin [version identifier](https://microservice-api-patterns.org/patterns/evolution/VersionIdentifier) to the next version. For example: (for v4.3.0)
   * `mvn versions:set -DgenerateBackupPoms=false -DnewVersion=4.3.0-SNAPSHOT`
   * `mvn org.eclipse.tycho:tycho-versions-plugin:1.2.0:update-eclipse-metadata` (note: 1.2.0 is the Tycho version, *not* the MDSL version!)
   * `git commit -a -m "Upgrade version to v4.3.0"`
   * `git push origin master` or, while still in feature branch, `git push`
   * _Hint:_ We still only work with SNAPSHOT releases in this project. There is no versioning process <!-- check [W] --> that creates actual releases (no difference between SNAPSHOT and release). Make sure that a three-digit semantic versioning scheme is always used!
4. Update and push the Eclipse Update Site (`./docs/updates` folder)
   * Delete all files in the `./docs/updates`, **except**  the `README.md` file!
   * Build the plugin by calling `mvn clean verify` in the `dsl-core` directory again.
   * Unzip the file `dsl-core/io.mdsl.repository/target/io.mdsl.repository-x.y.z-SNAPSHOT.zip` and copy all its contents into the `./docs/updates` folder.
   * `git add docs/updates/* -f` (forces adding files like JARs etc.; which are normally ignored)
   * `git commit -m "Update MDSL update site for release vx.x.x"`
   * `git push origin master` or, while still in feature branch, `git push` (and then create a PR, review it, merge it)
5. Create a release tag (for example 'v4.3.0') and document your release on GitHub (still in the internal/private repo).
6. Update the MDSL plugin in your Eclipse in order to check that the private update site works properly.

*Note:* Do not forget to switch back to master branch, and to create a new feature branch when continuing!


## Releasing to PUBLIC Repository

<!-- * More hints <https://github.com/Microservice-API-Patterns/MDSL-Specification/issues/16> -->

1. Copy all the contents of the private repo into the public repo (basically replace the whole content locally):
   * Best procedure: empty public folder completely and copy contents from private repository. 
   * *Important:* Delete things that should not go public, for instance, the experimental examples and older antlr4 grammar, as well as `RELEASING.MD` in `dsl-core` (this file)
2. Replace internal repo name in links on doc pages and READMEs with public repo name: 
    * `microservice-api-patterns.github.io/MDSL-Specification` -> `microservice-api-patterns.github.io/MDSL-Specification` (except for first occurrence in `docs/index.html`) and 
    * `https://github.com/Microservice-API-Patterns/MDSL-Specification/` to `https://github.com/Microservice-API-Patterns/MDSL-Specification/`(global find/replace in folder)
3. `git add --all`
4. `git add docs/updates/* -f` (forces adding files like JARs etc.; which are normally ignored)
5. Optional: If your current release (new contents in repository) includes other files such as JARs (for example, because there is a new project in the examples folder), use `git add {your-files} -f` (force adding) as well. Otherwise new contents will not be committed.
   * Example: `git add examples/protocol-buffers-example/grpc-code-sample/gradle/wrapper/* -f`
   * **Important**: You have to know what has been added here. Only add those files individually. If you execute a `git add {some-folder} -f` on the root folder or any other folder that contains generated sources or build output directories, you will add stuff that should not be committed.
6. `git commit -m "Release vx.y.z"` (commit all the files; replace the version in commit message)
7. Create a release tag (for example 'v4.3.0') and document your release on GitHub (public repo now).
8. `git push origin master --tags`
9. Ensure that builds run successfully <!-- was: here <https://travis-ci.com/github/Microservice-API-Patterns/MDSL-Specification> -->
10. Optional: fix errors if CI build was not successful.
11. Update the MDSL plugin in your Eclipse in order to check that the public update site works properly.
12. Build the CLI release locally:
    * Run `./gradlew clean build` (or `gradlew clean build`) again, optionally followed by `./gradlew publishToMavenLocal` (or `gradlew publishToMavenLocal`)
    * Rename the generated file `./dsl-core/io.mdsl.cli/build/distributions/mdsl-x.y.z-SNAPSHOT.tar` to `mdsl-cli-x.y.z.tar` (version number from previous steps)
    * Rename the generated file `./dsl-core/io.mdsl.cli/build/distributions/mdsl-x.y.z-SNAPSHOT.zip` to `mdsl-cli-x.y.z.zip` (version number from previous steps)
13. Edit the GitHub release and upload the two CLI binaries there, so that they are downloadable under the following links:
    * `https://github.com/Microservice-API-Patterns/MDSL-Specification/releases/download/vx.y.z/mdsl-cli-x.y.z.tar`
    * `https://github.com/Microservice-API-Patterns/MDSL-Specification/releases/download/vx.y.z/mdsl-cli-x.y.z.zip`
14. Update the CLI links in the CLI README `https://github.com/Microservice-API-Patterns/MDSL-Specification/blob/master/dsl-core/io.mdsl.cli/README.md`:
    * `[mdsl-cli-x.y.z.tar](https://github.com/Microservice-API-Patterns/MDSL-Specification/releases/download/v5.1.2/mdsl-cli-x.y.z.tar)`
    * `[mdsl-cli-x.y.z.zip](https://github.com/Microservice-API-Patterns/MDSL-Specification/releases/download/v5.1.2/mdsl-cli-x.y.z.zip)`

