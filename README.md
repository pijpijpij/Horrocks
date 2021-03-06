# Horrocks
There are multiple trends on Android at the moment. One of them is 
[Clean Architecture](https://8thlight.com/blog/uncle-bob/2012/08/13/the-clean-architecture.html). Another is 
[MVI](http://hannesdorfmann.com/android/model-view-intent). This library explores 
a [State Management talk](http://jakewharton.com/the-state-of-managing-state-with-rxjava/) by Jake Wharton. The pattern that video shows 
comes from [Redux](https://redux.js.org/).

Horrocks is not a port of Redux to Java, it takes some of its principles.
Key elements of Horrocks are:
- Immutability. All data exchanged is non-modifiable.
- Asynchronous processing with Rx
- Unidirectional flow of data
 
The benefits and drawbacks of these concepts are well documented elsewhere, for example in the document linked above.

This repository includes 2 different implementations:
- `library` is a Java library, with a lot of features. It is the victim of its language and of its own
  set of features. It is not longer actively developed.
- `library-kotlin` is a Kotlin library. Its functionality is born out of experience using the pattern. So some functionality
- found in `library` is not included.

## Library-kotlin

### Basic concepts
They are:
- `State`. The set of data that represents the state of the view.
- `display`. A method that given a state can display it on the UI.
- `Event`. A piece of data sent by the user to the system or a change of state of the system.
- `Reducer`. A piece of code that contains both data (equivalent to a Redux action) and process (Redux's reducer). When invoked on a
`State` this applies its data on to the state and produces a new `State`
- `Feature`. A piece of code that processes a specific type of events and creates `Reducer`s for this type of event.
- `Engine`: the code that collects the `Reducer`s emitted by `Feature`s, applies then to the current `State`.

#### Differences with `Library`
The `Model` found in `library` does not exist. Transforming the `State` into something a view can display is the role of the method `display`.
`View` is replaced by a method `display`.
`Event`s can come from 2 sources in `library-kotlin`:
- from an external source (no change there)
- from a state change. These state events are internal to the system.

`TriggeredReducerCreator`, `AsyncInteraction` and similar classes are greatly simplified and replaced by `Feature`s.

#### Event
A piece of data sent by the UI to the system. An event can be a complex dedicated data structure but it can also be a simple basic
data type, i.e. `String`, or even `Any`.

#### display
That method is passed to the `Engine` which calls it every time it has a new `State` to display. It is specific to the UI being
updated and it is entirely implemented by the application.

#### State
It is a set of data that represents the state of the view. It is almost but not exactly what is displayed on a UI: a state can contains  
some internal state of the screen tht is not meant to be displayed as-is.
`State` is a application-defined class. The library only expects this from it:
- it is immutable
- it is not nullable, i.e `String?` is not supported as a `State` but `String` is.

##### Transient Properties
Another notion is that of a transient property of `State`. Just before the engine applies a `Reducer` to its `State`,
the state's transient property is reset to its "un-set" value. So next time the state is emitted for a reason unrelated to that
property, the state includes that "un-set" value for that property. This was designed for cases where the property indicates the
view should perform a *one-off* action.
To implement this notion, the `Engine` applies its `clearEventProperties` method onto the state. `clearEventProperties` and uses  
the result as its new state. Note that there is no constraint on `State` to support this functionality.

#### Feature
A `Feature` emits a series of `Reducer`s. It can be triggered by an `Event`. This library provides an implementation
that contains all the boiler-plate code. The application must:
- provides the feature's specific behaviour, in a form defined in our feature DSL
- put the feature in the care of an instance of an `Engine`

#### Reducer
A `Reducer` is close semantically to Redux's Reducer: its main (only) method is a pure
function that take 2 parameters: the action data and a state to produce a new state.

#### Engine
It's probably the closest to a `Store` in Redux, but it's different enough that we kept its name different.
There should be one instance of an engine (noted above as the system) per screen in an app, listening to a number of `Features` and
applying `State`s to a single `display`.
Its distinctive capabilities are:
- an application-specific `display` method provided at construction.
- a state-specific `clearEventProperties` method which it uses to reset transient properties on `State` instances.
- an optional `Scheduler` on which it calls `display`, for applications that want to control the thread on which UI
updates are performed.
- An optional Log4J `logger` which the engine uses to log new states and errors:
  - a `Logger` named "Engine" if `logger` not provided at construction
  - `NoopLogger` is used (so no logging), if `null` was provided at construction
  - Otherwise, the logger provided at construction

### feature DSL
The `feature` method uses a simple DSL to help with `Feature` creation. It methods are:
- `triggeredStream` is a function that accepts the event and produces a stream of `Reducer`s. It also receives the current
  state (for context) as its seconds parameter.
- `onError` is a function that given an exception, produces a `Reducer`. It is called whenever the feature encounters an error.
  That allow contributing errors to the state rather than break the feature or ignore errors.
- `startWith` is an optional function that creates an event. If present, it will be called to provide the first event that the
  feature is to process
- `triggeredStream` has an override: it is a stream (rather than a function that creates a stream) of `Reducer`s. Only one of
  the 2 `triggeredStream` is allowed in the definition of a given feature.
- `triggeredSingle` is a specialised version of `triggeredStream`: it is a function that accepts an event and produces one
  `Reducer`. It also receives the current state (for context) as its seconds parameter. Only one of `triggeredStream` and
  `triggeredSingle` are allowed in the definition of a given feature.
- `triggeredSingle` has an override: it is a Single (rather than a function that creates a Single) `Reducer`. Only one of
  the 2 `triggeredSingle` is allowed in the definition of a given feature.


## Library

### Basic concepts
They are:
- `View`. The code that is to display data and generally interacts with the user.
- `Event`. A piece of data sent by the UI (`View`) to the system.
- `Model`. The set of data the view is to display.
- `State`. The set of data that represents the state of the view.
- `Reducer`. A piece of code that contains both data (equivalent to a Redux action) and process (Redux's reducer). When applied to a 
`State` this applies its data on to the state and produces a new `State`
- `TriggeredReducerCreator`. A piece of code that processes a specific type of UI events and creates `Reducer`s for this type of event.
  A simpler version `ReducerCreator` is not specifically triggered by an event.
- `Engine`: the code that collects the `Reducer`s emitted by `ReducerCreator`s and applies then to the current `State`.
It also calculate the `Model` from the `state` and emits it.

#### Event
A piece of data sent by the UI (`View`) to the system. An event can be a complex dedicated data structure but it can also be a simple basic 
data type, i.e. `String`.

#### Model
Views show data. This data takes the form of properties of the `Model`. All data shown on the view comes from the unique `Model`. 
The view also needs to pop up dialog box, navigate to other screens... These are also represented as properties of the 
same `Model`. The difference is that these properties are transient, i.e. they are reset each time before the state of the screen is 
re-calculated, and so `State` is about to be modified (by a new 
`Reducer`), they are reset.

#### State
It is a set of data that represents the state of the view. In a lot of screens, `Model` is the same as `State`. The difference 
between the 2 notions is that what is in the state may not be shown as-is on the screen and contains some internal state
of the screen.
`State` is the only source of data in `Model`, so it supports transient properties as well.

#### ReducerCreator
A `ReducerCreator` emits a series of `Reducer`s. It is named `ReducerCreator` because its semantics
are close to Redux's ActionCreator. It emits more than Actions however: Horrocks' `Reducer`s.
A `TriggeredReducerCreator` is a `ReducerCreator` that can be triggered by an `Event`.

#### Reducer
A `Reducer` is close semantically to Redux's Reducer. The major difference is that the main (only) method of a Redux reducer is a pure 
function that take 2 parameters: the action data and a state to produce a new state. Horrocks' `Reducer` already contains the data to 
apply to the `State`, so:
 - its main method has only one parameter,
 - it is not a pure function. 

#### Engine
It's probably the closest to a `Store` in Redux, but it's different enough that we kept its name different.
There should be one instance of an engine (noted above as the system) per screen in an app.

### Other Abstractions
#### Help with Feature creation
`TriggeredReducerCreator`s have some boiler-plate code. 4 classes are designed to remove of it as much as possible:
- `SingleReducerCreator` and `Interaction`
- `MultipleReducerCreator` and `AsyncInteraction`

##### Interaction
An `Interaction` accepts an `Event` of a specific, formats its data (if needed) and creates the `Reducer` that will apply that 
data to a `State`. The interaction is said completed when it `Reducer` is executed.
It is mostly used to actions on the screen that have a immediate result on that screen. For example clicking on a button 
changes the content of a text field.
One `Event` produces one `Reducer`.

##### AsyncInteraction
An `AsyncInteraction` accepts an `Event` of a specific type, formats the data (if needed) and emits a series of `Reducer`s 
that will apply data(s) to states. The interaction is completed when the _last_ of it `Reducers` are executed.
An `AsyncInteraction` emits between 0 and an infinity of `Reducer`s.

##### SingleReducerCreator
A `SingleReducerCreator` accepts an `Interaction` and applies it to all the `Event`s it received. It emits a stream of `Reducer`s 
that are produced by the interaction.
Note that `SingleReducerCreator` emits exactly the same number of `Reducer`s as it received `Event`s.

##### MultipleReducerCreator
A `MultipleReducerCreator` accepts an `Interaction` and applies it to all the `Event`s it received. It emits a stream of `Reducer`s 
that are produced by the interaction.
Note that `MultipleReducerCreator`s emits between 0 and an infinity of `Reducer`s.

#### Transient Properties
Another convenience notion is that of a transient property of `State`. Just before the engine applies a `Reducer` to its `State`, 
the state's transient property is reset to its "un-set" value. So next time the state is emitted for a reason unrelated to that 
property, the state includes that "un-set" value for that property. This was designed for cases where the property indicates the 
view should perform a *one-off* action.
To implement this notion, we do not annotate nor create a special type(s) for transient properties. Instead, the `Engine` applies 
a `TransientCleaner` onto the state. The engine gets its cleaner from its `Configuration`.


#### Validation
In certain cases, a `TriggeredReducerCreator` need to validate `Event`s against the current `State` of the app and then emit the relevant `Reducer`.
`StateProvider` is designed to be used in this scenario. Pass one in the constructor of the `ActionCreator` and Bob's your uncle. The 
only implementation of `StateProvider` That makes sense delegates to the `Storage` use in the screen. 
The `TriggeredReducerCreator` uses this state to decide what `Reducer`(s) to emit. Note that the state used then may not be the same as the one
handed to the `Reducer`(s), as some other `Reducer` of a different origin may have be executed in the meantime.


# Adding to Gradle project
Add the JitPack repository to your build file:
```groovy
allprojects {
  repositories {
    //...
    maven { url 'https://jitpack.io' }
  }
}
```
Add the dependency:
```groovy
dependencies {
  compile 'com.github.pijpijpij.horrocks:horrocks:0.6.0'
}
```

# Usage in Android
Any action on a presenter must done while the presenter's engine is active, i.e. when its `runWith(Configuration)` is being subscribed 
to, otherwise all events are simply ignored.

The lifecycle callback `onActivityResult()` occurs sometime between `onCreate()` and `onStart()`. When `onActivityResult()` is called in 
the activity or fragment, that usually triggers some action on the presenter. So a requirement for Horrocks to function in Android is for 
the subscription to be in place before then, i.e. in `onCreate()` (or one of its sub-calls, `onCreateView()` or `onViewCreated()`).

## MVP
In an MVP architecture, assuming that attaching the view to its presenter is done in `onCreate()`, then this is when the presenter should 
create that subscription. In the sample application, it is in the implementations of `BasePresenter.takeView()` that 
`Engine.runWith(Configuration)` is subscribed to.

# Building and Releasing the library

## Automated build status

Status   | CI   | Description
:---:    | :--- | :---
[![](https://jitpack.io/v/pijpijpij/horrocks.svg)](https://jitpack.io/#pijpijpij/horrocks) | [Jitpack](https://jitpack.io) | Released  binaries are available [there](https://jitpack.io/#pijpijpij/Horrocks). Jitpack is not used to run extensive tests, just build the releases.
[![CircleCI](https://circleci.com/gh/pijpijpij/Horrocks.svg?style=svg)](https://circleci.com/gh/pijpijpij/Horrocks) | [CircleCI](https://circleci.com/) | The full build, including running UI tests for the demo app is done via Circle CI. Yes!
[![Build Status](https://travis-ci.org/pijpijpij/Horrocks.svg?branch=master)](https://travis-ci.org/pijpijpij/Horrocks) | [Travis-CI](https://travis-ci.org/) | Previous builds were done with Travis CI but the UI tests did not complete before the build timed out. Also results are very difficult to get hold off.
[![Build Status](https://app.bitrise.io/app/110b2b59839df29d/status.svg?token=JtkKm00hyRv3f895SyGMbw&branch=master)](https://app.bitrise.io/app/110b2b59839df29d) | [Bitrise](https://app.bitrise.io/) | We also tried Bitrise but neither method of running UI tests completed before the build timeout.

## Build and install the libraries locally?

`> gradlew build` builds the project
`> gradlew install` places it in the local Maven repository.

## How to release the libraries?

That means creating a release version and prepare it for the next release increment. That includes setting up its SCM.
Releases are tagged with their version number (e.g. release 5.3.2 is build from the version tagged `5.3.2` in Git).

1. Checkout the head of `master` and start a command prompt
1. Run pre-release checks. Do a full build to ensure the code is good to be released.

    `> ./gradlew build`

1. Release (assuming authentication with SSH keys is already setup). If not, Bitbucket explained it well 
[here](https://confluence.atlassian.com/x/YwV9E):

    `> ./gradlew release`

    Make sure the last output line indicates it's been *pushed to origin*.

    To set the release number, rather than accept the usual bug-level increment, add the following property on the 
    command line:

    `-Prelease.forceVersion=k.m.n`

1. Build the release version of the app to take the new version number into account:

    `> ./gradlew build install`
    
    That is only needed if you do not want to wait for [Jitpack](https://jitpack.io/#pijpijpij/Horrocks) to finish its 
    build.


The overall command is quite simple:

    > ./gradlew build release

