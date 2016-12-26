# Contribution Guidelines

## Introduction

This document explains how to contribute changes to the play-ebean project.




## Bug reports

Please search the issues on the issue tracker with a variety of keywords to
ensure your bug is not already reported.

If unique, [open an issue](https://github.com/payintech/play-ebean/issues/new)
and answer the questions so we can understand and reproduce the problematic
behavior.

The burden is on you to convince us that it is actually a bug in play-ebean.
This is easiest to do when you write clear, concise instructions so we can
reproduce the behavior (even if it seems obvious). The more detailed and
specific you are, the faster we will be able to help you. Check out [How to
Report Bugs Effectively](http://www.chiark.greenend.org.uk/~sgtatham/bugs.html).

Please be kind, remember that play-ebean comes at no cost to you, and you're
getting free help.




## Code review

Changes to play-ebean must be reviewed before they are accepted, no matter who
makes the change even if it is an owner or a maintainer. We use GitHub's pull
request workflow to do that.

Please try to make your pull request easy to review for us. Please read the
"[How to get faster PR reviews](https://goo.gl/Z7Domi)" guide, it has lots of
useful tips for any project you may want to contribute. Some of the key points:

* Make small pull requests. The smaller, the faster to review and the more
  likely it will be merged soon.
* Don't make changes unrelated to your PR. Maybe there are typos on some
  comments, maybe refactoring would be welcome on a function... but if that
  is not related to your PR, please make *another* PR for that.
* Split big pull requests in multiple small ones. An incremental change will
  be faster to review than a huge PR.




## Sign your work

The sign-off is a simple line at the end of the explanation for the patch. Your
signature certifies that you wrote the patch or otherwise have the right to pass
it on as an open-source patch.

```
Signed-off-by: John Smith <john.smith@email.com>
```

Please use your real name, we really dislike pseudonyms or anonymous
contributions. We are in the opensource world without secrets. If you have set
your `user.name` and `user.email`, you can sign your commit automatically
with `git commit -s`.




## Write a good commit message

A good commit message serve at least three important purposes:

* To speed up the reviewing process.

* To help us write a good release note.

* To help the future maintainers of play-ebean, say five years into the
  future, to find out why a particular change was made to the code or
  why a specific feature was added.

Structure your commit message like this:

From: [[http://git-scm.com/book/ch5-2.html]]

> ```
> Short (50 chars or less) summary of changes
>
> More detailed explanatory text, if necessary.  Wrap it to about 72
> characters or so.  In some contexts, the first line is treated as the
> subject of an email and the rest of the text as the body.  The blank
> line separating the summary from the body is critical (unless you omit
> the body entirely); tools like rebase can get confused if you run the
> two together.
>
> Further paragraphs come after blank lines.
>
>   - Bullet points are okay, too
>
>   - Typically a hyphen or asterisk is used for the bullet, preceded by a
>     single space, with blank lines in between, but conventions vary here
> ```

#### DO
* Write the summary line and description of what you have done in the imperative
  mode, that is as if you were commanding someone. Start the line with "Fix",
  "Add", "Change" instead of "Fixed", "Added", "Changed".
* Always leave the second line blank.
* Line break the commit message (to make the commit message readable without
  having to scroll horizontally in `gitk`).

#### DON'T
* Don't end the summary line with a period - it's a title and titles don't end
  with a period.

#### Tips
* If it seems difficult to summarize what your commit does, it may be because
  it includes several logical changes or bug fixes, and are better split up
  into several commits using `git add -p`.

#### References
The following blog post has a nice discussion of commit messages:
http://chris.beams.io/posts/git-commit/
