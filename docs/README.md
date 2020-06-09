# MDSL Doc Page

This folder contains the MDSL Github page available at [https://microservice-api-patterns.github.io/MDSL-Specification/](https://microservice-api-patterns.github.io/MDSL-Specification/).

## Preview Page Locally
To preview the page locally (in case you want to make changes to the page), run the following commands within this folder:

 1. `bundle install`
 2. `bundle exec jekyll serve`
 
### Preconditions
To be able to preview the page as described above, you must have installed the following tools on your system:

 * [Jekyll](https://jekyllrb.com/docs/installation/)
   * Jekyll is a [Ruby Gem](https://rubygems.org/pages/download), so you have to install [Ruby](https://www.ruby-lang.org/en/downloads/) and [RubyGems](https://rubygems.org/pages/download) to be able to run it:
     * Verify that the following commands are available on your terminal:
       * `ruby -v`
       * `gem -v`
   * Installation instructions can be found [here](https://jekyllrb.com/docs/installation/).

## Eclipse Update Site
The folder _updates_ of this Github page contains an Eclipse update site with the latest version of the MDSL plugin. The update site can be updated as follows:

 1. Build the Update Site ZIP file as described in the README of the Xtext project: [Xtext project README](./../dsl-core#build-eclipse-update-site-locally) (`mvn clean verify`)
 2. Unzip the generated file (`dsl-core/io.mdsl.repository/target/io.mdsl.repository-x.x.x-SNAPSHOT.zip`)
 3. Copy the contents to the `docs/updates` folder of this repository (overwrite)
 4. Commit and push the changes
    1. `git add docs/updates/* -f`
    2. `git commit -m "Update MDSL update site for release vx.x.x"`
    3. `git push origin master`
