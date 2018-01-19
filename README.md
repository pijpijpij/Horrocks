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

##Basic concepts
They are:
- View. The code that is to display data and generally to interact with the user.
- Event. A piece of data emitted by the UI (View) to the system.
- ViewModel. A set of data the view is to display.
- State. A set of data that represents the state of the view. Most of the time, ViewModel can be used as state of the view.
- Feature. A piece of code that processes a specific type of UI event and contributes to the state of the view. It is triggered when the 
View emits an event. 
- Result: pieces of code emitted by Features that can contribute to advancing the state of the view.
- Engine. The code that listens for Results and applies then to the state. It also calculate the ViewModel from it and emits it..

###Event
This is the equivalent of an `Action` in Redux. A difference is that an event can be a complex data structure designed for he purpose of 
being an `Action` but it can also be a simple basic data type, i.e. `String`.

###ViewModel
Views show data. This data takes the form of properties of the ViewModel. All the data shown on the view comes from the ViewModel. 
But the view also needs to pop up dialog box, navigate to other screens... These are also represented as properties of the 
ViewModel. The difference is that these properties are transient, i.e. each time the State is about to be modified (by a new Result), 
they are reset.

###Feature
A Feature can be triggered by and event and emit a series of Results.

###Engine
It's probably the closest to a `Store` in Redux.

