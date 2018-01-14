# Horrocks
There are multiple trends on Android at the moment. One of them is 
[Clean Architecture](https://8thlight.com/blog/uncle-bob/2012/08/13/the-clean-architecture.html). Another is 
[MVI](http://hannesdorfmann.com/android/model-view-intent). This library explores 
a [State Management talk](http://jakewharton.com/the-state-of-managing-state-with-rxjava/) by Jake Wharton. That pattern comes from 
 [Redux](https://redux.js.org/).
 Horrocks is not a port of Redux to Java. It takes some of its principles. For example, there is no implementation of the notion of Action.
 
 Key elements of Horrocks are:
 - Immutability. All data exchanged is non-modifiable.
 - Asynchronous processing
 - unidirectional flow of data
 The benefits and drawbacks of these concepts are well documented elsewhere, for example in the document linked above.