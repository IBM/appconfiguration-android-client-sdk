# Contributing

Review the following guidelines for submitting questions, issues, or changes to this repository.

## Questions

If you have issues using the SDK or have a question about the AppConfiguration service, you can ask a question on [Stack Overflow](https://stackoverflow.com/questions/tagged/ibm-appconfiguration). Be sure to include the `ibm-appconfiguration` tag.

## Coding Style
This SDK follows coding style based on [Google’s Android coding standards][GoogleAndroidCodingStandard] for source code in the Kotlin Programming Language.

To ensure source code has no structural problems, run [Android Lint][AndroidLint] code scanning tool that helps to identify and correct problems with the structural quality of the code.

## Issues

If you encounter an issue with the Android Client SDK, you are welcome to submit a [bug report](https://github.com/IBM/appconfiguration-android-client-sdk/issues).
Before that, please search for similar issues. It's possible somebody has encountered this issue already.

## Pull Requests

If you want to contribute to the repository, here's a quick guide:

1. Fork the repository
2. Develop and test your code changes:
    * Follow the coding style as documented above
    * Please add one or more tests to validate your changes.
3. Make sure everything builds/tests cleanly.
4. Commit your changes
5. Push to your fork and submit a pull request to the `master` branch

## Running the tests

For Windows: use `gradlew.bat`
For Linux or Mac: use `./gradlew`

By default, when you run `./gradlew testDebugUnitTest`, the unit tests are run.

## Code coverage

This repo uses [Jacoco](https://www.eclemma.org/jacoco/) to measure code coverage. To obtain a code
coverage report, run `./gradlew jacocoTestReport`. View the coverage report:

```
open lib/build/reports/rep/jacocoTestReport/html/index.html
```

## Generating documentation

To generate the HTML docs for the project, run `./gradlew dokkaHtml`. View the docs:

```
open lib/build/dokka/html/index.html
```

To generate the Javadoc for the project, run `./gradlew dokkaJavaDoc`. View the docs:

```
open lib/build/dokka/javadoc/index.html
```

## Additional Resources

* [General GitHub documentation](https://help.github.com/)
* [GitHub pull request documentation](https://help.github.com/send-pull-requests/)

[stackoverflow]: http://stackoverflow.com/questions/ask?tags=ibm-appconfiguration
[GoogleAndroidCodingStandard]: https://google.github.io/styleguide/javaguide.html
[AndroidLint]: https://developer.android.com/studio/write/lint#commandline

# Developer's Certificate of Origin 1.1

By making a contribution to this project, I certify that:

(a) The contribution was created in whole or in part by me and I
   have the right to submit it under the open source license
   indicated in the file; or

(b) The contribution is based upon previous work that, to the best
   of my knowledge, is covered under an appropriate open source
   license and I have the right under that license to submit that
   work with modifications, whether created in whole or in part
   by me, under the same open source license (unless I am
   permitted to submit under a different license), as indicated
   in the file; or

(c) The contribution was provided directly to me by some other
   person who certified (a), (b) or (c) and I have not modified
   it.

(d) I understand and agree that this project and the contribution
   are public and that a record of the contribution (including all
   personal information I submit with it, including my sign-off) is
   maintained indefinitely and may be redistributed consistent with
   this project or the open source license(s) involved.