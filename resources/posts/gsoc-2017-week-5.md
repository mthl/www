---
title: Javascript for info-style navigation - Week 5
creator: Mathieu Lirzin
date: 2017-07-03T13:53:23+02:00
subject:
    - gsoc
    - texinfo
    - javascript
    - chromium
---

This is an update on the work I am doing this summer for my [Google Summer of Code](https://summerofcode.withgoogle.com/projects/#6199074135998464).  Please see [the first article](./gsoc-2017-week-1.md) of this serie of reports for a general introduction on what this project is about.

## Implementing index search

In the same spirit of menu navigation which was described [last week](./gsoc-2017-week-4.md), I have implemented the feature consisting of providing a text input with completion that allows the user to quickly access a particular node corresponding to a concept or API reference.  This feature is accessible with the `i` key.

To achieve that, we parse files ending with `-Index.xhtml` to find index links and build a dictionnary from them with a simple Javascript object.

## Refactoring of text input

While implementing the parsing and handling the `i` key took only one day of work. Adding those feature has brough some code duplication and complexity in the way the text input and message warnings are displayed.  To improve the situation I have applied some refactoring by separating the `Text_input` class from the `Minibuffer`.  The Minibuffer concept is taken from `info` and `info-mode`.  The difficulty is that warnings are displayed during a limited period using the `setTimeout` function and various user keyboard or mouse events can happened during that period and bring the application in an undesired state.

## Various improvements

In parallel of this refactoring I have worked on various tasks such as compatibility with Chromium 59 which brings some warning not shown by Firefox 54.  Those warnings were concerning some accesses made between iframes which didn't respect Chromium [same-origin policy](https://en.wikipedia.org/wiki/Same-origin_policy).  Besides those warnings I have worked on  ensuring that keyboard events are always bubbling which was not yet the case.  Some work has been done on having the focus always on the current iframe which should allow the user to scroll the pages with the keyboard without having to click in the current iframe first.

## Next step 

Now that the index search is working, I will focus on the global text search which will be accessible with the `s` key.

## Follow the developpement

Thanks to Gavin Smith, a live demo of the Kawa manual is available [here](https://www.gnu.org/software/texinfo/gsoc-2017-js-example/kawa)

The development of this project is done in public.  You can checkout the "gsoc-2017" branch of the [Git repository](https://git.savannah.gnu.org/git/texinfo.git) to see what is the current state of the project.
