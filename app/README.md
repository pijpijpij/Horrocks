# Sample application
This application is used to demonstrate how to use (or not to use) Horrocks in an Android app.
# Android Architecture Blueprints
Rather than start from scratch, I started from a branch of the Android Architecture Blueprints, more specifically, the code available in 
the branch [todo‑mvp‑dagger](https://github.com/googlesamples/android-architecture/tree/todo-mvp-dagger/). That branch uses 
[Dagger 2](https://google.github.io/dagger/) to add support for [dependency injection](https://en.wikipedia
.org/wiki/Dependency_injection). The focus is not on that however, I just wanted to show Horrocks in an **MVP** context.

# Notable changes
These changes were made independently of Horrocks, just because, well nobody's perfect and the sample wasn't quite up to scratch. 

## Done
This list explains what has been done.
- On the `Tasks` and `AddEditTask` screens, the FAB and its Coordinator layout were created in the activity layout but used in the 
fragment. They are moved to 
the fragment.
- using XML submenu for filtering instead of programmatically building it.
- Fragments implemented view directly instead of using delegation.
- on the `Tasks` screen, the presenter held filtering as a state that was just a duplicate of a similar state managed in the 
fragment.
Now the state is managed by the fragment and passed to the stateless presenter when needed.
- All views had a `isActive()` method. That was going against the normal flow of information between Presenter and View. That flag is now 
internal to views, or has entirely disappeared. 
- `AddEditTask` presenter had a `isDataMissing()` property called by the fragment to save/restore state on the presenter. That was going 
against the normal flow of information between Presenter and View. It was also "lazily" passed to the presenter when constructed.
That flag is now provided to the fragment by the presenter every time the presenter calls `View.display()`. 
- Some views were asymmetrically attached and detached to and from their presenters.

Some were just improvements:
- Introduced [ActivityStarter library](https://github.com/MarcinMoskala/ActivityStarter) to deal with saving and restoring instance states. 

## Still to do
This list explains remains to be done.
- Hungarian notation removal is only partial