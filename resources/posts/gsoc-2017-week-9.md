---
title: Javascript for info-style navigation - Week 9
creator: Mathieu Lirzin
date: 2017-07-28T14:41:57+02:00
subject:
    - gsoc
    - texinfo
    - javascript
---

This is an update on the work I am doing this summer for my [Google Summer of Code](https://summerofcode.withgoogle.com/projects/#6199074135998464).  Please see [the first article](./gsoc-2017-week-1.md) of this serie of reports for a general introduction on what this project is about.

## Sidebar scrolling

In order to help users knowing where the current page is located inside the global manual, we provide a sidebar containing the table of content which highlights the current node.  In the case of a huge manual that has a long table of content like the Kawa manual, It is possible to have a scollbar for the lateral iframe when accessing a deeply nested node.  It is important that the current highlighted node stays visible when navigating with the keyboard.  For that I have used the `Element.scrollIntoView` method in the sidebar browsing context which when applied on the highlighted link adapt the scrolling accordingly.
This method is called when handling the `update-sidebar` message which is responsible for changing the currently displayed link.

```js
function
on_message (event)
{
  var data = event.data;
  if (data.message_kind === "update-sidebar")
    {
      var selected = data.selected;
      /* ...Highlight the node corresponding to SELECTED.  */
      var elem = document.getElementById (selected);
      if (elem)
        elem.scrollIntoView (true);
    }
}

if (inside_sidebar)
  window.addEventListener ("message", on_message, false);
```

While this is working nicely, before knowing this method, I have tried using `Location.replace` instead to change the hash part of the URL of the iframe.  Using this method was definitely not the right approach since it messes with the focus of elements on Chromium.

## Iframe reloads

Thanks to my mentor Per Bothner, we have noticed that the features added last week has the undesired effect of automatically reloading iframes every time, instead of loading it once and only switch their visibility the next times.  This bug was the consequence of calling `Location.replace` with the empty string.  The intending effect was to scroll to the top of the page and this can be acheived with `window.scroll (0, 0)`.

## Compatibility warning

When the browser used for reading the manual doesn't have the minimum requirements for making the Javascript UI work, we fallback to a basic HTML navigation.  We want to notify the user that the script was unable to properly work in that browser, instead of silently fallback.  For that I have added a simple warning message that popup and dissapear after 3 seconds.

![](/images/compat-warn.png)

## Various improvements

* When moving from using `.xhtml` to using `.html` extensions for files, I didn't notice that links     needed to be fixed even when falling back to basic HTML navigation.  This has been fixed.
* The part managing the visible node in the sidebar has been refactored to use the generic `depth_first_walk` function.
* The dependency on the [URL API](https://developer.mozilla.org/en-US/docs/Web/API/URL) has been removed
* Additional type information using [TypeScript JSDoc comments](https://github.com/Microsoft/TypeScript/wiki/JsDoc-support-in-JavaScript) has been added

## Follow the developpement

I have updated the live demo of the Kawa manual which is available [here](https://www.gnu.org/software/texinfo/gsoc-2017-js-example/kawa).  If you have already accessed this page, it is possible that you face invalid cache issues.  Make sure that your local cache is cleared.

The development of this project is done in public.  You can checkout the "js" directory in the "gsoc-2017" branch of the [Git repository](https://git.savannah.gnu.org/git/texinfo.git) and run the build instructions from the `README` to see what is the current state of the project.
