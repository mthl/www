---
title: Javascript for info-style navigation - Final report
creator: Mathieu Lirzin
date: 2017-08-25T17:32:10+02:00
subject:
    - gsoc
    - texinfo
    - javascript
---

I spent this summer working on the implementation of the features available of the `info` program and `info-mode` in Emacs to the Web browser using Javascript.  Those features includes the regexp search, the index search, and an efficient keyboard-centric user interface. The code for this project is accessible via the **gsoc-2017** branch of the [Texinfo Git repository](https://git.savannah.gnu.org/cgit/texinfo.git/log/?h=gsoc-2017).

```sh
git clone https://git.savannah.gnu.org/git/texinfo.git
cd texinfo
git checkout gsoc-2017
```

Next, you can run the build instructions from the README to build the *GNU Kawa* and *GNU Hello* manuals that has been used as examples for this project.  Additionally you can access the live demo of those examples [here](https://www.gnu.org/software/texinfo/gsoc-2017-js-example/kawa/) and [there](https://www.gnu.org/software/texinfo/gsoc-2017-js-example/hello/).

## Implemented features

Most of the keyboard shortcuts available in the `info` program have been implemented.  They can be listed with the `?` key or the when clicking on the *?* button on the top right of the page.

#### Basic navigation

The `[` and `]` keys allow the user to navigate the manual linearly.  The order of navigation is deduced from the table of content which is present in the `index.html`.  The `n` and `p` keys allow navigating through the different sections of the same level and are based on the links that are statically present on each page.

#### Menu navigation

When the current page contains a menu it is possible to use the `m` key to access to it using the keyboard.  Completions are available in browsers that support the `<datalist>` element.

#### Index search

Manuals often contains index entries that allow accessing a specific section of the manual using the corresponding keyword.  This is possible to access them via the keyboard using the `i` key.  Like for the menu navigation completions are available in Web browsers that support the `<datalist>` element.

#### Global search

Instead of searching through a predefined set of keywords like in the index search, it is possible to search an arbitrary word with the `s` key.  The search is done across all the pages in order until one page matches the query.  When a page matches the query, it becomes the current page and the string matching the query is highlighted.  When the query fails an error message is displayed.

## Technical details

I will present shortly the main technical challenges of this project. You can read [the previous reports](/blog?categories=gsoc) to get more details on those challenges and learn about the issues I faced.

#### Using iframes

The Javascript UI uses the [one page per node](https://www.gnu.org/software/texinfo/manual/texinfo/html_node/HTML-Splitting.html#HTML-Splitting) output of `makeinfo`.  This has been done to allow fetching the pages as needed which has the benefit in a network context to not have to download the whole manual before being able to use it.  This is particularly important for big manuals like the Kawa one.  The display of those pages is done using iframes which doesn't depend on the use of a Web server like AJAX calls.  It was important to allow users use the Javascript UI via the `file://` protocol.  The difficulty of using iframes with such protocol, is that it requires using the [Message API](https://developer.mozilla.org/en-US/docs/Web/API/Window/postMessage) for communicating between the browsing contexts.  This work has been based on the prototype made by Per Bothner.

#### Transparent integration with links

Even if the pages are loaded in a iframe context, we want to be able to access the Javacript UI from every page and not only from "index.html".  Additionaly displaying the URL of the current iframe in the URL bar provides shorter and simpler URLs that are easier to share.  All of this has been enabled by the use of the [History API](https://developer.mozilla.org/en-US/docs/Web/API/History_API).

#### Adaptation to the makeinfo output

The first part of this project has been done using a complicated build process based on XML and XSL transformation provided by [Docbook](http://docbook.org/).  One optional goal of this project was to adapt the Javascript code, to make it work directly with the output of `makeinfo --html`.  This has been successfully achieved.

#### Modularity

Initially I have been using some tools allowing me to separate my code in different modules and to use ECMAScript 2017 syntax.  However those tools were heavily tied to the Node Package Manager (NPM).  The associated distribution paradigm doesn't accomodate well with the GNU Build System and the GNU GPL requirements.  As a consequence I choose to drop them and transition towards a monolithic script written in portable ECMAScript 5.  Even if NPM is not involved in the build process anymore, it can still be used for developer tools such as a linter and a type checker.

#### Portability

The portability is really important in our use case.  As a consequence, I have spent some amount of time testing with older browsers.  A fallback to basic HTML navigation has been added to support old browsers.  The detection of browsers capabilities has been done using feature tests provided by [Modernizr](https://modernizr.com/).

## Conclusion

This project has not being merged in Texinfo [SVN repository](https://svn.savannah.gnu.org/viewvc/texinfo/trunk/) yet.  However it should happen in the following months.  Regarding the missing features, probably the most notable one is the absence of support of Regular expression in the global search.  Additionally the use of the *space bar* and *backspace* keys for navigation has not been investigated.

I would like to thank my mentors Gavin Smith and Per Bothner for their guidance and dedication.  Being able to spend my summer on this project has been a pleasant experience that allowed me to become more familiar with JavaScript and Web programming in general.
