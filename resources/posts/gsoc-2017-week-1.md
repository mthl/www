---
title: Javascript for info-style navigation - Week 1
creator: Mathieu Lirzin
date: 2017-06-03T17:46:25+02:00
subject:
    - gsoc
    - gnu
    - texinfo
    - javascript
---

This summer I am doing a [Google Summer of Code](https://summerofcode.withgoogle.com/) for the [GNU project](https://www.gnu.org).  The project I will work on is to implement a Javascript UI for manuals generated with [GNU Texinfo](https://www.gnu.org/texinfo).  This [project](https://summerofcode.withgoogle.com/projects/#6199074135998464) is mentored by Gavin Smith which is the maintainer of Texinfo and [Per Bothner](http://per.bothner.com/) which is the main developper of [GNU Kawa](https://www.gnu.org/software/kawa) an implementation of the [Scheme language](https://en.wikipedia.org/wiki/Scheme_(programming_language)) using the Java virtual Machine (JVM).

## Introduction

Currently Texinfo can generate static web pages from a file using the Texinfo markup with the following command:

```shell
$ makeinfo --html file.texi
```

This generates a set a HTML pages corresponding to the nodes of the `.texi` file.  It is possible to add the `--no-split` option to combine all the node in one page.  [Here](https://www.gnu.org/software/hello/manual/html_node/index.html) is an example of one node per page HTML manual and [here](https://www.gnu.org/software/hello/manual/hello.html) a version of the same manual, with all nodes in one page.

Alternatively to the *HTML* output, the traditional output of `makeinfo` in the *info* format which can be read both by the `info` program and with `info-mode` inside emacs.  This output while being text based offers really nice features such as the keyboard navigation which offers efficient way to access the index with `i` key or search through the whole manual with the `s` key.  Here is a screenshot of what the [GNU Libc](https://www.gnu.org/software/libc/) manual looks like using `info`.

![](/images/info-libc.png)

While the *info* output empowers its users, it is not the most accessible way to read documentation because it relies on either using `emacs` or the terminal, and requires users to use the keyboard.  The goal of this *Google Summer of Code* project is to empower the users of the *HTML* documentation format with the same features which are available to the users of `emacs` or `info` by implementing them in *Javascript* which is the engine available is modern Web browsers.

For starting my project, I have been working on the basic keyboard navigation which seems like the most approchable 
feature to add.  This has allowed me to familiarize myself with the existing prototype that my mentor [Per Bothner](http://per.bothner.com/) had previously written for the Kawa manual.

## Existing prototype

![](/images/kawa-prototype.png)

The prototype is implementing a smooth navigation between the nodes by not reloading the entire page between each link and not having to display all the manual like what is done when using the `--no-split` option of `makeinfo`.  This is working by using one node per page HTML files and combine them using [iframes](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/iframe).  The idea is to keep the possibility of reading the manual without javascript, and benefit of having small payloads to transfert only when needed.  For example with the [emacs manual](https://www.gnu.org/software/emacs/manual/emacs.html), downloading the manual with all nodes in one page weights 3.5MB which is huge.  The drawback is that using *iframes* bring complexity since it requires dealing with the [Same Origin Policy](https://en.wikipedia.org/wiki/Same-origin_policy) which is really restrictive when using the `file://` pseudo protocol that we want to support.  As a consequence every communication between *iframes* has to be done using message passing using the `Window.postMessage` method.  When comparing that with basic DOM manipulation inside a unique `Window` object, this is significantly more complex.  Moreover event handlers has to take care of the *iframe* context where they are executed.

## Adding basic keyboard navigation

For the basic navigation I have decided to start implementing the `n`, `p`, `u` keys.  To implement keyboard navigation in Javascript, we have to start defining an event handler for the *keypress* event and then adapt the behavior in the callback by accessing the `key` property of the event which is passed as a parameter of the callback.

```js
function
on_keypress (event)
{
  switch (event.key)
    {
    case "n":
    case "p":
    case "u":
    default:
      break;
    }
}

window.addEventListener ("keypress", on_keypress, false);
```

For each case we want the *top* window to load the corresponding page in an *iframe* and make it the only one visible.  Since the event will be handled in an *iframe* we need to send a message to the *top* window.

```js
    case "n":
      top.postMessage ({ message_kind: "load-page", nav: "next" }, "*");
      break;
    case "p":
      top.postMessage ({ message_kind: "load-page", nav: "prev" }, "*");
      break;
    case "u":
      top.postMessage ({ message_kind: "load-page", nav: "up" }, "*");
      break;
    default:
      break;
```

This message has to be received by defining and handler for the *message* event.

```js
function
receiveMessage (event)
{
  var data = event.data;
  switch (data.message_kind)
    {
    case "load-page":           /* from key handler to top frame */
      {
        let ids = loaded_nodes.data[loaded_nodes.current];
        if (ids[data.nav])
          loadPage (ids[data.nav] + ".xhtml", "");
        break;
      }
     ...
    }
}

window.addEventListener ("message", receiveMessage, false);
```

The `loaded_node` is a global object treated as a dictionary containing the ids of the *next*, *previous*, and *up* links.  The associated id is then passed to the `loadPage` function which is in charge of loading the corresponding page in an *iframe* and making it the only one visible.  `loaded_node` is populated by `loadPage` by scanning the DOM of the loaded *iframe* which contains the *next*, *previous*, and *up* links.  This is done with the `navigation_link` function which uses the fact that the links can be identified with their `accesskey` attribute.

```js
function
navigation_links (content)
{
  let as = Array.from (content.querySelectorAll("footer a"));
  return as.reduce ((acc, node) => {
    let href = node.getAttribute ("href");
    let id = href.replace(/.*#/, "");
    switch (node.getAttribute ("accesskey"))
      {
      case "n":
        return Object.assign (acc, { next: id });
      case "p":
        return Object.assign (acc, { prev: id });
      case "u":
        return Object.assign (acc, { up: id });
      default:
        return acc;
      }
  }, {});
}
```

I won't get into more details of how `loadPage` actually works since this not related to the keyboard navigation.

## Build system

The prototype was written as a simple script.  Given the features that we want to have, this approach will not scale well.  As a consequence I have added the support for ES6 modules with the help of [Rollup](https://rollupjs.org/) which is a module bundler.  For more readable and maintainable code, I want to use modern Javascript features which are unfortunately not available in the majority of Browsers currently used.  To allow using those modern features while keeping portability, the solution is to use a *transpiler*.  I have decided to use [Buble](https://buble.surge.sh/) for that.  This kind of tooling bring us to an implicit consequence of using the `npm` package manager.  While this is not mandatory, this is would rather be inconvenient to bundle those dependencies by hand.

The open question concerns how to integrate that in the current Texinfo build system uses the [GNU Build System](https://www.gnu.org/software/automake/manual/html_node/GNU-Build-System.html#GNU-Build-System) and the [GNU Coding Standards](https://www.gnu.org/prep/standards/html_node/index.html) requires the build process to work without network access.  This is not compatible how `npm` works.  One solution would be distribute the generated Javascript bundle in the tarball, and turn `npm` and `nodejs` into development dependencies that only maintainers and contributors would need to have them.  Additionally it should be possible to configure `autoconf` to make those dependencies optional even for developers.

## Next step

The basic keyboard navigation is not complete for now.  It lacks the implementation of the `[` and `]` keys which allows the user to navigate following a depth first walk through the manual instead of what is done with the `n` and `p` keys which moves only between siblings.

## Follow the developpement

The development of this project is done in public.  You can checkout the [Git repository](https://notabug.org/mthl/texinfo) to see what is the current state of the project.
