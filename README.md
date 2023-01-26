# Repository Observer
**Observer** is a behavioral design pattern that lets you define a subscription mechanism to notify multiple objects about any events that happen with the object they’re observing.

In this exercise you need to implement a [`Repository`](src/main/java/com/epam/rd/autocode/observer/git/Repository.java) interface.\
This interface represents behavior of a repository of a version-control system, like Git.\
It supports commits to its branches and merges between branches.\
Also, it supports web hooks - observers, that observe commit or merge events.\
`Repository` methods:
- `Branch getBranch(String name)` - returns a branch for a given name. Returns `null` if no branch was found.
- `Branch newBranch(Branch sourceBranch, String name)` - creates and returns a new branch.
  New branch contains all the commits of the source branch.
  If the branch with such a name already exists, throw an `IllegalArgumentException`.
  If the source branch is not present in current repository, throw an `IllegalArgumentException`.
- `Commit commit(Branch branch, String author, String[] changes)` - creates a commit to the given branch. Parameters:
  - `branch` - a branch where to submit the commit,
  - `author` - name of the commit author,
  - `changes` - an array of changes which are the body of the commit.
- `void merge(Branch sourceBranch, Branch targetBranch)` - merges source branch commits to the target branch.
  All the commits from the source branch which are not present in the target branch, must be copied to the target branch.
  Keep the order of copied commits.
- `void addWebHook(WebHook webHook)` - adds a WebHook to this repository.
  Once a web hook is added, it tracks all the events matching its parameters.

The `WebHook` is another interface that you need to implement.
It is an observer to events of creating commits and merges between branches. Methods of the `WebHook` interface:
- `String branch()` - returns a name of the branch, which this webhook observes. 
- `Event.Type type()` - returns a type of events, which this webhook observes.
  - `COMMIT` value means that the webhook tracks all the commits to the marked branch.
  - `MERGE` value means that the webhook tracks all the merges where the marked branch is the target branch.
- `List<Event> caughtEvents()` - returns a List of Events caught by this WebHook. Returns empty List if no event was caught.
- `void onEvent(Event event)` - a method to submit an event to this webhook.

Implement [`com.epam.rd.autocode.observer.git.GitRepoObservers`](src/main/java/com/epam/rd/autocode/observer/git/GitRepoObservers.java) methods:
- `newRepository` - returns a Repository.\
  Once a repository is created, it must contain a single branch with name "main" and must contain no initial commits.
- `mergeToBranchWebHook` - returns a WebHook that observes merge events for a target branch.
- `commitToBranchWebHook` - returns a WebHook that observes commit events for a target branch.