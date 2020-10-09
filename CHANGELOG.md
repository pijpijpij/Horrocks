Change Log
==========

Version 0.6.0
-------------

- Added initial version of a new Kotlin UDF library, 'library-kotlin'. It has a simple API and functionality than `library`.

Version 0.5.2
-------------

 - The default engine emits the last emitted state to new subscribers.
 - Fix: multiple subscribers cause multiple executions of features.
 - Tec: Updated to AS 3.2.0, Gradle 4.8.1 and Kotlin 1.3.x.
 - Further separated the creation of Reducers from action triggers by introducing Triggerable.
 - Relaxed requirement for the engine: it still needs ReducerCreator, but it is a different
 one: it has been renamed ReducerCreator to TriggeredReducerCreator. The new ReducerCreator
 does not include any notion of event.
 - Fix ErrorReducerFactory should be public.
 The Default Engine no longer stops when a feature throws. ErrorReducerFactory is added to
 customize the engine's behaviour in that situation.

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

