---
title: Javascript for info-style navigation - Week 4
creator: Mathieu Lirzin
date: 2017-06-25T12:41:34+02:00
subject:
    - gsoc
    - texinfo
    - javascript
    - navigation
    - history
---

This is an update on the work I am doing this summer for my [Google Summer of Code](https://summerofcode.withgoogle.com/projects/#6199074135998464).  Please see [the first article](./gsoc-2017-week-1.md) of this serie of reports for a general introduction on what this project is about.

## Implementing menu navigation

One feature of `info-mode` and `info` consists in the possibility to navigate through the sub-menu of the current page using a text input.  This input is capable of word completion that can be completed using the `Tab` key.  When no menu is available in the current page, the text input is replaced with a message warning telling the user that there is "No menu for this node".

![](/images/info-coreutils-menu.png)

To implement a similar functionnality in a web page, I have used a fixed `<div>` element containing a `<input type="search" list="menu">` element.  The `list` attribute refers to a `<datalist id="menu">` element which allows us to define a list of possible completions.  To build this list, we have to dynamically scan the current page for links in the footer that we were previously ignoring when scanning for `up`, `next`, and `prev` links.   One issue with that approach is that the `datalist` feature may not be reliable on every browser we want to support.  We may want to find an alternative solution in the future.  The current display of the text input is less than optimal so it will need some improvements. 

## Implementing the navigation to *top* and *final* nodes

Since the implementation didn't took as much time as expected, I have decided to add the navigation using the `<` and `>` keys.  The navigation to the *top* and *final* nodes can be done using the `<` and `>` keys.  To implement that feature I have adapted the `create_link_dict` which was described in [last report](./gsoc-2017-week-2+3.md).  Since we are taversing the tree of nodes using a *Depth First Search* (DFS) algorithm we have to store a link in the returned dictionnary for the first and the final node of the tree walk.  To store those links which are not indexed by actual page identifiers but by an arbitary name, we use a pointer property which contains the identifer on the corresponding page.

## Handling history of the page navigation

When dealing with classic static pages, the browser offers the possibility to go back and forward in the history of pages previously seen using buttons close to the navigation bar.  However when using a non classic navigation such as the one we are using which is based on *iframes* those button don't work anymore.  To make them work we have to use the [history API](https://developer.mozilla.org/en-US/docs/Web/API/History_API) which offers a *stack* like interface to store data and which is reused by the `window.onpopstate` handler which is a function receiving a "popstate" event which is sent by the `history.back` and `history.forward` methods.

While The initial prototype was already containing some code using this API, I was not familiar with it so it took me some time to get a usable implementation.

## Next step 

The menu navigation was a intermediary step toward having an index and regexp search.  The text based input will reused for those features.

## Follow the developpement

The development of this project is done in public.  The repository has moved from [Notabug](https://notabug.org/) to [Savannah](https://savannah.gnu.org/).  You can checkout the "gsoc-2017" branch of the [new Git repository](https://git.savannah.gnu.org/git/texinfo.git) to see what is the current state of the project.
