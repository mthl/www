---
title: Javascript for info-style navigation - Week 7 & 8
creator: Mathieu Lirzin
date: 2017-07-23T13:21:29+02:00
subject:
    - gsoc
    - texinfo
    - javascript
    - asynchronicity
---

This is an update on the work I am doing this summer for my [Google Summer of Code](https://summerofcode.withgoogle.com/projects/#6199074135998464).  Please see [the first article](http://mathieu.lirzin.emi.u-bordeaux.fr/2017/06/03/gsoc2017-1/) of this serie of reports for a general introduction on what this project is about.

## Keyboard navigation from the main page

Regarding the build of the HTML manuals there were some minor issues such as the `next` navigation command that didn't work on the main page.  While the integration of the build process of the manual taken from Kawa was quite straight forward.  This particular issue was harder to solve.  This required me to acquire some basic knowledge about Docbook, XML, and XSLT that I lacked.  To solve this particular issue has been donne by modifying the xml source with docbook specific XSL tranformation managing the hierarchy of the first pages of the manual.  In order to have a `next` navigation command for the main page working we have to ensure it is at the same level of the tree as the first following page.

## HTML file extensions

I have convert the file extensions from `.xhtml` to `.html`.  The consequence of changing the extension was that files were parsed differently by the browser, which bring some incompatibilities.  To fix those file, ideally we would provide XSL tranformations on top of what is done by Docbook stylesheets.  however, my limited knowledge regarding XSL, I have prefered the use of `sed` which is more easy for me to work with.  `sed` has the drawback that the transformation it applies might silently fail in case of formatting changes in the Docbook XHTML output.

## Type checking

One issue with JavaScript and loosely typed languages in general is that you often detect bugs at runtime.  TypeScript is a superset of Javacript that defines a type system on top of Javascript and then compiles down to Javascript.  Often Types serve as a way to optimize the generated code, However in the case of TypeScript that is not the case.  This is only used to detect issues about the code and to reason about it more easily.  This type system embrace the loosely typed nature of its subset by introducing a type `any` which permits to any Javascript code to be valid TypeScript code.  The more precise types are refinements on top of the `any` type.

The canonical way to add type information with Typescript is associated with the specific syntax `function f (x: type): type` for function signatures.  Since TypeScript 2.3 it is possible to add type annotations in the form of JSDoc comments in basic Javascript code.  This allows us to optionally benefit from the TypeScript developper tools, while not depending on its tool chain.

## Display of the URL

The generated html pages, we are working with has the form "index.html", "foo.html", "bar.html" ... .  The JavaScript application which is using iframes to display them from the "index.html" had URL of the form `prefix/index.html#pageid` or `prefix/index.html#pageid.nodeid` when the page is associated multiple nodes.  It has already been possible to refer to them using `prefix/nodeid.html` or `prefix/pageid.html#nodeid` (modulo a bug described in next section).  This is redirecting to the `index.html` page with the corresponding iframe set as current.  Previously the redirection had the consequence that the displayed URL matches the first form we described.  However the [History API](https://developer.mozilla.org/en-US/docs/Web/API/History_API) it is possible to choose the form of the displayed URL we want by adapting the third argument of the  `history.pushState` and `history.replaceState` methods.

When using the `file:` protocol, depending on the browser used it is not possible to set a different file name that the one actually loaded.  This is due to some browser specific *Same-Origin Policy* (Chromium in that case). To work around that, I have used some error handling to fallback to only modifying the hash part of the displayed URL, like what was previously done.

```js
var visible_url = foo.html
try
  {
    window.history.pushState (linkid, null, visible_url);
  }
catch (err)
  {
    /* Fallback to changing only the hash part which is safer.  */
    visible_url = "index.html#foo";
    window.history.pushState (linkid, null, visible_url);
  }
```

## Reliable asynchronicity

Certains nodes of the manuals can actually be contained in the same HTML page.  To provide a link to them we are using anchors that identifies them.  One issue I discovered while working on the previous topic.  Is that everything worked fine when clicking on a link that was refering to one of those node, but when loading it from the corresponding URL, the scroll to the appropriate anchor was not happening.  After Some investigation I have discovered that there was a concurrency issue between the load of the iframe containing the anchor node and the send of the "scroll-to" message from the top page to that iframe.

What was happening is that the "scroll-to" messages were not received since the message handler on the other side was not ready.  To work around that issue I have added an event listener on the load of the iframe queue of messages that were sent before that `load` event.

```js
var iframe = div.querySelector ("iframe");
if (!iframe)
  {
    iframe = document.createElement ("iframe");
    iframe.setAttribute ("class", "node");
    iframe.setAttribute ("src", linkid_to_url (pageid));
    div.appendChild (iframe);
    iframe.addEventListener ("load", function (event) {
      /* Send pending messages.  */
      var msgs = resolve_page.pendings[pageid];
      if (msgs)
        {
          for (var i = 0; i < msgs.length; i += 1)
            this.contentWindow.postMessage (msgs[i], "*");
        }
      pending_messages[pageid] = false;
    }, false);
  }
if (scroll)
  {
    msg = { message_kind: "scroll-to", hash: hash };
    if (pending_messages[pageid] === false)
      iframe.contentWindow.postMessage (msg, "*");
    else if (pending_messages.hasOwnProperty (pageid))
      pending_messages[pageid].push (msg);
    else
      pending_messages[pageid] = [msg];
  }
```

Ideally we would want to use [Promises](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise) in such situation.  The concept of events is more appropriate for things than can happen multiple times such as mouse clicks.  However for loading the page in an iframe, this should only happen once and what is require is only add a dependency between the readiness of the iframe and the send of the message.  However there is an issue of portability, since [promisses are not supported by Internet Explorer](https://caniuse.com/#feat=promises).  We might add a [polyfill](https://github.com/stefanpenner/es6-promise) for it, but since it weights 2.4 KB, it might arguably not worth it for the particular issue we are solving.

## Help screen

The Keyboard UI was assuming that the user was already familiar with the `info` shortcuts.  However in term of discoverability this was not ideal.  To improve that I have added a `?` button which displays an overlay screen summarizing all the keyboard shortcuts available along with their associated action.

![](/images/info_js_help.png)

## Portability

As described in previous report [modernizr](https://modernizr.com/) has been integrated in the code.  The grain of feature requirements is still gross since most of them result in fallback to basic HTML.  The only exception to that rule concerns the text inputs (for the menu and index search) which handle the case of not having the [`<datalist>`](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/datalist) feature by not displaying any completion.

## Next Step

One issue that has been reported is that the lateral table of content is not automatically scrolling to ensure that the highlighted node is visible.  I will try to fix that.

## Follow the developpement

I have updated the live demo of the Kawa manual which is available [here](https://www.gnu.org/software/texinfo/gsoc-2017-js-example/kawa).  If you have already accessed this page, it is possible that you face invalid cache issues.  Make sure that your local cache is cleared.

The development of this project is done in public.  You can checkout the "js" directory in the "gsoc-2017" branch of the [Git repository](https://git.savannah.gnu.org/git/texinfo.git) and run the build instructions from the `README` to see what is the current state of the project.
