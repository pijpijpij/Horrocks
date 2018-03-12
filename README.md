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

## Basic concepts
They are:
- `View`. The code that is to display data and generally interacts with the user.
- `Event`. A piece of data sent by the UI (`View`) to the system.
- `Model`. A set of data the view is to display.
- `State`. A set of data that represents the state of the view. In a lot of screens, `Model` can be used as `State`.
- `Reducer`. A piece of code that contains both data (equivalent to a Redux action) and process (Redux's reducer). When applied to a 
`State` this applies its data on to the state and produces a new `State`
- `ReducerCreator`. A piece of code that processes a specific type of UI events and creates `Reducer`s for these events.
- `Engine`: the code that collects the `Reducer`s emitted by `ReducerCreator`s and applies then to the current `State`.
It also calculate the `Model` from the `state` and emits it.

### Event
A piece of data sent by the UI (`View`) to the system. An event can be a complex dedicated data structure but it can also be a simple basic 
data type, i.e. `String`.

### Model
Views show data. This data takes the form of properties of the `Model`. All data shown on the view comes from the unique `Model`. 
The view also needs to pop up dialog box, navigate to other screens... These are also represented as properties of the 
same `Model`. The difference is that these properties are transient, i.e. each time the `State` is about to be modified (by a new 
`Reducer`), they are reset.

### State
It is a set of data that represents the state of the view. In a lot of screens, `Model` can be used as `State`.

### ReducerCreator
An `ReducerCreator` can be triggered by an `Event` and emits a series of `Reducer`s. It is named `ReducerCreator` because its semantics 
are close to Redux's ActionCreator. It emits more than Actions however: Horrocks' `Reducer`s.
 
### Reducer
A `Reducer` is close semantically to Redux's Reducer. The major difference is that the main (only) method of a Redux reducer is a pure 
function that take 2 parameters: the action data and a state to produce a new state. Horrocks' `Reducer` already contains the data to 
apply to the `State`, so:
 - its main method has only one parameter,
 - it is not a pure function. 

### Engine
It's probably the closest to a `Store` in Redux, but it's different enough that we kept its name different.
There should be one instance of an engine (noted above as the system) per screen in an app.

## Other Abstractions
`ReducerCreator` have some boiler-plate code. 4 classes are designed to remove as much as possible:
- `SingleReducerCreator` and `Interaction`
- `SingleReducerCreator` and `AsyncInteraction`

### Validation
In certain cases, `ReducerCreator` need to validate `Event`s against the current `State` of the app and then emit the relevant `Reducer`.
`StateProvider` is designed to be used in this scenario. Pass one in the constructor of the `ActionCreator` and Bob's your uncle. The 
only implementation of `StateProvider` That makes sense delegates to the `Storage` use in the screen. 
The `ReducerCreator` uses this state to decide what `Reducer`(s) to emit. Note that the state used then may not be the same as the one 
handed to the `Reducer`(s), as some other `Reducer` of a different origin may have be executed in the meantime.
  

# Adding to Gradle
Add the JitPack repository to your build file:
```groovy
allprojects {
  repositories {
    //...
    maven { url 'https://jitpack.io' }
  }
}
```
Add the dependency
```groovy
dependencies {
  compile 'com.github.pijpijpij.horrocks:horrocks:0.1.1-SNAPSHOT'
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

# Building and Releasing the libraries

## Automated build status
There is not CI quite yet :(.

The binaries of releases are also available thanks to [Jitpack](https://jitpack.io). The latest release there is 
[![](https://jitpack.io/v/pijpijpij/horrocks.svg)](https://jitpack.io/#pijpijpij/horrocks).

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
    
    That is only needed if you do not want to wait for [Jitpack](https://jitpack.io/#org.github.pijpijpij/horrocks/) to finish its 
    build.


The overall command is quite simple:

    > ./gradlew build release

