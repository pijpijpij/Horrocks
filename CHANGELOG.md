Change Log
==========

Version 0.2.3
-------------

 - Tec: Updated to AS 3.1.0, Gradle 4.4 and Kotlin 1.2.31.

Version 0.2.2
-------------

 - Fix: `MultipleReducerCreator` and `SingleReducerCreator` did not support logging event or Reducers whose `toString()` contains a `%`.

Version 0.2.1
-------------

 - Logger API removed and replaced by the one in AndroidUtils set of libraries.

Version 0.2.0
-------------

 - Logger API is augmented to New and improved API of Logger
 - Tec: Using explicit interfaces rather than non-specific `Function` provided by RxJava.
 - Tec: Using names closer to the Redux equivalent:
   - `Result` is now called `Reducer` The difference with Redux's `Reducer` is still that its `reduce()` method does not have an action 
   as a parameter.
   - `Feature` is now called `ActionCreator`
   - `Store` is now called `Storage`: it was never intended to map onto Redux's `Store`.
   
Version 0.1.1
-------------

 - Fix: documentation issues
 - Tec: repository configured to build with Travis CI

Version 0.1.0
-------------

Initial release.

