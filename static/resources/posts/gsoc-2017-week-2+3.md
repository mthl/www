---
title: Javascript for info-style navigation - Week 2 & 3
creator: Mathieu Lirzin
date: 2017-06-16T12:45:58+02:00
subject:
    - gsoc
    - texinfo
    - javascript
    - redux
    - graph
---

This is an update on the work I am doing this summer for my [Google Summer of Code](https://summerofcode.withgoogle.com/projects/#6199074135998464).  Please see [the first article](./gsoc-2017-week-1.md) of this serie of reports for a general introduction on what this project is about.

## Implementing *backward* and *forward* navigation keys

I have implemented the navigation with the `[` and `]` keys which respectively correspond to **backward** and **forward**. Those keys allow the user to traverse the manual linearly, compared to the `p` and `n` keys which traverse the nodes of the manual considering only considering the nodes of the same level and not their sub-nodes.

A manual is actually a tree of nodes with the beginning of the manual representing the root of the tree, and the sections it is composed of representing the children nodes.  With that model in mind, the linear traversal of the manual correspond to a tree traversal using a [depth first search](https://en.wikipedia.org/wiki/Depth-first_search) algorithm.

Contrary to the previously implemented navigation links the **backward** and **forward** links are not present in the HTML pages.  As a consequence they need to be computed from the table of content, which contains all the information needed.  Indeed the table of content contains an `<ul>` element with `<li>` children elements that corresponds to tree structure of the manual.  Using the *depth first search* traversal of those elements allows us to achieve the computation we want with the `create_link_dict` function which takes the top `<ul>` node as an argument and for each of its children apply the `add_link` internal function.  `add_link` populates `links` the appropriate key value pairs by using `prev_id` which store the previously seen `href` attribute.  Since we use a *depth first search* traversal the previously seen element correspond to the *backward* link and reciprocally the current `href` corresponds to the *forward* link of the previously seen `href`. 

```javascript
function
create_link_dict (nav)
{
  let prev_id = "*TOP*";
  let links = {};

  function
  add_link (elem)
  {
    if (elem.matches ("a") && elem.hasAttribute ("href"))
      {
        let id = elem.getAttribute ("href");
        links[prev_id] = Object.assign ({}, links[prev_id], { forward: id });
        links[id] = Object.assign ({}, links[id], { backward: prev_id });
        prev_id = id;
      }
  }

  depth_first_walk (nav, add_link, Node.ELEMENT_NODE);
  return links;
}

let links = create_link_dict (document.querySelector ("ul"));
```

The actual implementation of `depth_first_walk` is simple since it consists of applying `func` to `node` and recursively applying `func` to all its `children` in order.  The particularity of this implementation is that we have an additional `node_type` argument which allows to filter the nodes we want to consider with `func`.  This argument is a [Node type constant](https://developer.mozilla.org/en-US/docs/Web/API/Node/nodeType#Node_type_constants) which is compared to the actual `nodeType` property of the `node` argument.

```javascript
function
depth_first_walk (node, func, node_type)
{
  if (!node_type || (node.nodeType === node_type))
    func (node);

  for (let child = node.firstChild; child; child = child.nextSibling)
    depth_first_walk (child, func, node_type);
}
```

The dictionary returned by `create_link_dict` is used by the global key event handler to retrieve the *iframe* identifier we need to make visible when going **forward** or **backward**.

## Improving the architecture

The original prototype I have started working with, had no particular architecture attached to it, meaning there was no separate model, in other words the model was the DOM itself.  This approach which is nice when prototyping doesn't scale well when combining features.  The problem is that the asynchronicity of events combined with constant mutation of the DOM is hard to reason about and  make it easy to introduce bugs.  The solution to that issue is to separate the **view** from the **model**.  The *model* contains the logic and the data structures of the application.  The view(s) contain(s) a particular representation of the model.  In our case the representation is the *DOM*.  The fuzzy part is to make the *model* and the *view* interact nicely.  The traditional solution consists of using an [Model View Controller](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93controller) architecture (MVC) which brings a *Controller* to manage the interactions between the *view* and the *model*.  This means handling click events and updating the *view* when the model changes.  While this architecture is traditional, it brings with it a dose of complexity because of the *two-way dataflow* between the *model* and the *view*.  A modern alternative to that architecture is to use a *Unidirectional dataflow* like what is implemented by the [Redux](http://redux.js.org/) Javascript framework.  The idea is basically to implement the model using an **unique state object** and **reducers** which are pure functions taking an *action* and a *state* and returning a new *state*.  The view is then only a function of the *state* to *void* that **renders the state inside the DOM**.  *Actions* are sent to a global **store** via event handlers.  The **store** is the object responsible of the state of the application and applying the reducers to it.  This short explanation without any diagram is obviously not sufficient to get whole picture, so I encourage everyone to look around the internet for more information.

For this project, I have tried to use this *Unidirectional dataflow* by reimplementating something similar to what *Redux* does.  For now this works fine, and allows me to reason about the program more easily.

## Next step

The basic key navigation implementation is now done.  The next step of this project will be to implement the *menu navigation* which consists of allowing the users based on a text input field to access a particular sub-node of the manual.  This would serve as a basis for further features based on text inputs such as the *index search* and *regexp search*. 

## Follow the developpement

The development of this project is done in public.  You can checkout the [Git repository](https://notabug.org/mthl/texinfo) to see what is the current state of the project.
